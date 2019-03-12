#!/usr/bin/env bash

databaseDirectory="$(pwd)"/../data/db

mkdir -p ${databaseDirectory}

if [[ $1 ]]; then
    TAG=$1
    echo "Using tag '${TAG}' from the first argument"
else
    TAG='ara-db'
    echo "No tag in first argument: using '${TAG}'"
fi

if [[ $2 ]]; then
    PORT=$2
    echo "Using port '${PORT}' from the second argument"
else
    PORT=3306
    echo "No port in second argument: using '${PORT}'"
fi

function stopContainer {
    echo "Stopping ${TAG} container..."
    docker container ls -a | grep ${TAG} | cut -d ' ' -f 1 | xargs docker stop
}

docker run --rm \
           -p${PORT}:3306 \
           -v /${databaseDirectory}:/var/lib/mysql \
           ${TAG} \
       || stopContainer
