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
package pg.streaming.target.extraction;

import org.apache.kafka.common.serialization.IntegerDeserializer;
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
import pg.streaming.target.extraction.serdes.SpanArraySerializer;
import zipkin2.Endpoint;
import zipkin2.Span;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


public class TargetExtractionKafkaStreamsTest {

    private KafkaConfiguration config;
    private TopologyTestDriver testDriver;
    private final int slowdownThreshold = 1000;
    private final int acceptableDuration = 500;
    private final int slowdownDuration = 1500;
    private Serde<Windowed<String>> windowedSerde;


    @Before
    public void setup() {
        this.config = new KafkaConfiguration("test:1234", "group", "input", "output", 10, 5, this.slowdownThreshold);
        final Properties streamsConfiguration = TargetExtractionKafkaStreams.buildKafkaProperties(config, TestUtils.tempDirectory().getAbsolutePath());
        final Topology topology = TargetExtractionKafkaStreams.buildTopology(config);
        this.testDriver = new TopologyTestDriver(topology, streamsConfiguration);
        this.windowedSerde = WindowedSerdes.timeWindowedSerdeFrom(String.class, Duration.ofSeconds(config.getWindowDurationInSeconds()).toMillis());
    }

    @Test
    public void shouldSeparateValuesIntoDistinctKeys() {
        final TestInputTopic<String, Span[]> actualInputTopic = testDriver.createInputTopic(config.getInputTopic(), new StringSerializer(), new SpanArraySerializer());
        final TestOutputTopic<Windowed<String>, Integer> actualOutputTopic = testDriver.createOutputTopic(config.getOutputTopic(), this.windowedSerde.deserializer(), new IntegerDeserializer());

        Instant instant = Instant.ofEpochMilli(1577833200000L);

        Endpoint hostA = Endpoint.newBuilder().ip("192.168.0.101").build();
        Endpoint hostB = Endpoint.newBuilder().ip("192.168.0.102").build();

        Instant time0 = instant.plusSeconds(0);
        actualInputTopic.pipeInput(createSpan(time0, hostA, acceptableDuration), time0);
        actualInputTopic.pipeInput(createSpan(time0, hostB, slowdownDuration), time0);

        TimeWindow timeWindow0 = new TimeWindow(instant.minusSeconds(5).toEpochMilli(), instant.plusSeconds(5).toEpochMilli());
        TimeWindow timeWindow1 = new TimeWindow(instant.plusSeconds(0).toEpochMilli(), instant.plusSeconds(10).toEpochMilli());

        final Map<Windowed<String>, Integer> expectedOutput = new HashMap<>();
        expectedOutput.put(new Windowed<>(hostA.ipv4(), timeWindow0), 0);
        expectedOutput.put(new Windowed<>(hostA.ipv4(), timeWindow1), 0);
        expectedOutput.put(new Windowed<>(hostB.ipv4(), timeWindow0), 1);
        expectedOutput.put(new Windowed<>(hostB.ipv4(), timeWindow1), 1);

        Map<Windowed<String>, Integer> actualOutput = actualOutputTopic.readKeyValuesToMap();

        assertThat(actualOutput.size(), equalTo(expectedOutput.size()));
        for (Map.Entry<Windowed<String>, Integer> actualEntry : actualOutput.entrySet()) {
            Integer expectedTarget = expectedOutput.get(actualEntry.getKey());
            assertThat(actualEntry.getValue(), equalTo(expectedTarget));
        }
    }

    @Test
    public void shouldWindowAndHopValues() {
        final TestInputTopic<String, Span[]> actualInputTopic = testDriver.createInputTopic(config.getInputTopic(), new StringSerializer(), new SpanArraySerializer());
        final TestOutputTopic<Windowed<String>, Integer> actualOutputTopic = testDriver.createOutputTopic(config.getOutputTopic(), this.windowedSerde.deserializer(), new IntegerDeserializer());

        Endpoint hostA = Endpoint.newBuilder().ip("192.168.0.101").build();
        Instant instant = Instant.ofEpochMilli(1577833200000L);
        Instant time0 = instant.plusSeconds(0);
        Instant time1 = instant.plusSeconds(3);
        Instant time2 = instant.plusSeconds(12);
        Instant time3 = instant.plusSeconds(21);
        Instant time4 = instant.plusSeconds(24);

        actualInputTopic.pipeInput(createSpan(time0, hostA, acceptableDuration), time0);
        actualInputTopic.pipeInput(createSpan(time1, hostA, slowdownDuration), time1);
        actualInputTopic.pipeInput(createSpan(time2, hostA, acceptableDuration), time2);
        actualInputTopic.pipeInput(createSpan(time3, hostA, slowdownDuration), time3);
        actualInputTopic.pipeInput(createSpan(time4, hostA, acceptableDuration), time4);

        TimeWindow timeWindow0 = new TimeWindow(instant.minusSeconds(5).toEpochMilli(), instant.plusSeconds(5).toEpochMilli());
        TimeWindow timeWindow1 = new TimeWindow(instant.plusSeconds(0).toEpochMilli(), instant.plusSeconds(10).toEpochMilli());
        TimeWindow timeWindow2 = new TimeWindow(instant.plusSeconds(5).toEpochMilli(), instant.plusSeconds(15).toEpochMilli());
        TimeWindow timeWindow3 = new TimeWindow(instant.plusSeconds(10).toEpochMilli(), instant.plusSeconds(20).toEpochMilli());
        TimeWindow timeWindow4 = new TimeWindow(instant.plusSeconds(15).toEpochMilli(), instant.plusSeconds(25).toEpochMilli());
        TimeWindow timeWindow5 = new TimeWindow(instant.plusSeconds(20).toEpochMilli(), instant.plusSeconds(30).toEpochMilli());

        final Map<Windowed<String>, Integer> expectedOutput = new HashMap<>();
        expectedOutput.put(new Windowed<>(hostA.ipv4(), timeWindow0), 1);
        expectedOutput.put(new Windowed<>(hostA.ipv4(), timeWindow1), 1);
        expectedOutput.put(new Windowed<>(hostA.ipv4(), timeWindow2), 0);
        expectedOutput.put(new Windowed<>(hostA.ipv4(), timeWindow3), 0);
        expectedOutput.put(new Windowed<>(hostA.ipv4(), timeWindow4), 1);
        expectedOutput.put(new Windowed<>(hostA.ipv4(), timeWindow5), 1);

        Map<Windowed<String>, Integer> actualOutput = actualOutputTopic.readKeyValuesToMap();

        assertThat(actualOutput.size(), equalTo(expectedOutput.size()));
        for (Map.Entry<Windowed<String>, Integer> actualEntry : actualOutput.entrySet()) {
            Integer expectedTarget = expectedOutput.get(actualEntry.getKey());
            assertThat(actualEntry.getValue(), equalTo(expectedTarget));
        }

    }

    private Span[] createSpan(Instant instant, Endpoint remoteEndpoint, long duration) {
        return new Span[]{Span.newBuilder()
                .traceId("fb51b34e757fbd79")
                .id("525eee0c3b45fc82")
                .timestamp(instant.toEpochMilli())
                .remoteEndpoint(remoteEndpoint)
                .duration(duration)
                .build()};
    }

}