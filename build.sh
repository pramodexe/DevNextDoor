#!/bin/bash

# Set Java version to 21 for compatibility with Gradle 8.13
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

# Build the project
./gradlew "$@"
