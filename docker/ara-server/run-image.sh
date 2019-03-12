#!/usr/bin/env bash

dataDirectory="$(pwd)"/../data/server

mkdir -p ${dataDirectory}

if [[ $1 ]]; then
    TAG=$1
    echo "Using tag '${TAG}' from the first argument"
else
    TAG='ara-server'
    echo "No tag in first argument: using '${TAG}'"
fi

if [[ $2 ]]; then
    PORT=$2
    echo "Using port '${PORT}' from the second argument"
else
    PORT=8080
    echo "No port in second argument: using '${PORT}'"
fi

function stopContainer {
    echo "Stopping ${TAG} container..."
    docker container ls -a | grep ${TAG} | cut -d ' ' -f 1 | xargs docker stop
}

docker run --rm \
           -p${PORT}:8080 \
           -v /${dataDirectory}:/opt/ara/data \
           ${TAG} \
       || stopContainer
