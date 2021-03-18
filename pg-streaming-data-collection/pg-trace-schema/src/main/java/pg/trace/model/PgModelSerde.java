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

package pg.trace.model;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.specific.SpecificRecord;

import java.util.HashMap;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

public class PgModelSerde {

    public static <T extends SpecificRecord> SpecificAvroSerde<T> getSpecificAvroSerde(final String schemaRegistryUrl) {
        final SpecificAvroSerde<T> specificAvroSerde = new SpecificAvroSerde<>();
        final HashMap<String, String> serdeConfig = new HashMap<>();
        serdeConfig.put(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        specificAvroSerde.configure(serdeConfig, false);
        return specificAvroSerde;
    }

}
