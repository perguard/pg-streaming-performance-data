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
package pg.trace.collection;

import java.io.InputStream;
import java.util.Properties;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import pg.trace.collection.nagios.ServiceStatusDTO;
import pg.trace.collection.nagios.ServiceStatusSerializer;
import pg.trace.model.*;

public class NagiosTraceCollectionTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, ServiceStatusDTO> inputTopicFromDTO;
    private TestInputTopic<String, String> inputTopicFromString;
    private TestOutputTopic<String, SystemTrace> outputTopic;
    private KafkaConfiguration config;

    @Before
    public void setup() {
        this.config = new KafkaConfiguration("test:1234", "group", "input", "output", KafkaConfiguration.CollectionSource.NAGIOS, "mock://feature-test");
        final Properties kafkaProperties = TraceCollectionKafaStreams.buildKafkaProperties(this.config);
        final Topology topology = TraceCollectionKafaStreams.buildTopology(this.config);
        this.testDriver = new TopologyTestDriver(topology, kafkaProperties);

        SpecificAvroSerde<SystemTrace> systemTraceSerde = PgModelSerde.getSpecificAvroSerde(config.getSchemaRegistryUrl());
        inputTopicFromString = testDriver.createInputTopic(config.getInputTopic(), new StringSerializer(), new StringSerializer());
        inputTopicFromDTO = testDriver.createInputTopic(config.getInputTopic(), new StringSerializer(), new ServiceStatusSerializer());
        outputTopic = testDriver.createOutputTopic(config.getOutputTopic(), new StringDeserializer(), systemTraceSerde.deserializer());
    }

    @Test
    public void shouldProcessSampleDTO() {
        ServiceStatusDTO testDTO = getSampleServiceStatusDTO();

        inputTopicFromDTO.pipeInput(testDTO);
        SystemTrace systemTrace = outputTopic.readValue();

        // 1584375356 is equivalent to 2020-03-16 16:15:56 UTC
        Assert.assertEquals(Long.valueOf(1584375356000L), systemTrace.getTimestamp());
        Assert.assertEquals("testhost", systemTrace.getHostname());
        Assert.assertEquals(TraceMetric.MEMORY, systemTrace.getMetric());
        Assert.assertEquals(TraceMeasure.UTILIZATION, systemTrace.getMeasure());
        Assert.assertEquals("18", systemTrace.getValue());
        Assert.assertEquals(TraceUnit.PERCENT, systemTrace.getUnit());
        Assert.assertTrue(outputTopic.isEmpty());
    }

    @Test
    public void shouldProcessSampleJson() {
        try (InputStream input = NagiosTraceCollectionTest.class.getResourceAsStream("/nagios-example.json")) {
            String msg = IOUtils.toString(input,"UTF-8");

            inputTopicFromString.pipeInput(msg);
            SystemTrace systemTrace = outputTopic.readValue();

            // confirm e.g. on https://www.unixtimestamp.com : 1589225116 is equivalent to 2020-05-11 19:25:16 (UTC)
            Assert.assertEquals(Long.valueOf(1589225116000L), systemTrace.getTimestamp());
            Assert.assertEquals("Cassandra", systemTrace.getHostname());
            Assert.assertEquals(TraceMetric.MEMORY, systemTrace.getMetric());
            Assert.assertEquals(TraceMeasure.UTILIZATION, systemTrace.getMeasure());
            Assert.assertEquals("28.10", systemTrace.getValue());
            Assert.assertEquals(TraceUnit.PERCENT, systemTrace.getUnit());
            Assert.assertTrue(outputTopic.isEmpty());
        } catch (Exception io) {
            System.out.println("Could not access sample JSON file:" + io.getLocalizedMessage());
        }
    }

    @After
    public void tearDown() {
        try {
            testDriver.close();
        } catch (final RuntimeException e) {
            // https://issues.apache.org/jira/browse/KAFKA-6647 causes exception when executed in Windows, ignoring it
            // Logged stacktrace cannot be avoided
            // This issue has been resolved in 2.6.0, 2.5.1 (Mar 2020)
            System.out.println("Ignoring exception, test failing in Windows due this exception:" + e.getLocalizedMessage());
        }
    }

    public static ServiceStatusDTO getSampleServiceStatusDTO() {
        ServiceStatusDTO serviceStatusDTO = new ServiceStatusDTO();
        serviceStatusDTO.setStatusUpdateTime("2020-03-16 16:15:56");
        serviceStatusDTO.setHostName("testhost");
        serviceStatusDTO.setDisplayName("Memory Usage");
        serviceStatusDTO.setPerformanceData(new com.fasterxml.jackson.databind.node.TextNode("18"));
        return serviceStatusDTO;
    }

}
