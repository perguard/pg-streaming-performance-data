# Trace collection

<sub>*Please have a look at the parent [README](../README.md) first.*</sub>

A [Kafka Streams](https://kafka.apache.org/documentation/streams/) application. It reads raw performance traces (i.e.,
[Nagios](https://www.nagios.com/products/nagios-xi/),
[Telegraf](https://www.influxdata.com/time-series-platform/telegraf/)) from a Kafka topic, transforms them into our
[domain model / schema](../pg-trace-schema), and sends the output to another Kafka topic.

## Run trace collection

Please have a look at the parent [README](../README.md) that explains building and running the collection with
[Gradle](https://gradle.org/) and [Docker Compose](https://docs.docker.com/compose/).
This project will be built as part the parent a multi-project.

Have a look at the [implementation](./src/main/java/pg/trace/collection/TraceCollectionKafaStreams.java)
and [test](./src/test/java/pg/trace/collection/TelegrafTraceCollectionTest.java) of the application.