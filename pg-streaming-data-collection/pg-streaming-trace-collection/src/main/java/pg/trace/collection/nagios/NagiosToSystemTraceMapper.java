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

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.trace.model.SystemTrace;
import pg.trace.model.TraceMeasure;
import pg.trace.model.TraceMetric;
import pg.trace.model.TraceUnit;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class NagiosToSystemTraceMapper implements KeyValueMapper<String, ServiceStatusDTO, Iterable<KeyValue<String, SystemTrace>>> {

    private final Logger log = LoggerFactory.getLogger(NagiosToSystemTraceMapper.class);

    @Override
    public Iterable<KeyValue<String, SystemTrace>> apply(String key, ServiceStatusDTO value) {
        List<KeyValue<String, SystemTrace>> result = new ArrayList<>();
        if (value != null) {
            SystemTrace systemTrace = new SystemTrace();
            systemTrace.setHostname(value.getHostName());
            systemTrace.setMetric(extractMetric(value));
            systemTrace.setTimestamp(parseTimestamp(value));
            systemTrace.setUnit(extractUnit(value));
            systemTrace.setMeasure(extractMeasure(value));
            systemTrace.setValue(value.getPerformanceData());
            result.add(new KeyValue<>("nagios", systemTrace));
        }
        return result;
    }

    private long parseTimestamp(ServiceStatusDTO status) {
        // times on service status reports are assumed to be in UTC if no timezone is included in the report
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));
        try {
            ZonedDateTime zdtStatusUpdateTime = ZonedDateTime.parse(status.getStatusUpdateTime(), formatter);
            return zdtStatusUpdateTime.toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            log.warn("Could not parse date for service status " + e.toString() + ", assigning current time instead.");
        }
        return System.currentTimeMillis(); // this returns UTC time, just as we need
    }

    private TraceUnit extractUnit(ServiceStatusDTO status) {
        try {
            if (status.getDisplayName().toUpperCase().equals("MEMORY USAGE")) {
                return TraceUnit.PERCENT;
            }
        } catch (NullPointerException e) {
            // do nothing
        }
        return TraceUnit.UNKNOWN;
    }

    private TraceMetric extractMetric(ServiceStatusDTO status) {
        try {
            if (status.getDisplayName().toUpperCase().equals("MEMORY USAGE")) {
                return TraceMetric.MEMORY;
            }
        } catch (NullPointerException e) {
            // do nothing
        }
        return TraceMetric.UNKNOWN;
    }

    private TraceMeasure extractMeasure(ServiceStatusDTO status) {
        try {
            if (status.getDisplayName().toUpperCase().equals("MEMORY USAGE")) {
                return TraceMeasure.UTILIZATION;
            }
        } catch (NullPointerException e) {
            // do nothing
        }
        return TraceMeasure.UNKNOWN;
    }
}
