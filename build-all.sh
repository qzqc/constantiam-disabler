#!/bin/bash
set -e

export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"

TOML="gradle/libs.versions.toml"
OUTDIR="build/libs"

build_version() {
    local mc_ver="$1"
    local yarn_ver="$2"
    local fabric_ver="$3"
    local meteor_ver="$4"
    local output_name="$5"

    echo "=== Building for Minecraft $mc_ver ==="

    sed -i "s|^minecraft = \".*\"|minecraft = \"$mc_ver\"|" "$TOML"
    sed -i "s|^yarn-mappings = \".*\"|yarn-mappings = \"$yarn_ver\"|" "$TOML"
    sed -i "0,/^fabric-loader = \".*\"/s|^fabric-loader = \".*\"|fabric-loader = \"$fabric_ver\"|" "$TOML"
    sed -i "s|^meteor = \".*\"|meteor = \"$meteor_ver\"|" "$TOML"

    ./gradlew clean build

    cp "$OUTDIR/meteor-rcc-spam-addon-1.0.0.jar" "$OUTDIR/${output_name}.jar"
    echo "Built: $OUTDIR/${output_name}.jar"
}

build_version "1.21.1" "1.21.1+build.3" "0.15.11" "0.5.8-SNAPSHOT" "addon-1.21.1"
cp "$OUTDIR/addon-1.21.1.jar" /tmp/addon-1.21.1.jar
build_version "1.21.4" "1.21.4+build.8" "0.16.10" "1.21.4-SNAPSHOT" "addon-1.21.4"
cp /tmp/addon-1.21.1.jar "$OUTDIR/addon-1.21.1.jar"

echo "=== Done ==="
ls -la $OUTDIR/addon-*.jar
