# Schema for the domain model of performance traces

<sub>*Please have a look at the parent [README](../README.md) first.*</sub>

This project is a Java library and contains the [Apache Avro](https://avro.apache.org/) schema files for our
[domain model](./src/main/avro/pg/trace/model/system-trace.avsc) as well as a helper method to get Serializers and
Deserializers (Serde).

Java code is automatically generated into the ```build``` folder and declared as a source set so generated Java classes
can be used by other projects implementing this project.

## Build the library

We use [Gradle](https://gradle.org/) to build the library.

Please make sure to have the following technologies installed:

* Java / JDK (e.g., [AdoptOpenJDK](https://adoptopenjdk.net/))
* [Gradle](https://gradle.org/)

Simply build the entire multi-project from the parent directory or just the library by running:

```
gradle build
```

Based on the Avro schema files in `src/main/avro`, Java sources are generated in `build/generated-main-avro-java` and
automatically marked as source set.

This library is also built as part of the parent multi-project.

## How to use the library

Please have a look at the sibling project.

For instance, a Serde (serializer and deserializer) can be initialized as follows:

```java
Serde<SystemTrace> systemTraceSerde=
        PgModelSerde.getSpecificAvroSerde(schemaRegistryUrl);
```
