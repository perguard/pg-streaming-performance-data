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

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.codec.SpanBytesEncoder;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

public class SoftwareTraceProducer implements Runnable {

    private final Logger log = LoggerFactory.getLogger(SoftwareTraceProducer.class);
    private final Producer<String, Span[]> producer;
    private final String topic;
    private final Endpoint hostFoo;
    private final Endpoint hostBar;


    public SoftwareTraceProducer(KafkaConfiguration config, String hostFoo, String hostBar) {
        final Properties kafkaProperties = new Properties();
        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        kafkaProperties.put(ProducerConfig.CLIENT_ID_CONFIG, config.getGroupId() + "-client");
        this.topic = config.getInputSoftwareTraceTopic();
        this.producer = new KafkaProducer<>(kafkaProperties, new StringSerializer(), new SpanArraySerializer());
        this.hostFoo = Endpoint.newBuilder().ip(hostFoo).build();
        this.hostBar = Endpoint.newBuilder().ip(hostBar).build();
    }

    @Override
    public void run() {
        log.info("Producing software traces");
        this.produceSoftwareTraceRecords();
    }

    private void produceSoftwareTraceRecords() {
        Long timestamp = Instant.now().toEpochMilli();
        this.produceSoftwareTraceRecord(timestamp, hostFoo, 500);
        this.produceSoftwareTraceRecord(timestamp, hostBar, 1500);
    }

    private void produceSoftwareTraceRecord(long timestamp, Endpoint host, long duration) {
        Span[] spans = new Span[]{Span.newBuilder()
                .traceId("fb51b34e757fbd79")
                .id("525eee0c3b45fc82")
                .timestamp(timestamp)
                .remoteEndpoint(host)
                .duration(duration)
                .build()};
        ProducerRecord<String, Span[]> record = new ProducerRecord<>(this.topic, spans);
        this.producer.send(record);
    }

    private class SpanArraySerializer implements Serializer<Span[]> {

        public SpanArraySerializer() {
        }

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
        }

        @Override
        public byte[] serialize(String topic, Span[] data) {
            return SpanBytesEncoder.JSON_V2.encodeList(Arrays.asList(data));
        }

        @Override
        public void close() {
        }

    }

}
