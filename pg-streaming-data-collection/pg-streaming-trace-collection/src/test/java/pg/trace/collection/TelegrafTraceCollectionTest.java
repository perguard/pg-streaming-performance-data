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

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pg.trace.model.*;

import java.util.Properties;

public class TelegrafTraceCollectionTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, SystemTrace> outputTopic;
    private KafkaConfiguration config;

    @Before
    public void setup() {
        this.config = new KafkaConfiguration("test:1234", "group", "input", "output", KafkaConfiguration.CollectionSource.TELEGRAF, "mock://feature-test");
        final Properties kafkaProperties = TraceCollectionKafaStreams.buildKafkaProperties(this.config);
        final Topology topology = TraceCollectionKafaStreams.buildTopology(this.config);
        this.testDriver = new TopologyTestDriver(topology, kafkaProperties);

        SpecificAvroSerde<SystemTrace> systemTraceSerde = PgModelSerde.getSpecificAvroSerde(config.getSchemaRegistryUrl());
        inputTopic = testDriver.createInputTopic(config.getInputTopic(), new StringSerializer(), new StringSerializer());
        outputTopic = testDriver.createOutputTopic(config.getOutputTopic(), new StringDeserializer(), systemTraceSerde.deserializer());
    }

    @Test
    public void shouldProcessCPUMeasurement() {
        String msg = "cpu,cpu=cpu-total,host=fortiss-n-111 usage_guest=0,usage_guest_nice=0,usage_user=4.58984375,usage_idle=94.0234375,usage_iowait=0,usage_irq=0,usage_steal=0,usage_system=1.38671875,usage_nice=0,usage_softirq=0 1603371510000000000"; // timestamp: 10/22/2020 @ 12:58:30 UTC

        inputTopic.pipeInput(msg);
        SystemTrace systemTrace = outputTopic.readValue();

        Assert.assertEquals(Long.valueOf(1603371510000L), systemTrace.getTimestamp());
        Assert.assertEquals("fortiss-n-111", systemTrace.getHostname());
        Assert.assertEquals(TraceMetric.CPU, systemTrace.getMetric());
        Assert.assertEquals(TraceMeasure.UTILIZATION, systemTrace.getMeasure());
        Assert.assertEquals("5.9765625", systemTrace.getValue());
        Assert.assertEquals(TraceUnit.PERCENT, systemTrace.getUnit());
        Assert.assertTrue(outputTopic.isEmpty());
    }

    @Test
    public void shouldProcessMemoryMeasurement() {
        String msg = "mem,host=fortiss-n-111 used_percent=43.37512260322711,available_percent=56.62487739677289,total=17038376960i,available=9647960064i,used=7390416896i 1603371570000000000"; // timestamp: 10/22/2020 @ 12:59:30 UTC

        inputTopic.pipeInput(msg);
        SystemTrace systemTrace = outputTopic.readValue();

        Assert.assertEquals(Long.valueOf(1603371570000L), systemTrace.getTimestamp());
        Assert.assertEquals("fortiss-n-111", systemTrace.getHostname());
        Assert.assertEquals(TraceMetric.MEMORY, systemTrace.getMetric());
        Assert.assertEquals(TraceMeasure.UTILIZATION, systemTrace.getMeasure());
        Assert.assertEquals("43.37512260322711", systemTrace.getValue());
        Assert.assertEquals(TraceUnit.PERCENT, systemTrace.getUnit());
        Assert.assertTrue(outputTopic.isEmpty());
    }

    @Test
    public void shouldProcessDiskIOMeasurement() {
        String msg = "diskio,host=fortiss-n-111,name=C: write_bytes=11768876032i,read_time=815i,write_time=526i,weighted_io_time=0i,iops_in_progress=0i,reads=501689i,writes=451277i,read_bytes=11983475712i,io_time=0i,merged_reads=0i,merged_writes=0i 1603371760000000000"; // timestamp: 10/22/2020 @ 13:02:40 UTC

        inputTopic.pipeInput(msg);
        SystemTrace systemTrace = outputTopic.readValue();

        Assert.assertEquals(Long.valueOf(1603371760000L), systemTrace.getTimestamp());
        Assert.assertEquals("fortiss-n-111", systemTrace.getHostname());
        Assert.assertEquals(TraceMetric.DISK_READ, systemTrace.getMetric());
        Assert.assertEquals(TraceMeasure.RESPONSE_TIME, systemTrace.getMeasure());
        Assert.assertEquals("815", systemTrace.getValue());
        Assert.assertEquals(TraceUnit.MILLISECONDS, systemTrace.getUnit());
        Assert.assertTrue(outputTopic.isEmpty());
    }

    @After
    public void tearDown() {
        testDriver.close();
    }

}
