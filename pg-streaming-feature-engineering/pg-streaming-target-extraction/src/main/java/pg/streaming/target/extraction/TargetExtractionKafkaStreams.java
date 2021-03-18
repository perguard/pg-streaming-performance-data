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

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.test.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.streaming.target.extraction.serdes.SpanArraySerde;
import zipkin2.Span;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


public class TargetExtractionKafkaStreams {

    private static final Logger log = LoggerFactory.getLogger(TargetExtractionKafkaStreams.class);

    public static void main(final String[] args) throws ExecutionException, InterruptedException {
        final KafkaConfiguration config = new KafkaConfiguration();
        final Properties kafkaProperties = buildKafkaProperties(config, TestUtils.tempDirectory().getAbsolutePath());
        final Topology topology = buildTopology(config);

        createTopicsIfNotExist(config);

        final KafkaStreams streams = new KafkaStreams(topology, kafkaProperties);
        streams.cleanUp();
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    protected static Properties buildKafkaProperties(KafkaConfiguration config, final String stateDir) {
        final Properties kafkaProperties = new Properties();
        kafkaProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, config.getGroupId());
        kafkaProperties.put(StreamsConfig.CLIENT_ID_CONFIG, config.getGroupId() + "-client");
        kafkaProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        kafkaProperties.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);
        // kafkaProperties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        kafkaProperties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 30000);
        return kafkaProperties;
    }

    protected static Topology buildTopology(KafkaConfiguration config) {
        int slowdownThreshold = config.getSlowdownThreshold();

        final Duration windowDuration = Duration.ofSeconds(config.getWindowDurationInSeconds());
        final Duration advanceDuration = Duration.ofSeconds(config.getHoppingDurationInSeconds());

        final Serde<String> stringSerde = Serdes.String();
        final Serde<Integer> intSerde = Serdes.Integer();
        final Serde<Span[]> spanArraySerde = SpanArraySerde.getInstance();
        final Serde<Windowed<String>> windowedHostSerde = WindowedSerdes.timeWindowedSerdeFrom(String.class, windowDuration.toMillis());

        final StreamsBuilder builder = new StreamsBuilder();

        builder
                .stream(config.getInputTopic(), Consumed.with(stringSerde, spanArraySerde))
                .map(TargetExtractionKafkaStreams::mapEndpointAndDuration)
                .groupByKey(Grouped.with(stringSerde, intSerde))
                .windowedBy(TimeWindows.of(windowDuration).advanceBy(advanceDuration))
                .aggregate(() -> 0, hasSlowdownInWindow(slowdownThreshold), Materialized.with(stringSerde, intSerde))
                .toStream()
                .to(config.getOutputTopic(), Produced.with(windowedHostSerde, intSerde));

        return builder.build();
    }

    private static KeyValue<String, Integer> mapEndpointAndDuration(String key, Span[] spans) {
        if (spans.length > 0) {
            Span span = spans[0];
            return new KeyValue<>(span.remoteEndpoint().ipv4(), span.duration().intValue());
        } else {
            log.debug("Processed empty span array");
            return null;
        }
    }

    private static Aggregator<String, Integer, Integer> hasSlowdownInWindow(int threshold) {
        return (ip, duration, isSlowdown) -> {
            if (isSlowdown == 1 || duration > threshold) {
                return 1;
            } else {
                return 0;
            }
        };
    }

    private static void createTopicsIfNotExist(KafkaConfiguration config) throws ExecutionException, InterruptedException {
        NewTopic inputTopic = new NewTopic(config.getInputTopic(), config.getInputTopicPartitions(), config.getInputTopicReplicationFactor());
        NewTopic outputTopic = new NewTopic(config.getOutputTopic(), config.getOutputTopicPartitions(), config.getOutputTopicReplicationFactor());
        List<NewTopic> topics = new ArrayList<>(Arrays.asList(inputTopic, outputTopic));

        Map<String, Object> conf = new HashMap<>();
        conf.put("bootstrap.servers", config.getBootstrapServers());
        AdminClient client = AdminClient.create(conf);
        ListTopicsResult listTopicsResult = client.listTopics();
        Collection<TopicListing> topicListings = listTopicsResult.listings().get();
        topics.forEach(newTopic -> {
            List<TopicListing> equalTopics = topicListings
                    .stream()
                    .filter(topicListing -> topicListing.name().equals(newTopic.name()))
                    .collect(Collectors.toList());
            if (equalTopics.size() == 0) {
                log.info("Creating topic " + newTopic.name());
                client.createTopics(Collections.singletonList(newTopic));
            } else {
                log.info("Topic " + newTopic.name() + " already exists");
            }
        });
        client.close();
    }

}