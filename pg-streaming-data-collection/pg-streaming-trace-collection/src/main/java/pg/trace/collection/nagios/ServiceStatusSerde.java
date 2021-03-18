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

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class ServiceStatusSerde implements Serde<ServiceStatusDTO> {

    private static ServiceStatusSerde instance;
    private ServiceStatusSerializer serializer;
    private ServiceStatusDeserializer deserializer;

    public ServiceStatusSerde() {
        this.serializer = new ServiceStatusSerializer();
        this.deserializer = new ServiceStatusDeserializer();
    }

    public static synchronized ServiceStatusSerde getInstance() {
        if (ServiceStatusSerde.instance == null) {
            ServiceStatusSerde.instance = new ServiceStatusSerde();
        }
        return ServiceStatusSerde.instance;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public void close() {
    }

    @Override
    public Serializer<ServiceStatusDTO> serializer() {
        return serializer;
    }

    @Override
    public Deserializer<ServiceStatusDTO> deserializer() {
        return deserializer;
    }
}
