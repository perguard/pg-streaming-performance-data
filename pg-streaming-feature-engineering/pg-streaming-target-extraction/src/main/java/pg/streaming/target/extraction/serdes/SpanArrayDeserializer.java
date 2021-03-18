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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin2.Span;
import zipkin2.codec.SpanBytesDecoder;

import java.util.Map;

/**
 * SpanArrayDeserializer implements Deserializer interface for converting bytes to a {@link Span} array.
 */
public class SpanArrayDeserializer implements Deserializer<Span[]> {

    private final ObjectMapper mapper;
    private final Logger log = LoggerFactory.getLogger(SpanArrayDeserializer.class);

    public SpanArrayDeserializer() {
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // do nothing
    }

    @Override
    public Span[] deserialize(String topic, byte[] data) {
        return SpanBytesDecoder.JSON_V2.decodeList(data).toArray(new Span[0]);
    }

    @Override
    public void close() {
        // do nothing
    }
}
