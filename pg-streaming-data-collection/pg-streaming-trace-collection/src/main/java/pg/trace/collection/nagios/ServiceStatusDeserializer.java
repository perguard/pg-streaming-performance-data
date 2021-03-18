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
package pg.trace.collection.nagios;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class ServiceStatusDeserializer implements Deserializer<ServiceStatusDTO> {

    private ObjectMapper mapper;
    private final Logger log = LoggerFactory.getLogger(ServiceStatusDeserializer.class);

    public ServiceStatusDeserializer() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public ServiceStatusDTO deserialize(String topic, byte[] data) {
        if (data == null) {
            log.warn("No data");
            return null;
        } else {
            try {
                return mapper.readValue(data, ServiceStatusDTO.class);
            } catch (IOException e) {
                log.error("Could not deserialize data", e);
            }
        }
        return null;
    }

    @Override
    public void close() {
    }
}


