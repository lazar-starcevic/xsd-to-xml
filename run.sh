#!/usr/bin/env bash

GEN_DIR="src/main/java/com/xsdtoxml/main/generated"
XSD_DIR="src/main/resources/generated-xsd"
XSD_FILE="$XSD_DIR/schema0.xsd"

if [[ -d "$GEN_DIR" ]]; then
  rm -rf "$GEN_DIR"
  echo "Removed: $GEN_DIR"
else
  echo "No generated dir to remove: $GEN_DIR"
fi

if [[ -f "$XSD_FILE" ]]; then
  rm -f "$XSD_FILE"
  echo "Removed: $XSD_FILE"
else
  echo "No XSD to remove at: $XSD_FILE"
fi

if [[ ! -d "$XSD_DIR" ]]; then
  mkdir "$XSD_DIR"
  echo "Created missing directory: $XSD_DIR"
fi

echo "Building..."
mvn clean install

MAIN_CLASS="${MAIN_CLASS:-com.xsdtoxml.main.Main}"
echo "Running $MAIN_CLASS"
mvn -Dexec.mainClass="$MAIN_CLASS" exec:java
