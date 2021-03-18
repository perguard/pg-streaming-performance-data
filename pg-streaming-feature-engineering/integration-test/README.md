# Integration test for feature engineering

<sub>*Please have a look at the parent [README](../README.md) first.*</sub>

This repository contains an integration test for the three projects
[pg-streaming-feature-extraction](../pg-streaming-feature-extraction),
[pg-streaming-target-extraction](../pg-streaming-target-extraction), and
[pg-streaming-labeling](../pg-streaming-labeling).

It produces [system traces](../pg-streaming-feature-extraction) and
[software traces](../pg-streaming-target-extraction) ([Zipkin](https://zipkin.io/)) and consumes
[labeled data](../pg-streaming-labeling).

### Run integration test


The easiest way to run the project is to use [Gradle](https://gradle.org/) and
[Docker Compose](https://docs.docker.com/compose/).

Please make sure to have the following technologies installed:

* Java / JDK (e.g., [AdoptOpenJDK](https://adoptopenjdk.net/))
* [Gradle](https://gradle.org/)
* [Docker](https://www.docker.com/) (>= 3.2 to support *profiles*)

We use a [Docker Gradle plugin](https://plugins.gradle.org/plugin/com.bmuschko.docker-java-application) and
[Docker Compose Gradle pugin](https://plugins.gradle.org/plugin/com.avast.gradle.docker-compose) to start
[Zookeeper](https://zookeeper.apache.org/),
[Kafka](https://kafka.apache.org/),
[Schema Registry](https://docs.confluent.io/platform/current/schema-registry/installation/index.html), and all our Kafka
Streams applications. All images, including images for our applications, are automatically built as part of the
integration test.

To start the test, simply run:

```
gradle test -i
```

Docker-compose will be automatically be started and stopped.