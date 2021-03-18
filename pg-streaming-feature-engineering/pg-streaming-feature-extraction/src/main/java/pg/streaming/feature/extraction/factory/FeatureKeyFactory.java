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

package pg.streaming.feature.extraction.factory;

import pg.streaming.schema.FeatureKey;
import pg.trace.model.SystemTrace;
import pg.trace.model.TraceMeasure;
import pg.trace.model.TraceMetric;
import pg.trace.model.TraceUnit;

public class FeatureKeyFactory {

    public static FeatureKey createFeatureKey(SystemTrace systemTrace) {
        return FeatureKey.newBuilder()
                .setHostname(systemTrace.getHostname())
                .setMetric(systemTrace.getMetric())
                .setMeasure(systemTrace.getMeasure())
                .setUnit(systemTrace.getUnit())
                .build();
    }

    public static FeatureKey createFeatureKey(String hostname, TraceMetric metric, TraceMeasure measure, TraceUnit unit) {
        return FeatureKey.newBuilder()
                .setHostname(hostname)
                .setMetric(metric)
                .setMeasure(measure)
                .setUnit(unit)
                .build();
    }

}
