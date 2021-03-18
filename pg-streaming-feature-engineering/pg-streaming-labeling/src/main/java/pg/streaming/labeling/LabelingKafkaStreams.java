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
import pg.streaming.schema.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class LabelingKafkaStreams {

    private static final Logger log = LoggerFactory.getLogger(LabelingKafkaStreams.class);

    public static void main(final String[] args) throws ExecutionException, InterruptedException {
        final LabelingKafkaStreams app = new LabelingKafkaStreams();
        final KafkaConfiguration config = new KafkaConfiguration();
        final Properties kafkaProperties = app.buildKafkaProperties(config, TestUtils.tempDirectory().getAbsolutePath());
        final Topology topology = app.buildTopology(config);

        createTopicsIfNotExist(config);

        final KafkaStreams streams = new KafkaStreams(topology, kafkaProperties);
        streams.cleanUp();
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    protected Properties buildKafkaProperties(KafkaConfiguration config, final String stateDir) {
        final Properties kafkaProperties = new Properties();
        kafkaProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, config.getGroupId());
        kafkaProperties.put(StreamsConfig.CLIENT_ID_CONFIG, config.getGroupId() + "-client");
        kafkaProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        kafkaProperties.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);
        // kafkaProperties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        kafkaProperties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 30000);
        return kafkaProperties;
    }

    protected Topology buildTopology(KafkaConfiguration config) {
        final long windowSize = Duration.ofSeconds(config.getWindowDurationInSeconds()).toMillis();

        final Serde<Feature> featureSerde = PgStreamingSerde.getSpecificAvroSerde(config.getSchemaRegistryUrl());
        final Serde<LabeledFeature> labeledFeatureSerde = PgStreamingSerde.getSpecificAvroSerde(config.getSchemaRegistryUrl());
        final Serde<FullObservation> fullObservationSerde = PgStreamingSerde.getSpecificAvroSerde(config.getSchemaRegistryUrl());
        final Serde<FeatureKey> featureKeySerde = PgStreamingSerde.getSpecificAvroSerde(config.getSchemaRegistryUrl());
        final WindowedSerdes.TimeWindowedSerde<FeatureKey> windowedFeatureKeySerde = new WindowedSerdes.TimeWindowedSerde<>(featureKeySerde, windowSize);
        final Serde<Windowed<String>> windowedStringSerde = WindowedSerdes.timeWindowedSerdeFrom(String.class, windowSize);

        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<Windowed<FeatureKey>, Feature> featureStream = builder
                .stream(config.getInputFeatureTopic(), Consumed.with(windowedFeatureKeySerde, featureSerde));

        final KStream<Windowed<String>, Integer> targetStream = builder
                .stream(config.getInputTargetTopic(), Consumed.with(windowedStringSerde, Serdes.Integer()));

        final ObservationJoiner observationJoiner = new ObservationJoiner();
        featureStream
                .map(this::movePartsOfKeyToValue)
                .join(
                        targetStream,
                        observationJoiner,
                        JoinWindows.of(Duration.ofSeconds(config.getHoppingDurationInSeconds() / 4)),
                        StreamJoined.with(windowedStringSerde, fullObservationSerde, Serdes.Integer())
                )
                .map(this::movePartsOfValueToKey)
                .to(config.getOutputTopic(), Produced.with(windowedFeatureKeySerde, labeledFeatureSerde));

        return builder.build();
    }

    private KeyValue<Windowed<String>, FullObservation> movePartsOfKeyToValue(Windowed<FeatureKey> windowed, Feature feature) {
        Windowed<String> windowedHostname = new Windowed<>(windowed.key().getHostname(), windowed.window());
        LabeledFeature labeledFeature = LabeledFeature.newBuilder()
                .setKurtosis(feature.getKurtosis())
                .setMaximum(feature.getMaximum())
                .setMean(feature.getMean())
                .setMedian(feature.getMedian())
                .setMinimum(feature.getMinimum())
                .setPearsonsCorrelation(feature.getPearsonsCorrelation())
                .setPercentile25(feature.getPercentile25())
                .setPercentile75(feature.getPercentile75())
                .setSkewness(feature.getSkewness())
                .setSlope(feature.getSlope())
                .setStandardDeviation(feature.getStandardDeviation())
                .build();
        FullObservation fullObservation = FullObservation.newBuilder()
                .setFeatureKey(windowed.key())
                .setLabeledFeature(labeledFeature)
                .build();
        return KeyValue.pair(windowedHostname, fullObservation);
    }

    private KeyValue<Windowed<FeatureKey>, LabeledFeature> movePartsOfValueToKey(Windowed<String> windowed, FullObservation fullObservation) {
        Windowed<FeatureKey> newWindowed = new Windowed<>(fullObservation.getFeatureKey(), windowed.window());
        return KeyValue.pair(newWindowed, fullObservation.getLabeledFeature());
    }

    private class ObservationJoiner implements ValueJoiner<FullObservation, Integer, FullObservation> {
        @Override
        public FullObservation apply(FullObservation fullObservation, Integer label) {
            fullObservation.getLabeledFeature().setLabel(label);
            return fullObservation;
        }
    }

    private static void createTopicsIfNotExist(KafkaConfiguration config) throws InterruptedException, ExecutionException {
        NewTopic inputFeatureTopic = new NewTopic(config.getInputFeatureTopic(), config.getInputFeatureTopicPartitions(), config.getInputFeatureTopicReplicationFactor());
        NewTopic inputTargetTopic = new NewTopic(config.getInputTargetTopic(), config.getInputTargetTopicPartitions(), config.getInputTargetTopicReplicationFactor());
        NewTopic outputTopic = new NewTopic(config.getOutputTopic(), config.getOutputTopicPartitions(), config.getOutputTopicReplicationFactor());
        List<NewTopic> topics = new ArrayList<>(Arrays.asList(inputFeatureTopic, inputTargetTopic, outputTopic));

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