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

package pg.trace.collection.telegraf;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.trace.model.SystemTrace;
import pg.trace.model.TraceMeasure;
import pg.trace.model.TraceMetric;
import pg.trace.model.TraceUnit;

import java.util.ArrayList;
import java.util.List;

public class TelegrafToSystemTraceMapper implements KeyValueMapper<String, String, Iterable<KeyValue<String, SystemTrace>>> {

    private final Logger log = LoggerFactory.getLogger(TelegrafToSystemTraceMapper.class);

    @Override
    public Iterable<KeyValue<String, SystemTrace>> apply(String key, String value) {
        List<KeyValue<String, SystemTrace>> result = new ArrayList<>();
        if (value != null) {
            SystemTrace systemTrace = new SystemTrace();

            // InfluxDB line protocol: see https://docs.influxdata.com/influxdb/v2.0/reference/syntax/line-protocol/

            String[] elements = value.split("[ \n]");
            String measurement = elements[0].substring(0, elements[0].indexOf(","));

            java.util.Map<String, String> tags = java.util.Arrays.stream(elements[0].substring(measurement.length() + 1).split(","))
                    .map(s -> s.split("="))
                    .collect(java.util.stream.Collectors.toMap(
                            a -> a[0],  // key
                            a -> a[1]   // value
                    ));

            java.util.Map<String, String> fields = java.util.Arrays.stream(elements[1].split(","))
                    .map(s -> s.split("="))
                    .collect(java.util.stream.Collectors.toMap(
                            a -> a[0],  // key
                            a -> a[1]   // value
                    ));

            if (measurement.equals("cpu") && tags.get("cpu").equals("cpu-total")) {
                systemTrace.setMetric(TraceMetric.CPU);
                systemTrace.setMeasure(TraceMeasure.UTILIZATION);
                systemTrace.setUnit(TraceUnit.PERCENT);
                systemTrace.setValue(Float.toString(100 - Float.parseFloat(fields.get("usage_idle"))));
            } else if (measurement.equals("mem")) {
                systemTrace.setMetric(TraceMetric.MEMORY);
                systemTrace.setMeasure(TraceMeasure.UTILIZATION);
                systemTrace.setUnit(TraceUnit.PERCENT);
                systemTrace.setValue(fields.get("used_percent"));
            } else if (measurement.equals("diskio")) {
                systemTrace.setMetric(TraceMetric.DISK_READ);
                systemTrace.setMeasure(TraceMeasure.RESPONSE_TIME);
                systemTrace.setUnit(TraceUnit.MILLISECONDS);
                String rt = fields.get("read_time");
                systemTrace.setValue(rt.substring(0, rt.length() - 1));
            } else {
                log.debug("Skipping unknown Telegraf measurement: " + measurement);
                return result;
            }

            systemTrace.setHostname(tags.get("host"));
            systemTrace.setTimestamp(Long.parseUnsignedLong(elements[2]) / 1000000);
            result.add(new KeyValue<>("telegraf", systemTrace));
            log.debug("Transformed Telegraf " + measurement + " measurement for " + systemTrace.getHostname());
        }
        return result;
    }

}