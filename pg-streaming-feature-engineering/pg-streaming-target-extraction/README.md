# Target extraction from software traces

<sub>*Please have a look at the parent [README](../README.md) first.*</sub>

A [Kafka Streams](https://kafka.apache.org/documentation/streams/) application. 
It reads arrays of [Zipkin Spans](https://zipkin.io/zipkin/2.11.0/zipkin/zipkin2/Span.html) 
from an input Kafka topic and calculates slowdowns, i.e., whether response times exceed a specified threshold or not.
We calculate slowdowns per window. Windows advance by some hopping interval (and may overlap).
The resulting window stream consists of a key (includes the host IP) and a value (0 for no slowdown, 1 for a slowdown). 

## Run target extraction

Please have a look at the parent [README](../README.md) that explains building and running the extraction with
[Gradle](https://gradle.org/) and [Docker Compose](https://docs.docker.com/compose/).
This project will be built as part the parent a multi-project.

Have a look at the [implementation](./src/main/java/pg/streaming/target/extraction/TargetExtractionKafkaStreams.java)
and [test](./src/test/java/pg/streaming/target/extraction/TargetExtractionKafkaStreamsTest.java) of the application.