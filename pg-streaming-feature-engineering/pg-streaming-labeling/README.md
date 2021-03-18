# Labeling features with targets

<sub>*Please have a look at the parent [README](../README.md) first.*</sub>

A [Kafka Streams](https://kafka.apache.org/documentation/streams/) application. It reads hopping windows of
[features](../pg-streaming-schema/src/main/avro/pg/streaming/schema/feature.avsc) of one Kafka topic and hopping windows
of [targets](../pg-streaming-target-extraction/README.md) from another Kafka topic, joins them, and sends the
output to another Kafka topic.

```
// input: feature window
key: <t0-t1; hostA; cpu; utilization>, value: <0.5; ...>
key: <t0-t1; hostA; net; throughput> , value: <0.7; ...>
key: <t0-t1; hostB; cpu; utilization>, value: <0.9; ...>

// input: target window
key: <t0-t1; hostA>, value: <0>
key: <t0-t1; hostB>, value: <1>

// output: labeled data
key: <t0-t1; hostA; cpu; utilization>, value: <0.5; ...; 0>
key: <t0-t1; hostA; net; throughput> , value: <0.7; ...; 0>
key: <t0-t1; hostB; cpu; utilization>, value: <0.9; ...; 1>
```

## Run labeling

Please have a look at the parent [README](../README.md) that explains building and running the extraction with
[Gradle](https://gradle.org/) and [Docker Compose](https://docs.docker.com/compose/). This project will be built as part
the parent a multi-project.

Have a look at the [implementation](./src/main/java/pg/streaming/labeling/LabelingKafkaStreams.java)
and [test](./src/test/java/pg/streaming/labeling/LabelingKafkaStreamsTest.java) of the application.