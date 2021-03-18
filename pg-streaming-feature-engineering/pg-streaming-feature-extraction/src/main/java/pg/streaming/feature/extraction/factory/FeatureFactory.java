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

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.streaming.schema.Feature;

import java.util.List;
import java.util.stream.IntStream;

public class FeatureFactory {

    private static Logger log = LoggerFactory.getLogger(FeatureFactory.class);

    public static Feature createFeature(List<Double> valueList) {
        double[] valueArray = valueList.stream().mapToDouble(d -> d).toArray();
        return createFeature(valueArray);
    }

    public static Feature createFeature(double[] valueArray) {
        DescriptiveStatistics ds = new DescriptiveStatistics(valueArray);
        return Feature.newBuilder()
                .setStandardDeviation(0.0)
                .setMinimum(ds.getMin())
                .setMaximum(ds.getMax())
                .setMean(ds.getMean())
                .setStandardDeviation(ds.getStandardDeviation())
                .setPercentile25(ds.getPercentile(25))
                .setMedian(ds.getPercentile(50))
                .setPercentile75(ds.getPercentile(75))
                .setSkewness(ds.getSkewness())
                .setKurtosis(ds.getKurtosis())
                .setSlope(getSlope(valueArray))
                .setPearsonsCorrelation(getPearsonsCorrelation(valueArray))
                .build();
    }

    private static double getSlope(double[] values) {
        double[][] valueSeries = new double[values.length][2];
        for (int i = 0; i < values.length; i++) {
            valueSeries[i][0] = i;
            valueSeries[i][1] = values[i];
        }
        SimpleRegression sr = new SimpleRegression();
        sr.addData(valueSeries);
        return sr.getSlope();
    }

    private static double getPearsonsCorrelation(double[] values) {
        double[] range = IntStream.rangeClosed(0, values.length - 1).asDoubleStream().toArray();
        PearsonsCorrelation pc = new PearsonsCorrelation();
        try {
            return pc.correlation(range, values);
        } catch (MathIllegalArgumentException e) {
            log.debug("Could not calculate pearsons correlation", e);
        }
        return 0.0;
    }

}
