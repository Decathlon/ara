#!/usr/bin/env bash

if [[ $1 ]]; then
    TAG=$1
    echo "Using tag '${TAG}' from the first argument"
else
    TAG='ara-server'
    echo "No tag in first argument: using '${TAG}'"
fi

JAR=ara.jar
rm -f ${JAR}
cp $(ls ../../final/target/ara-*.jar | grep -v javadoc | grep -v sources) ${JAR}

docker build -t ${TAG} .
