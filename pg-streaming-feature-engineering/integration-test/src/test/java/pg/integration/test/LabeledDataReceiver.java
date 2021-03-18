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

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.WindowedSerdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.streaming.schema.FeatureKey;
import pg.streaming.schema.LabeledFeature;
import pg.streaming.schema.PgStreamingSerde;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

public class LabeledDataReceiver implements Callable<Map<Windowed<FeatureKey>, LabeledFeature>> {

    private final Logger log = LoggerFactory.getLogger(LabeledDataReceiver.class);
    private KafkaConfiguration config;

    public LabeledDataReceiver(KafkaConfiguration config) {
        this.config = config;
    }

    @Override
    public Map<Windowed<FeatureKey>, LabeledFeature> call() {
        Map<Windowed<FeatureKey>, LabeledFeature> labeledData = new HashMap<>();

        final Consumer<Windowed<FeatureKey>, LabeledFeature> consumer = createConsumer();

        final int noRecordsRetries = config.getWindowDurationInSeconds() * 2;
        int noRecordsCount = 0;

        while (true) {
            log.info("Poll records");
            final ConsumerRecords<Windowed<FeatureKey>, LabeledFeature> consumerRecords =
                    consumer.poll(Duration.ofSeconds(config.getHoppingDurationInSeconds()));

            if (consumerRecords.count() == 0) {
                log.info("No records");
                noRecordsCount++;
                if (noRecordsCount > noRecordsRetries) {
                    log.info("Break polling as there are no new records");
                    break;
                }
            }
            consumerRecords.forEach(record -> labeledData.put(record.key(), record.value()));
            consumer.commitAsync();
        }
        consumer.close();
        return labeledData;
    }

    private Consumer<Windowed<FeatureKey>, LabeledFeature> createConsumer() {
        final long windowSize = Duration.ofSeconds(config.getWindowDurationInSeconds()).toMillis();
        final Serde<FeatureKey> featureKeySerde = PgStreamingSerde.getSpecificAvroSerde(config.getSchemaRegistryUrl());
        final WindowedSerdes.TimeWindowedSerde<FeatureKey> windowedFeatureKeySerde = new WindowedSerdes.TimeWindowedSerde<>(featureKeySerde, windowSize);
        final Serde<LabeledFeature> labeledFeatureSerde = PgStreamingSerde.getSpecificAvroSerde(config.getSchemaRegistryUrl());

        Properties kafkaProperties = new Properties();
        kafkaProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        kafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, config.getGroupId());
        kafkaProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, config.getGroupId() + "-client");

        KafkaConsumer<Windowed<FeatureKey>, LabeledFeature> consumer =
                new KafkaConsumer(kafkaProperties, windowedFeatureKeySerde.deserializer(), labeledFeatureSerde.deserializer());
        consumer.subscribe(Collections.singletonList(config.getOutputTopic()));
        return consumer;
    }

}
