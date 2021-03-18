# Schema for data models of features and labeled data 

<sub>*Please have a look at the parent [README](../README.md) first.*</sub>

This project is a Java library and contains the [Apache Avro](https://avro.apache.org/) schema files for all other 
stream apps as well as a helper method to get Serializers and Deserializers (Serde).

Java code is automatically generated into the ```build``` folder and declared as a source set so generated Java classes
can be used by other projects implementing this project.

## Build the library

### Required technologies

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

Please have a look at the sibling projects.

For instance, a Serde (serializer and deserializer) can be initialized as follows:

```java
Serde<FeatureKey> featureKeySerde=
        PgStreamingSerde.getSpecificAvroSerde(schemaRegistryUrl);
```