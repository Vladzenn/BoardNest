#!/usr/bin/env sh
set -eu
MAVEN_VERSION=3.9.11
MAVEN_HOME="${HOME}/.m2/wrapper/dists/apache-maven-${MAVEN_VERSION}"
if [ ! -x "${MAVEN_HOME}/bin/mvn" ]; then
  echo "Maven ${MAVEN_VERSION} is not cached. Downloading the official Apache Maven distribution..."
  ROOT="${HOME}/.m2/wrapper/dists"
  mkdir -p "$ROOT"
  ARCHIVE="${ROOT}/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
  URL="https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
  if command -v curl >/dev/null 2>&1; then
    curl -fL "$URL" -o "$ARCHIVE"
  elif command -v wget >/dev/null 2>&1; then
    wget "$URL" -O "$ARCHIVE"
  else
    echo "curl or wget is required to download Maven." >&2
    exit 1
  fi
  tar -xzf "$ARCHIVE" -C "$ROOT"
  rm -f "$ARCHIVE"
fi
exec "${MAVEN_HOME}/bin/mvn" "$@"
