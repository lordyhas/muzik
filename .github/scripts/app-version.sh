#!/usr/bin/env bash
# Prints "versionName versionCode" read from an app/build.gradle.kts given on stdin.
#   app-version.sh < app/build.gradle.kts
#   git show origin/production:app/build.gradle.kts | app-version.sh
set -euo pipefail
gradle=$(cat)
name=$(sed -n 's/.*versionName = "\(.*\)".*/\1/p' <<< "$gradle" | head -1)
code=$(sed -n 's/.*versionCode = \([0-9]*\).*/\1/p' <<< "$gradle" | head -1)
if [ -z "$name" ] || [ -z "$code" ]; then
  echo "::error::Could not read versionName/versionCode from app/build.gradle.kts" >&2
  exit 1
fi
echo "$name $code"
