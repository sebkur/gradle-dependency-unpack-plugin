#!/bin/bash

set -e
set -x

pushd .
cd test1
pwd
JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/ ./gradlew clean test createRuntime
./scripts/Test
JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/ ./gradlew depunpack
popd

pushd .
cd test2
pwd
JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/ ./gradlew clean test createRuntime
./scripts/Test
JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/ ./gradlew depunpack
popd

pushd .
cd test2
pwd
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/ ./gradlew clean test createRuntime
./scripts/Test
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/ ./gradlew depunpack
popd

pushd .
cd test3
pwd
JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/ ./gradlew clean test createRuntime
./scripts/Test
JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/ ./gradlew depunpack
popd

pushd .
cd test3
pwd
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/ ./gradlew clean test createRuntime
./scripts/Test
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/ ./gradlew depunpack
popd
