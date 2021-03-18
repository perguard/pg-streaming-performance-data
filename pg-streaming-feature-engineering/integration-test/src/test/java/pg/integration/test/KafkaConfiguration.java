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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class KafkaConfiguration {

    private final Logger log = LoggerFactory.getLogger(KafkaConfiguration.class);
    private String bootstrapServers;
    private String groupId;
    private String inputSoftwareTraceTopic;
    private String inputSystemTraceTopic;
    private String outputTopic;
    private int windowDurationInSeconds = -1;
    private int hoppingDurationInSeconds = -1;
    private String schemaRegistryUrl;

    public KafkaConfiguration() {
        initConfigFromFile();
        checkConfig();
    }

    private void initConfigFromFile() {
        try (InputStream input = KafkaConfiguration.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            this.bootstrapServers = prop.getProperty("kafka.bootstrap-servers");
            this.groupId = prop.getProperty("kafka.group-id");
            this.inputSoftwareTraceTopic = prop.getProperty("kafka.input-software-trace-topic");
            this.inputSystemTraceTopic = prop.getProperty("kafka.input-system-trace-topic");
            this.outputTopic = prop.getProperty("kafka.output-topic");
            this.windowDurationInSeconds = Integer.parseInt(prop.getProperty("app.window-duration-in-seconds"));
            this.hoppingDurationInSeconds = Integer.parseInt(prop.getProperty("app.hopping-duration-in-seconds"));
            this.schemaRegistryUrl = prop.getProperty("schema-registry.url");
        } catch (Exception io) {
            log.info("Could not load complete configuration from file");
        }
    }

    private void checkConfig() {
        log.info("Using the following configuration " + this.toString());
        if (this.bootstrapServers == null ||
                this.groupId == null ||
                this.inputSoftwareTraceTopic == null ||
                this.inputSystemTraceTopic == null ||
                this.outputTopic == null ||
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

    public String getInputSoftwareTraceTopic() {
        return inputSoftwareTraceTopic;
    }

    public String getInputSystemTraceTopic() {
        return inputSystemTraceTopic;
    }

    public String getOutputTopic() {
        return outputTopic;
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
                ", inputSoftwareTraceTopic='" + inputSoftwareTraceTopic + '\'' +
                ", inputSystemTraceTopic='" + inputSystemTraceTopic + '\'' +
                ", outputTopic='" + outputTopic + '\'' +
                ", windowDurationInSeconds=" + windowDurationInSeconds +
                ", hoppingDurationInSeconds=" + hoppingDurationInSeconds +
                ", schemaRegistryUrl='" + schemaRegistryUrl + '\'' +
                '}';
    }
}