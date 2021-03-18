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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class KafkaConfiguration {

    private final Logger log = LoggerFactory.getLogger(KafkaConfiguration.class);
    private String bootstrapServers;
    private String groupId;
    private String inputFeatureTopic;
    private int inputFeatureTopicPartitions = 1;
    private short inputFeatureTopicReplicationFactor = 1;
    private String inputTargetTopic;
    private int inputTargetTopicPartitions = 1;
    private short inputTargetTopicReplicationFactor = 1;
    private String outputTopic;
    private int outputTopicPartitions = 1;
    private short outputTopicReplicationFactor = 1;
    private int windowDurationInSeconds = -1;
    private int hoppingDurationInSeconds = -1;
    private String schemaRegistryUrl;

    public KafkaConfiguration() {
        initConfigFromFile();
        initConfigFromEnvironmentVariables();
        checkConfig();
    }

    public KafkaConfiguration(String bootstrapServers, String groupId, String inputFeatureTopic, String inputTargetTopic,
                              String outputTopic, int windowDurationInSeconds, int hoppingDurationInSeconds,
                              String schemaRegistryUrl) {
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
        this.inputFeatureTopic = inputFeatureTopic;
        this.inputTargetTopic = inputTargetTopic;
        this.outputTopic = outputTopic;
        this.windowDurationInSeconds = windowDurationInSeconds;
        this.hoppingDurationInSeconds = hoppingDurationInSeconds;
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    private void initConfigFromFile() {
        try (InputStream input = LabelingKafkaStreams.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            this.bootstrapServers = prop.getProperty("kafka.bootstrap-servers");
            this.groupId = prop.getProperty("kafka.group-id");
            this.inputFeatureTopic = prop.getProperty("kafka.input-feature-topic");
            this.inputFeatureTopicPartitions = Integer.parseInt(prop.getProperty("kafka.input-feature-topic-partitions"));
            this.inputFeatureTopicReplicationFactor = Short.parseShort(prop.getProperty("kafka.input-feature-topic-replication-factor"));
            this.inputTargetTopic = prop.getProperty("kafka.input-target-topic");
            this.inputTargetTopicPartitions = Integer.parseInt(prop.getProperty("kafka.input-target-topic-partitions"));
            this.inputTargetTopicReplicationFactor = Short.parseShort(prop.getProperty("kafka.input-target-topic-replication-factor"));
            this.outputTopic = prop.getProperty("kafka.output-topic");
            this.outputTopicPartitions = Integer.parseInt(prop.getProperty("kafka.output-topic-partitions"));
            this.outputTopicReplicationFactor = Short.parseShort(prop.getProperty("kafka.output-topic-replication-factor"));
            this.windowDurationInSeconds = Integer.parseInt(prop.getProperty("app.window-duration-in-seconds"));
            this.hoppingDurationInSeconds = Integer.parseInt(prop.getProperty("app.hopping-duration-in-seconds"));
            this.schemaRegistryUrl = prop.getProperty("schema-registry.url");
        } catch (Exception io) {
            log.info("Could not load complete configuration from file");
        }
    }

    private void initConfigFromEnvironmentVariables() {
        if (System.getenv("kafka.bootstrap-servers") != null) {
            this.bootstrapServers = System.getenv("kafka.bootstrap-servers");
        }
        if (System.getenv("kafka.group-id") != null) {
            this.groupId = System.getenv("kafka.group-id");
        }
        if (System.getenv("kafka.input-feature-topic") != null) {
            this.inputFeatureTopic = System.getenv("kafka.input-feature-topic");
        }
        if (System.getenv("kafka.input-target-topic") != null) {
            this.inputTargetTopic = System.getenv("kafka.input-target-topic");
        }
        if (System.getenv("kafka.output-topic") != null) {
            this.outputTopic = System.getenv("kafka.output-topic");
        }
        if (System.getenv("schema-registry.url") != null) {
            this.schemaRegistryUrl = System.getenv("schema-registry.url");
        }
        try {
            if (System.getenv("app.window-duration-in-seconds") != null) {
                this.windowDurationInSeconds = Integer.parseInt(System.getenv("app.window-duration-in-seconds"));
            }
            if (System.getenv("app.hopping-duration-in-seconds") != null) {
                this.hoppingDurationInSeconds = Integer.parseInt(System.getenv("app.hopping-duration-in-seconds"));
            }
            if (System.getenv("kafka.input-topic-partitions") != null) {
                this.inputFeatureTopicPartitions = Integer.parseInt(System.getenv("kafka.input-feature-topic-partitions"));
            }
            if (System.getenv("kafka.input-topic-replication-factor") != null) {
                this.inputFeatureTopicReplicationFactor = Short.parseShort(System.getenv("kafka.input-feature-topic-replication-factor"));
            }
            if (System.getenv("kafka.input-topic-partitions") != null) {
                this.inputTargetTopicPartitions = Integer.parseInt(System.getenv("kafka.input-target-topic-partitions"));
            }
            if (System.getenv("kafka.input-topic-replication-factor") != null) {
                this.inputTargetTopicReplicationFactor = Short.parseShort(System.getenv("kafka.input-target-topic-replication-factor"));
            }
            if (System.getenv("kafka.output-topic-partitions") != null) {
                this.outputTopicPartitions = Integer.parseInt(System.getenv("kafka.output-topic-partitions"));
            }
            if (System.getenv("kafka.output-topic-replication-factor") != null) {
                this.outputTopicReplicationFactor = Short.parseShort(System.getenv("kafka.output-topic-replication-factor"));
            }
        } catch (NumberFormatException nfe) {
            log.info("Could not load parse integer", nfe);
        }
    }

    private void checkConfig() {
        log.info("Using the following configuration " + this.toString());
        if (this.bootstrapServers == null ||
                this.groupId == null ||
                this.inputFeatureTopic == null ||
                this.inputFeatureTopicPartitions < 0 ||
                this.inputFeatureTopicReplicationFactor < 0 ||
                this.inputTargetTopic == null ||
                this.inputTargetTopicPartitions < 0 ||
                this.inputTargetTopicReplicationFactor < 0 ||
                this.outputTopic == null ||
                this.outputTopicPartitions < 0 ||
                this.outputTopicReplicationFactor < 0 ||
                this.windowDurationInSeconds == -1 ||
                this.hoppingDurationInSeconds == -1 ||
                this.schemaRegistryUrl == null) {
            throw new RuntimeException("Configuration information is missing. Cannot continue.");
        }
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getInputFeatureTopic() {
        return inputFeatureTopic;
    }

    public int getInputFeatureTopicPartitions() {
        return inputFeatureTopicPartitions;
    }

    public short getInputFeatureTopicReplicationFactor() {
        return inputFeatureTopicReplicationFactor;
    }

    public String getInputTargetTopic() {
        return inputTargetTopic;
    }

    public int getInputTargetTopicPartitions() {
        return inputTargetTopicPartitions;
    }

    public short getInputTargetTopicReplicationFactor() {
        return inputTargetTopicReplicationFactor;
    }

    public String getOutputTopic() {
        return outputTopic;
    }

    public int getOutputTopicPartitions() {
        return outputTopicPartitions;
    }

    public short getOutputTopicReplicationFactor() {
        return outputTopicReplicationFactor;
    }

    public int getWindowDurationInSeconds() {
        return windowDurationInSeconds;
    }

    public int getHoppingDurationInSeconds() {
        return hoppingDurationInSeconds;
    }

    public String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }

    @Override
    public String toString() {
        return "KafkaConfiguration{" +
                "bootstrapServers='" + bootstrapServers + '\'' +
                ", groupId='" + groupId + '\'' +
                ", inputFeatureTopic='" + inputFeatureTopic + '\'' +
                ", inputFeatureTopicPartitions=" + inputFeatureTopicPartitions +
                ", inputFeatureTopicReplicationFactor=" + inputFeatureTopicReplicationFactor +
                ", inputTargetTopic='" + inputTargetTopic + '\'' +
                ", inputTargetTopicPartitions=" + inputTargetTopicPartitions +
                ", inputTargetTopicReplicationFactor=" + inputTargetTopicReplicationFactor +
                ", outputTopic='" + outputTopic + '\'' +
                ", outputTopicPartitions=" + outputTopicPartitions +
                ", outputTopicReplicationFactor=" + outputTopicReplicationFactor +
                ", windowDurationInSeconds=" + windowDurationInSeconds +
                ", hoppingDurationInSeconds=" + hoppingDurationInSeconds +
                ", schemaRegistryUrl='" + schemaRegistryUrl + '\'' +
                '}';
    }

}