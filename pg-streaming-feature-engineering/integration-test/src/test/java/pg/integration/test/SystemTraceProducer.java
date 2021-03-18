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

package pg.integration.test;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.streaming.schema.PgStreamingSerde;
import pg.trace.model.SystemTrace;
import pg.trace.model.TraceMeasure;
import pg.trace.model.TraceMetric;
import pg.trace.model.TraceUnit;

import java.time.Instant;
import java.util.Properties;

public class SystemTraceProducer implements Runnable {

    private final Logger log = LoggerFactory.getLogger(SystemTraceProducer.class);
    private final Producer<String, SystemTrace> producer;
    private final String topic;
    private SystemTrace fooCPUSystemTraceTemplate;
    private SystemTrace fooMemorySystemTraceTemplate;
    private SystemTrace barCPUSystemTraceTemplate;
    private SystemTrace barMemorySystemTraceTemplate;

    public SystemTraceProducer(KafkaConfiguration config, String hostFoo, String hostBar) {
        final Properties kafkaProperties = new Properties();
        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        kafkaProperties.put(ProducerConfig.CLIENT_ID_CONFIG, config.getGroupId() + "-client");
        Serde<SystemTrace> systemTraceSerde = PgStreamingSerde.getSpecificAvroSerde(config.getSchemaRegistryUrl());
        this.producer = new KafkaProducer<>(kafkaProperties, new StringSerializer(), systemTraceSerde.serializer());
        this.topic = config.getInputSystemTraceTopic();
        this.setupTestSystemTraces(hostFoo, hostBar);
    }

    private void setupTestSystemTraces(String hostFoo, String hostBar) {
        this.fooCPUSystemTraceTemplate = SystemTrace.newBuilder().setHostname(hostFoo).setTimestamp(0L).setValue("")
                .setMeasure(TraceMeasure.UTILIZATION).setMetric(TraceMetric.CPU).setUnit(TraceUnit.PERCENT).build();
        this.fooMemorySystemTraceTemplate = SystemTrace.newBuilder().setHostname(hostFoo).setTimestamp(0L).setValue("")
                .setMeasure(TraceMeasure.UTILIZATION).setMetric(TraceMetric.MEMORY).setUnit(TraceUnit.MEGABYTES).build();
        this.barCPUSystemTraceTemplate = SystemTrace.newBuilder().setHostname(hostBar).setTimestamp(0L).setValue("")
                .setMeasure(TraceMeasure.UTILIZATION).setMetric(TraceMetric.CPU).setUnit(TraceUnit.PERCENT).build();
        this.barMemorySystemTraceTemplate = SystemTrace.newBuilder().setHostname(hostBar).setTimestamp(0L).setValue("")
                .setMeasure(TraceMeasure.UTILIZATION).setMetric(TraceMetric.MEMORY).setUnit(TraceUnit.MEGABYTES).build();
    }

    @Override
    public void run() {
        log.info("Producing system traces");
        this.produceSystemTraceRecords();
    }

    private void produceSystemTraceRecords() {
        Long timestamp = Instant.now().toEpochMilli();
        produceSystemTraceRecord(timestamp, String.valueOf(new NormalDistribution(40,5).sample()), fooCPUSystemTraceTemplate);
        produceSystemTraceRecord(timestamp, String.valueOf(new NormalDistribution(400,5).sample()), fooMemorySystemTraceTemplate);
        produceSystemTraceRecord(timestamp, String.valueOf(new NormalDistribution(60,5).sample()), barCPUSystemTraceTemplate);
        produceSystemTraceRecord(timestamp, String.valueOf(new NormalDistribution(600,5).sample()), barMemorySystemTraceTemplate);
    }

    private void produceSystemTraceRecord(long timestamp, String value, SystemTrace systemTraceTemplate) {
        SystemTrace systemTrace = SystemTrace.newBuilder()
                .setTimestamp(timestamp)
                .setHostname(systemTraceTemplate.getHostname())
                .setMeasure(systemTraceTemplate.getMeasure())
                .setMetric(systemTraceTemplate.getMetric())
                .setUnit(systemTraceTemplate.getUnit())
                .setValue(value)
                .build();
        ProducerRecord<String, SystemTrace> record = new ProducerRecord<>(this.topic, systemTrace);
        this.producer.send(record);
    }

}
