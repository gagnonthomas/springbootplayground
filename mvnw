#!/bin/sh
# Maven Wrapper script - downloads and runs Maven
set -e

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd "$(dirname "$0")" && pwd)}"
MAVEN_WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
MAVEN_WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"

if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
    WRAPPER_URL=$(grep 'wrapperUrl' "$MAVEN_WRAPPER_PROPERTIES" | cut -d'=' -f2-)
    echo "Downloading Maven Wrapper from $WRAPPER_URL"
    if command -v wget > /dev/null; then
        wget -q -O "$MAVEN_WRAPPER_JAR" "$WRAPPER_URL"
    elif command -v curl > /dev/null; then
        curl -sL -o "$MAVEN_WRAPPER_JAR" "$WRAPPER_URL"
    else
        echo "Error: wget or curl is required to download the Maven Wrapper" >&2
        exit 1
    fi
fi

DIST_URL=$(grep 'distributionUrl' "$MAVEN_WRAPPER_PROPERTIES" | cut -d'=' -f2-)
DIST_NAME=$(basename "$DIST_URL" .zip)
MAVEN_HOME="$HOME/.m2/wrapper/dists/$DIST_NAME"

if [ ! -d "$MAVEN_HOME" ]; then
    mkdir -p "$MAVEN_HOME"
    echo "Downloading Maven from $DIST_URL"
    if command -v wget > /dev/null; then
        wget -q -O "/tmp/$DIST_NAME.zip" "$DIST_URL"
    elif command -v curl > /dev/null; then
        curl -sL -o "/tmp/$DIST_NAME.zip" "$DIST_URL"
    fi
    unzip -q -o "/tmp/$DIST_NAME.zip" -d "$MAVEN_HOME"
    rm -f "/tmp/$DIST_NAME.zip"
fi

MVN_CMD=$(find "$MAVEN_HOME" -name mvn -type f | head -1)
exec "$MVN_CMD" "$@"
