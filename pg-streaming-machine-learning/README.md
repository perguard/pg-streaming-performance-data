# Machine learning for streaming performance traces

This folder provides a very simple prototype demonstration to implement online machine learning for streaming data and
train and predict performance bottleneck events based
[feature engineered performance traces](../pg-streaming-feature-engineering). **Please have a look there first.**
For this purpose, we use [scikit-multiflow](https://scikit-multiflow.readthedocs.io/) and implement an
[Adaptive Random Forest Classifier](./pg_streaming_machine_learning/skmultiflow/adaptive_random_forest_classifier.py)
that is updated for each event in the data stream.

The incoming data stream is in [Apache Avro](https://avro.apache.org/) registered at a
[Schema Registry](https://docs.confluent.io/platform/current/schema-registry/installation/index.html). As
[Kafka Streams](https://kafka.apache.org/documentation/streams/) is not available in Python, we use
[Faust](https://faust.readthedocs.io/) to consume hopping windows of those performance traces. Data a deserialized using
[Dataclasses Avro Schema Generator](https://marcosschroh.github.io/dataclasses-avroschema/) and
[Python Schema Registry Client](https://marcosschroh.github.io/python-schema-registry-client/).

## Run machine learning

The easiest way to run the project is to use [Python3](https://www.python.org/) and/or
[Docker Compose](https://docs.docker.com/compose/).

To generate test data, have a look at the [integration test](../pg-streaming-feature-engineering/integration-test).

### Run using Docker Compose

Please make sure to have the following technologies installed:

* [Docker](https://www.docker.com/) (>= 3.2 to support *profiles*)

To build our app, run:

```
docker-compose build
```

To start all default containers, that is, [Zookeeper](https://zookeeper.apache.org/),
[Kafka](https://kafka.apache.org/),
[Schema Registry](https://docs.confluent.io/platform/current/schema-registry/installation/index.html)
and this project, run the following command:

```
docker-compose --profile ml --profile infrastructure up
```

To stop and remove everything, we recommend using the following command to prevent future errors
with [Apache Kafka](https://kafka.apache.org/):

```
docker-compose rm -sfv
```

### Run using Python

Please make sure to have the following technologies installed:

* [Python3](https://www.python.org/)
* either
    * [Docker](https://www.docker.com/) (>= 3.2 to support *profiles*)
* or
    * [Zookeeper](https://zookeeper.apache.org/)
    * [Kafka](https://kafka.apache.org/)
    * [Schema Registry](https://docs.confluent.io/platform/current/schema-registry/installation/index.html)

Start dependent instances of [Zookeeper](https://zookeeper.apache.org/), [Kafka](https://kafka.apache.org/),
[Schema Registry](https://docs.confluent.io/platform/current/schema-registry/installation/index.html) either natively or
using [Docker Compose](https://docs.docker.com/compose/):

```
docker-compose --profile infrastructure up
```

Install dependencies:

```
pip3 install -e .
```

Export settings:

```
export SIMPLE_SETTINGS=settings
```

Run using [Faust](https://faust.readthedocs.io/):

```
faust -A pg_streaming_machine_learning.app worker -l INFO
```

