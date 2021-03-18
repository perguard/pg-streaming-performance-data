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
package pg.streaming.target.extraction.serdes;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import zipkin2.Span;

import java.util.Map;

/**
 * We use Serializer and Deserializer (Serdes) to convert bytes of the record to a {@link Span} array.
 */
public class SpanArraySerde implements Serde<Span[]> {

    private static SpanArraySerde instance;
    private final SpanArraySerializer serializer;
    private final SpanArrayDeserializer deserializer;

    public SpanArraySerde() {
        this.serializer = new SpanArraySerializer();
        this.deserializer = new SpanArrayDeserializer();
    }

    public static synchronized SpanArraySerde getInstance() {
        if (SpanArraySerde.instance == null) {
            SpanArraySerde.instance = new SpanArraySerde();
        }
        return SpanArraySerde.instance;
    }

    /**
     * This class can configure the underlying serializer and deserializer.
     */
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    /**
     * This serde class will close the underlying serializer and deserializer.
     */
    @Override
    public void close() {

    }

    @Override
    public Serializer<Span[]> serializer() {
        return this.serializer;
    }

    @Override
    public Deserializer<Span[]> deserializer() {
        return this.deserializer;
    }
}
