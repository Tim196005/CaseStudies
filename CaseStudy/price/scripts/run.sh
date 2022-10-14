#!/usr/bin/env bash

export JAVA_OPTS="-Xms512m -Xmx4096m -XX:+UseG1GC -Djava.net.preferIPv4Stack=true -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8078"

export BASE_DIR="$(pwd)/"
export JAVA_CONFIG_DIR="${BASE_DIR}/resources/"

echo "========================================================================================="
echo "PATH: $PATH"
echo "JAVA_HOME: $JAVA_HOME"
echo "JAVA_OPTS: $JAVA_OPTS"
echo "BASE_DIR: $BASE_DIR"
echo "JAVA_CONFIG_DIR: $JAVA_CONFIG_DIR"
echo "========================================================================================="

java -version

java ${JAVA_OPTS} -jar ./target/price-1.0.0-SNAPSHOT.jar
