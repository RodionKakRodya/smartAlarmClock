#!/usr/bin/env bash
set -euo pipefail
base64 --decode gradle/wrapper/gradle-wrapper.jar.base64 > gradle/wrapper/gradle-wrapper.jar
echo "Restored gradle/wrapper/gradle-wrapper.jar"
