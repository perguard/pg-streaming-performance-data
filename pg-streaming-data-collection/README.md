# Data collection of performance traces

This folder contains a multi-project build in Gradle for collecting system traces from different APM vendors.

The multi-project contains the following subprojects, each with an own README:

* [`pg-trace-schema`](./pg-trace-schema): a Java library that contains our domain model for a *system trace* using
  Apache Avro. It is used by all other apps.
* [`pg-streaming-trace-collection`](./pg-streaming-trace-collection): a Java
  / [Kafka Streams](https://kafka.apache.org/documentation/streams/) app that can be configured to either collect traces
  from [Nagios](https://www.nagios.com/products/nagios-xi/) or
  [Telegraf](https://www.influxdata.com/time-series-platform/telegraf/) and transform the data to our domain
  model/schema.

## Run data collection

The easiest way to run the project is to use [Gradle](https://gradle.org/) and
[Docker Compose](https://docs.docker.com/compose/).

Please make sure to have the following technologies installed:

* Java / JDK (e.g., [AdoptOpenJDK](https://adoptopenjdk.net/))
* [Gradle](https://gradle.org/)
* [Docker](https://www.docker.com/) (>= 3.2 to support *profiles*)

We use a [Docker Gradle plugin](https://plugins.gradle.org/plugin/com.bmuschko.docker-java-application) to build Docker
images.

Build the multi-project and docker images by running:

```
gradle build dockerBuildImage
```

To start all default containers, that is, [Zookeeper](https://zookeeper.apache.org/),
[Kafka](https://kafka.apache.org/),
[Schema Registry](https://docs.confluent.io/platform/current/schema-registry/installation/index.html)
and [`pg-streaming-trace-collection`](./pg-streaming-trace-collection), run the following command:

```
docker-compose up
```

Alternatively, use [profiles with Compose](https://docs.docker.com/compose/profiles/) to start additional containers:

* `docker-compose --profile telegraf up` starts
  [Telegraf](https://www.influxdata.com/time-series-platform/telegraf/)
  to generate test data (see [config](./pg-streaming-trace-collection/src/test/resources/telegraf.conf))

* `docker-compose --profile debug up` starts [Kafdrop](https://github.com/obsidiandynamics/kafdrop)

To stop and remove everything, we recommend using the following command to prevent future errors
with [Apache Kafka](https://kafka.apache.org/):

```
docker-compose rm -sfv
```
