# Feature extraction from system traces

<sub>*Please have a look at the parent [README](../README.md) first.*</sub>

A [Kafka Streams](https://kafka.apache.org/documentation/streams/) application. It reads
[system traces](../pg-streaming-schema/src/main/avro/pg/trace/model/system-trace.avsc) from an input Kafka topic and
calculates [features](../pg-streaming-schema/src/main/avro/pg/streaming/schema/feature.avsc) (mean, sd, slope, ...). We
calculate features per window. Windows advance by some hopping interval
(and may overlap as seen in the diagram of the parent README).

## Run feature extraction

Please have a look at the parent [README](../README.md) that explains building and running the extraction with
[Gradle](https://gradle.org/) and [Docker Compose](https://docs.docker.com/compose/).
This project will be built as part the parent a multi-project.

Have a look at the [implementation](./src/main/java/pg/streaming/feature/extraction/FeatureExtractionKafkaStreams.java)
and [test](./src/test/java/pg/streaming/feature/extraction/FeatureExtractionKafkaStreamsTest.java) of the application.