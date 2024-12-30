#!/bin/bash

set -e

pushd .
cd test1
pwd
JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/ ./gradlew clean
popd

pushd .
cd test2
pwd
JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/ ./gradlew clean
popd

pushd .
cd test3
pwd
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/ ./gradlew clean
popd
