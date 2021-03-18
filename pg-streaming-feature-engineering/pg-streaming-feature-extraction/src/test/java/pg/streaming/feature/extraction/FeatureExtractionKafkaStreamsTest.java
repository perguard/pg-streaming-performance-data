/*
 * Copyright (c) 2021 fortiss - Research Institute of the Free State of Bavaria
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package pg.streaming.feature.extraction;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.WindowedSerdes;
import org.apache.kafka.streams.kstream.internals.TimeWindow;
import org.apache.kafka.test.TestUtils;
import org.junit.Before;
import org.junit.Test;
import pg.streaming.feature.extraction.factory.FeatureFactory;
import pg.streaming.schema.Feature;
import pg.streaming.schema.FeatureKey;
import pg.streaming.schema.PgStreamingSerde;
import pg.trace.model.SystemTrace;
import pg.trace.model.TraceMeasure;
import pg.trace.model.TraceMetric;
import pg.trace.model.TraceUnit;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


public class FeatureExtractionKafkaStreamsTest {

    private KafkaConfiguration config;
    private TopologyTestDriver testDriver;
    private Serde<SystemTrace> systemTraceSerde;
    private Serde<Feature> featureSerde;
    private WindowedSerdes.TimeWindowedSerde<FeatureKey> windowedFeatureKeySerde;

    @Before
    public void setup() {
        this.config = new KafkaConfiguration("test:1234", "group", "input", "output", 10, 5, "mock://feature-test");
        final Properties streamsConfiguration = FeatureExtractionKafkaStreams.buildKafkaProperties(config, TestUtils.tempDirectory().getAbsolutePath());
        final Topology topology = FeatureExtractionKafkaStreams.buildTopology(config);
        this.testDriver = new TopologyTestDriver(topology, streamsConfiguration);

        this.systemTraceSerde = PgStreamingSerde.getSpecificAvroSerde(config.getSchemaRegistryUrl());

        long windowSize = Duration.ofSeconds(config.getWindowDurationInSeconds()).toMillis();
        this.featureSerde = PgStreamingSerde.getSpecificAvroSerde(config.getSchemaRegistryUrl());
        SpecificAvroSerde<FeatureKey> featureKeySerde = PgStreamingSerde.getSpecificAvroSerde(config.getSchemaRegistryUrl());
        this.windowedFeatureKeySerde = new WindowedSerdes.TimeWindowedSerde<>(featureKeySerde, windowSize);
    }

    @Test
    public void shouldSeparateValuesIntoDistinctKeys() {
        final TestInputTopic<String, SystemTrace> actualInputTopic = testDriver.createInputTopic(config.getInputTopic(), new StringSerializer(), systemTraceSerde.serializer());
        final TestOutputTopic<Windowed<FeatureKey>, Feature> actualOutputTopic = testDriver.createOutputTopic(config.getOutputTopic(), windowedFeatureKeySerde.deserializer(), featureSerde.deserializer());

        final List<FeatureKey> actualFeatureKeys = Arrays.asList(
                new FeatureKey("hostA", TraceMetric.CPU, TraceMeasure.UTILIZATION, TraceUnit.PERCENT),
                new FeatureKey("hostA", TraceMetric.CPU, TraceMeasure.UTILIZATION, TraceUnit.MILLISECONDS),
                new FeatureKey("hostA", TraceMetric.CPU, TraceMeasure.RESPONSE_TIME, TraceUnit.PERCENT),
                new FeatureKey("hostA", TraceMetric.CPU, TraceMeasure.RESPONSE_TIME, TraceUnit.MILLISECONDS),
                new FeatureKey("hostA", TraceMetric.DISK_READ, TraceMeasure.UTILIZATION, TraceUnit.PERCENT),
                new FeatureKey("hostA", TraceMetric.DISK_READ, TraceMeasure.UTILIZATION, TraceUnit.MILLISECONDS),
                new FeatureKey("hostA", TraceMetric.DISK_READ, TraceMeasure.RESPONSE_TIME, TraceUnit.PERCENT),
                new FeatureKey("hostA", TraceMetric.DISK_READ, TraceMeasure.RESPONSE_TIME, TraceUnit.MILLISECONDS),
                new FeatureKey("hostB", TraceMetric.CPU, TraceMeasure.UTILIZATION, TraceUnit.PERCENT),
                new FeatureKey("hostB", TraceMetric.CPU, TraceMeasure.UTILIZATION, TraceUnit.MILLISECONDS),
                new FeatureKey("hostB", TraceMetric.CPU, TraceMeasure.RESPONSE_TIME, TraceUnit.PERCENT),
                new FeatureKey("hostB", TraceMetric.CPU, TraceMeasure.RESPONSE_TIME, TraceUnit.MILLISECONDS),
                new FeatureKey("hostB", TraceMetric.DISK_READ, TraceMeasure.UTILIZATION, TraceUnit.PERCENT),
                new FeatureKey("hostB", TraceMetric.DISK_READ, TraceMeasure.UTILIZATION, TraceUnit.MILLISECONDS),
                new FeatureKey("hostB", TraceMetric.DISK_READ, TraceMeasure.RESPONSE_TIME, TraceUnit.PERCENT),
                new FeatureKey("hostB", TraceMetric.DISK_READ, TraceMeasure.RESPONSE_TIME, TraceUnit.MILLISECONDS)
        );

        Instant instant = Instant.ofEpochMilli(1577833200000L);
        for (int i = 0; i < actualFeatureKeys.size(); i++) {
            FeatureKey key = actualFeatureKeys.get(i);
            actualInputTopic.pipeInput(new SystemTraceBuilder(key).setTimestamp(instant).setValue(i).build(), instant);
        }

        Map<Windowed<FeatureKey>, Feature> expectedOutput = new HashMap<>();
        for (int i = 0; i < actualFeatureKeys.size(); i++) {
            FeatureKey key = actualFeatureKeys.get(i);
            Feature feature = FeatureFactory.createFeature(new double[]{i});
            TimeWindow timeWindow0 = new TimeWindow(instant.minusSeconds(5).toEpochMilli(), instant.plusSeconds(5).toEpochMilli());
            TimeWindow timeWindow1 = new TimeWindow(instant.plusSeconds(0).toEpochMilli(), instant.plusSeconds(10).toEpochMilli());
            expectedOutput.put(new Windowed<>(key, timeWindow0), feature);
            expectedOutput.put(new Windowed<>(key, timeWindow1), feature);
        }

        Map<Windowed<FeatureKey>, Feature> actualOutput = actualOutputTopic.readKeyValuesToMap();
        assertThat(actualOutput.size(), equalTo(expectedOutput.size()));
        for (Map.Entry<Windowed<FeatureKey>, Feature> actualEntry : actualOutput.entrySet()) {
            Feature expectedFeature = expectedOutput.get(actualEntry.getKey());
            assertThat(actualEntry.getValue(), equalTo(expectedFeature));
        }
    }

    @Test
    public void shouldWindowAndHopValues() {
        final TestInputTopic<String, SystemTrace> actualInputTopic = testDriver.createInputTopic(config.getInputTopic(), new StringSerializer(), systemTraceSerde.serializer());
        final TestOutputTopic<Windowed<FeatureKey>, Feature> actualOutputTopic = testDriver.createOutputTopic(config.getOutputTopic(), windowedFeatureKeySerde.deserializer(), featureSerde.deserializer());

        Instant instant = Instant.ofEpochMilli(1577833200000L);
        FeatureKey key = new FeatureKey("hostA", TraceMetric.CPU, TraceMeasure.UTILIZATION, TraceUnit.PERCENT);

        Instant time0 = instant;
        Instant time1 = instant.plusSeconds(3);
        Instant time2 = instant.plusSeconds(12);
        Instant time3 = instant.plusSeconds(21);
        Instant time4 = instant.plusSeconds(24);
        actualInputTopic.pipeInput(new SystemTraceBuilder(key).setTimestamp(time0).setValue(10).build(), time0);
        actualInputTopic.pipeInput(new SystemTraceBuilder(key).setTimestamp(time1).setValue(20).build(), time1);
        actualInputTopic.pipeInput(new SystemTraceBuilder(key).setTimestamp(time2).setValue(30).build(), time2);
        actualInputTopic.pipeInput(new SystemTraceBuilder(key).setTimestamp(time3).setValue(40).build(), time3);
        actualInputTopic.pipeInput(new SystemTraceBuilder(key).setTimestamp(time4).setValue(50).build(), time4);

        TimeWindow timeWindow0 = new TimeWindow(instant.minusSeconds(5).toEpochMilli(), instant.plusSeconds(5).toEpochMilli());
        TimeWindow timeWindow1 = new TimeWindow(instant.plusSeconds(0).toEpochMilli(), instant.plusSeconds(10).toEpochMilli());
        TimeWindow timeWindow2 = new TimeWindow(instant.plusSeconds(5).toEpochMilli(), instant.plusSeconds(15).toEpochMilli());
        TimeWindow timeWindow3 = new TimeWindow(instant.plusSeconds(10).toEpochMilli(), instant.plusSeconds(20).toEpochMilli());
        TimeWindow timeWindow4 = new TimeWindow(instant.plusSeconds(15).toEpochMilli(), instant.plusSeconds(25).toEpochMilli());
        TimeWindow timeWindow5 = new TimeWindow(instant.plusSeconds(20).toEpochMilli(), instant.plusSeconds(30).toEpochMilli());

        Map<Windowed<FeatureKey>, Feature> expectedOutput = new HashMap<>();
        expectedOutput.put(new Windowed<>(key, timeWindow0), FeatureFactory.createFeature(new double[]{10.0, 20.0}));
        expectedOutput.put(new Windowed<>(key, timeWindow1), FeatureFactory.createFeature(new double[]{10.0, 20.0}));
        expectedOutput.put(new Windowed<>(key, timeWindow2), FeatureFactory.createFeature(new double[]{30.0}));
        expectedOutput.put(new Windowed<>(key, timeWindow3), FeatureFactory.createFeature(new double[]{30.0}));
        expectedOutput.put(new Windowed<>(key, timeWindow4), FeatureFactory.createFeature(new double[]{40.0, 50.0}));
        expectedOutput.put(new Windowed<>(key, timeWindow5), FeatureFactory.createFeature(new double[]{40.0, 50.0}));

        Map<Windowed<FeatureKey>, Feature> actualOutput = actualOutputTopic.readKeyValuesToMap();
        assertThat(actualOutput.size(), equalTo(expectedOutput.size()));
        for (Map.Entry<Windowed<FeatureKey>, Feature> actualEntry : actualOutput.entrySet()) {
            Feature expectedFeature = expectedOutput.get(actualEntry.getKey());
            assertThat(actualEntry.getValue(), equalTo(expectedFeature));
        }
    }

    private static class SystemTraceBuilder {

        private final SystemTrace trace;

        public SystemTraceBuilder(FeatureKey featureKey) {
            this.trace = new SystemTrace();
            this.trace.setHostname(featureKey.getHostname());
            this.trace.setMetric(featureKey.getMetric());
            this.trace.setMeasure(featureKey.getMeasure());
            this.trace.setUnit(featureKey.getUnit());
        }

        public SystemTraceBuilder setTimestamp(Instant date) {
            this.trace.setTimestamp(date.toEpochMilli());
            return this;
        }

        public SystemTraceBuilder setValue(int value) {
            this.trace.setValue(String.valueOf(value));
            return this;
        }

        public SystemTrace build() {
            return this.trace;
        }
    }

}