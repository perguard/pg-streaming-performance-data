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

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import pg.trace.collection.nagios.ServiceStatusDTO;
import pg.trace.collection.nagios.ServiceStatusSerializer;

/**
 * This is a standalone service for manual, interactive testing.
 * It sends a static test message (system trace) to the transformation service
 * (that is running on the Kafka cluster specified in config.properties).
 */
public class NagiosTestDataProducer {

    private final Logger log = LoggerFactory.getLogger(NagiosTestDataProducer.class);
    private KafkaConfiguration config;
    private Properties props;

    public NagiosTestDataProducer() {
        config = new KafkaConfiguration();
        props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
    }

    public void sendServiceStatus() {
        Producer<String, ServiceStatusDTO> producer = new KafkaProducer<>(props, new StringSerializer(), new ServiceStatusSerializer());

        ServiceStatusDTO status = NagiosTraceCollectionTest.getSampleServiceStatusDTO();

        // this fits the test baselines better
        status.setHostName("Cassandra");

        // putting the current time into the System Trace makes it easier to identify
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        status.setStatusUpdateTime(dateFormat.format(new Date()));

        ProducerRecord<String, ServiceStatusDTO> record = new ProducerRecord<>(config.getInputTopic(), status);

        log.info("Sending record. Current UNIX time * 1000 is " + System.currentTimeMillis());
        producer.send(record);
        producer.close();
    }

    public static void main(String[] args) {
        NagiosTestDataProducer producer = new NagiosTestDataProducer();
        producer.sendServiceStatus();

    }

}
