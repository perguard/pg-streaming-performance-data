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
package pg.streaming.labeling;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.*;
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
import pg.streaming.schema.Feature;
import pg.streaming.schema.FeatureKey;
import pg.streaming.schema.LabeledFeature;
import pg.streaming.schema.PgStreamingSerde;
import pg.trace.model.TraceMeasure;
import pg.trace.model.TraceMetric;
import pg.trace.model.TraceUnit;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


public class LabelingKafkaStreamsTest {

    private KafkaConfiguration config;
    private TopologyTestDriver testDriver;
    private Serde<LabeledFeature> labeledFeatureSerde;
    private Serde<Feature> featureSerde;
    private Serde<Windowed<String>> windowedStringSerde;
    private WindowedSerdes.TimeWindowedSerde<FeatureKey> windowedFeatureKeySerde;

    @Before
    public void setup() {
        this.config = new KafkaConfiguration("test:1234", "group", "feature-input", "target-input", "output", 10, 5, "mock://join-test");
        LabelingKafkaStreams app = new LabelingKafkaStreams();
        final Properties streamsConfiguration = app.buildKafkaProperties(config, TestUtils.tempDirectory().getAbsolutePath());
        final Topology topology = app.buildTopology(config);
        this.testDriver = new TopologyTestDriver(topology, streamsConfiguration);
        long windowSize = Duration.ofSeconds(config.getWindowDurationInSeconds()).toMillis();
        this.labeledFeatureSerde = PgStreamingSerde.getSpecificAvroSerde(this.config.getSchemaRegistryUrl());
        this.featureSerde = PgStreamingSerde.getSpecificAvroSerde(this.config.getSchemaRegistryUrl());
        SpecificAvroSerde<FeatureKey> featureKeySerde = PgStreamingSerde.getSpecificAvroSerde(this.config.getSchemaRegistryUrl());
        this.windowedFeatureKeySerde = new WindowedSerdes.TimeWindowedSerde<>(featureKeySerde, windowSize);
        this.windowedStringSerde = WindowedSerdes.timeWindowedSerdeFrom(String.class, windowSize);
    }

    @Test
    public void shouldAddLabelsToFeatures() {
        FeatureKey keyACpu = new FeatureKey("hostA", TraceMetric.CPU, TraceMeasure.UTILIZATION, TraceUnit.PERCENT);
        FeatureKey keyAMem = new FeatureKey("hostA", TraceMetric.MEMORY, TraceMeasure.THROUGHPUT, TraceUnit.BYTES);
        FeatureKey keyBCpu = new FeatureKey("hostB", TraceMetric.CPU, TraceMeasure.UTILIZATION, TraceUnit.PERCENT);

        Instant instant = Instant.ofEpochMilli(1577833200000L);
        TimeWindow window0 = new TimeWindow(instant.minusSeconds(5).toEpochMilli(), instant.plusSeconds(5).toEpochMilli());
        TimeWindow window1 = new TimeWindow(instant.plusSeconds(0).toEpochMilli(), instant.plusSeconds(10).toEpochMilli());

        Feature feature0 = Feature.newBuilder().setMean(0).build();
        Feature feature1 = Feature.newBuilder().setMean(1).build();
        Feature feature2 = Feature.newBuilder().setMean(2).build();

        final TestInputTopic<Windowed<FeatureKey>, Feature> actualFeatureInputTopic = testDriver.createInputTopic(config.getInputFeatureTopic(), windowedFeatureKeySerde.serializer(), featureSerde.serializer());
        final TestInputTopic<Windowed<String>, Integer> actualTargetInputTopic = testDriver.createInputTopic(config.getInputTargetTopic(), windowedStringSerde.serializer(), new IntegerSerializer());
        actualFeatureInputTopic.pipeInput(new Windowed<>(keyACpu, window0), feature0);
        actualFeatureInputTopic.pipeInput(new Windowed<>(keyAMem, window0), feature1);
        actualFeatureInputTopic.pipeInput(new Windowed<>(keyBCpu, window0), feature2);
        actualTargetInputTopic.pipeInput(new Windowed<>(keyACpu.getHostname(), window0), 0);
        actualTargetInputTopic.pipeInput(new Windowed<>(keyBCpu.getHostname(), window0), 1);
        actualTargetInputTopic.pipeInput(new Windowed<>(keyACpu.getHostname(), window1), 0);
        actualTargetInputTopic.pipeInput(new Windowed<>(keyBCpu.getHostname(), window1), 1);
        actualFeatureInputTopic.pipeInput(new Windowed<>(keyACpu, window1), feature0);
        actualFeatureInputTopic.pipeInput(new Windowed<>(keyAMem, window1), feature1);
        actualFeatureInputTopic.pipeInput(new Windowed<>(keyBCpu, window1), feature2);


        final TestOutputTopic<Windowed<FeatureKey>, LabeledFeature> actualOutputTopic = testDriver.createOutputTopic(config.getOutputTopic(), windowedFeatureKeySerde.deserializer(), labeledFeatureSerde.deserializer());
        LabeledFeature labeledFeature0 = LabeledFeature.newBuilder().setMean(feature0.getMean()).setLabel(0).build();
        LabeledFeature labeledFeature1 = LabeledFeature.newBuilder().setMean(feature1.getMean()).setLabel(0).build();
        LabeledFeature labeledFeature2 = LabeledFeature.newBuilder().setMean(feature2.getMean()).setLabel(1).build();

        Map<Windowed<FeatureKey>, LabeledFeature> expectedOutput = new HashMap<>();
        expectedOutput.put(new Windowed<>(keyACpu, window0), labeledFeature0);
        expectedOutput.put(new Windowed<>(keyAMem, window0), labeledFeature1);
        expectedOutput.put(new Windowed<>(keyBCpu, window0), labeledFeature2);
        expectedOutput.put(new Windowed<>(keyACpu, window1), labeledFeature0);
        expectedOutput.put(new Windowed<>(keyAMem, window1), labeledFeature1);
        expectedOutput.put(new Windowed<>(keyBCpu, window1), labeledFeature2);

        Map<Windowed<FeatureKey>, LabeledFeature> actualOutput = actualOutputTopic.readKeyValuesToMap();
        assertThat(actualOutput.size(), equalTo(expectedOutput.size()));
        for (Map.Entry<Windowed<FeatureKey>, LabeledFeature> actualEntry : actualOutput.entrySet()) {
            LabeledFeature expectedFeature = expectedOutput.get(actualEntry.getKey());
            assertThat(actualEntry.getValue().getMean(), equalTo(expectedFeature.getMean()));
            assertThat(actualEntry.getValue().getLabel(), equalTo(expectedFeature.getLabel()));
        }
    }

}