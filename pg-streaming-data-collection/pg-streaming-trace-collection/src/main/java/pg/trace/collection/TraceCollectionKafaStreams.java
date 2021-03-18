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
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.trace.model.PgModelSerde;
import pg.trace.model.SystemTrace;
import pg.trace.collection.nagios.NagiosToSystemTraceMapper;
import pg.trace.collection.nagios.ServiceStatusSerde;
import pg.trace.collection.telegraf.TelegrafToSystemTraceMapper;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TraceCollectionKafaStreams {

    private static final Logger log = LoggerFactory.getLogger(TraceCollectionKafaStreams.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final KafkaConfiguration config = new KafkaConfiguration();
        final Properties kafkaProperties = buildKafkaProperties(config);
        final Topology topology = buildTopology(config);
        TraceCollectionKafaStreams.log.info("Topology built. " + topology.describe().toString());

        createTopicsIfNotExist(config);

        final KafkaStreams streams = new KafkaStreams(topology, kafkaProperties);
        streams.cleanUp();
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    protected static Properties buildKafkaProperties(KafkaConfiguration config) {
        Properties streamprops = new Properties();
        streamprops.put(StreamsConfig.APPLICATION_ID_CONFIG, config.getGroupId());
        streamprops.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        return streamprops;
    }

    protected static Topology buildTopology(KafkaConfiguration config) {
        switch (config.getCollectionSource()) {
            case NAGIOS:
                return buildNagiosTopology(config);
            case TELEGRAF:
                return buildTelegrafTopology(config);
            default:
                return new Topology();
        }
    }

    protected static Topology buildNagiosTopology(KafkaConfiguration config) {
        final Serde<String> stringSerde = Serdes.String();
        final ServiceStatusSerde serviceStatusSerde = ServiceStatusSerde.getInstance();
        final SpecificAvroSerde<SystemTrace> systemTraceSerde = PgModelSerde.getSpecificAvroSerde(config.getSchemaRegistryUrl());

        final StreamsBuilder builder = new StreamsBuilder();
        builder
                .stream(config.getInputTopic(), Consumed.with(stringSerde, serviceStatusSerde))
                .flatMap(new NagiosToSystemTraceMapper())
                .to(config.getOutputTopic(), Produced.with(stringSerde, systemTraceSerde));

        return builder.build();
    }

    protected static Topology buildTelegrafTopology(KafkaConfiguration config) {
        final Serde<String> stringSerde = Serdes.String();
        final SpecificAvroSerde<SystemTrace> systemTraceSerde = PgModelSerde.getSpecificAvroSerde(config.getSchemaRegistryUrl());

        final StreamsBuilder builder = new StreamsBuilder();
        builder
                .stream(config.getInputTopic(), Consumed.with(stringSerde, stringSerde))
                .flatMap(new TelegrafToSystemTraceMapper())
                .to(config.getOutputTopic(), Produced.with(stringSerde, systemTraceSerde));

        return builder.build();
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
