#!/usr/bin/env bash

if [[ $1 ]]; then
    DB_TAG=$1
    echo "Using database tag '${DB_TAG}' from the first argument"
else
    TAG='ara-db'
    echo "No database tag in first argument: using '${DB_TAG}'"
fi

if [[ $2 ]]; then
    SERVER_TAG=$2
    echo "Using server tag '${SERVER_TAG}' from the second argument"
else
    TAG='ara-server'
    echo "No server tag in second argument: using '${SERVER_TAG}'"
fi

if [[ $3 ]]; then
    DB_PORT=$3
    echo "Using database port '${DB_PORT}' from the third argument"
else
    DB_PORT=3306
    echo "No database port in third argument: using '${DB_PORT}'"
fi

if [[ $4 ]]; then
    SERVER_PORT=$4
    echo "Using server port '${SERVER_PORT}' from the fourth argument"
else
    SERVER_PORT=8080
    echo "No server port in fourth argument: using '${SERVER_PORT}'"
fi

if [[ $5 ]]; then
    DATA_DIR=$5
    echo "Using data directory '${DATA_DIR}' from the fifth argument"
else
    DATA_DIR=../data
    echo "No data directory in fifth argument: using '${DATA_DIR}'"
fi

# Create all data/* sub-directories required by volumes from the Docker images of the compose file
cat docker-compose-template.yml \
    | sed -rn 's/.*- __DATA_DIR__(.+)\:.+/\1/p' \
    | xargs --replace=DIR mkdir -p ${DATA_DIR}/DIR

cat docker-compose-template.yml \
    | sed s!__DB_TAG__!${DB_TAG}!g \
    | sed s!__SERVER_TAG__!${SERVER_TAG}!g \
    | sed s/__DB_PORT__/${DB_PORT}/g \
    | sed s/__SERVER_PORT__/${SERVER_PORT}/g \
    | sed s!__DATA_DIR__!${DATA_DIR}!g \
    > docker-compose.yml

docker-compose up
