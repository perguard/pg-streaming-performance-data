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

import org.apache.kafka.streams.kstream.Window;
import org.apache.kafka.streams.kstream.Windowed;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.streaming.schema.FeatureKey;
import pg.streaming.schema.LabeledFeature;

import java.util.*;
import java.util.concurrent.*;

public class IntegrationTest {

    private final Logger log = LoggerFactory.getLogger(IntegrationTest.class);
    private KafkaConfiguration config = new KafkaConfiguration();
    private String hostFoo = "192.168.0.101";
    private String hostBar = "192.168.0.102";
    private int testWindows = 5;
    private ScheduledExecutorService producingExecutors;
    private ExecutorService consumingExecutors;
    private Future<Map<Windowed<FeatureKey>, LabeledFeature>> future;

    @Before
    public void setup() throws InterruptedException {
        producingExecutors = Executors.newScheduledThreadPool(2);
        consumingExecutors = Executors.newSingleThreadExecutor();
        Thread.sleep(10000); // sleep so all containers of docker-compose are really up and ready
    }

    @Test
    public void test() throws ExecutionException, InterruptedException {
        producingExecutors.scheduleAtFixedRate(new SoftwareTraceProducer(config, hostFoo, hostBar), 0, 1, TimeUnit.SECONDS);
        producingExecutors.scheduleAtFixedRate(new SystemTraceProducer(config, hostFoo, hostBar), 0, 1, TimeUnit.SECONDS);
        future = consumingExecutors.submit(new LabeledDataReceiver(config));

        this.waitAndShutdownExecutors(producingExecutors, consumingExecutors);

        Map<Windowed<FeatureKey>, LabeledFeature> labeledData = future.get();
        logLabeledData(labeledData);
        int hoppingWindowsPerWindow = config.getWindowDurationInSeconds() / config.getHoppingDurationInSeconds();
        int totalWindowsPerHostAndMetric = hoppingWindowsPerWindow * testWindows + 1 + 1; // one before and one afterwards
        int totalWindows = totalWindowsPerHostAndMetric * 4; // host: foo, bar; metric: CPU, MEM
        Assert.assertEquals(totalWindows, labeledData.size());
    }

    private void waitAndShutdownExecutors(ScheduledExecutorService producingExecutors, ExecutorService consumingExecutors) throws InterruptedException {
        int threadDuration = config.getWindowDurationInSeconds() * testWindows; // waiting total duration of 5 windows
        Thread.sleep(1000 * threadDuration);
        producingExecutors.shutdown();
        if (!producingExecutors.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
            producingExecutors.shutdownNow();
        }
        consumingExecutors.shutdown();
    }

    private boolean logLabeledData(Map<Windowed<FeatureKey>, LabeledFeature> labeledData) {
        Set<FeatureKey> keys = new HashSet<>();
        Set<Window> windows = new HashSet<>();
        labeledData.forEach((featureKeyWindowed, labeledFeature) -> {
            keys.add(featureKeyWindowed.key());
            windows.add(featureKeyWindowed.window());
        });

        List<Window> sortedWindows = new ArrayList<>(windows);
        sortedWindows.sort((o1, o2) -> (int) (o1.startTime().toEpochMilli() - o2.startTime().toEpochMilli()));

        keys.forEach(featureKey -> {
            sortedWindows.forEach(window -> {
                Windowed<FeatureKey> windowed = new Windowed<>(featureKey, window);
                log.info(window.startTime().toString() + " - " + window.endTime().toString() + " " +
                        featureKey.toString() + " " + labeledData.get(windowed).toString());
            });
        });
        return labeledData.size() > 0;
    }

}
