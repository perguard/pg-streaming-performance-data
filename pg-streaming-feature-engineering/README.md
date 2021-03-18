# Feature engineering of performance traces

This folder contains a multi-project build in Gradle for feature engineering software and system traces to allow for our
machine learning on streaming data.

The multi-project contains the following subprojects, each with an own README:

* [`pg-streaming-schema`](./pg-streaming-schema): a Java library that contains our data models using Apache Avro. It is
  used by all other apps.
* [`pg-streaming-feature-extraction`](./pg-streaming-feature-extraction): a Java / Kafka Streams app to calculate
  features in hopping windows from a stream of system traces.
* [`pg-streaming-target-extraction`](./pg-streaming-target-extraction): a Java / Kafka Streams app to calculate targets
  in hopping windows from a stream of software traces (i.e., Zipkin).
* [`pg-streaming-labeling`](./pg-streaming-labeling): a Java / Kafka Streams app to join the feature and target stream
  so we have a stream of labeled data in hopping windows.
* [`integration-test`](./integration-test): a Java project that runs an integration test of all projects above. It
  automatically builds docker images and starts and stops docker-compose.

The feature engineering procedure is as follows:

![README](README.svg)

## Run feature engineering

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

Build the multi-project and execute the integration test by running:

```
gradle build
```

To start all default containers, run of the following command:

```
docker-compose up
```

Alternatively, use [profiles with Compose](https://docs.docker.com/compose/profiles/) to start additional containers:

* `docker-compose --profile debug up` starts [Kafdrop](https://github.com/obsidiandynamics/kafdrop)

To stop and remove everything, we recommend using the following command to prevent future errors
with [Apache Kafka](https://kafka.apache.org/):

```
docker-compose rm -sfv
```

To generate test data, have a look at the [integration test](./integration-test) or on our other project
[pg-streaming-data-collection](../pg-streaming-data-collection) in the root of this repository.
