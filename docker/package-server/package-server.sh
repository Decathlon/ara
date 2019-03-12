#!/usr/bin/env bash

if [[ $1 ]]; then
    TAG=$1
    echo "Using tag '${TAG}' from the first argument"
else
    TAG='ara-db'
    echo "No tag in first argument: using '${TAG}'"
fi

SKIP_TESTS=$2
if [[ ${SKIP_TESTS} = true ]]; then
    OPTIONS='-DskipTests -DskipITs -DskipPitest'
    echo "Skipping tests from the second argument"
else
    OPTIONS=''
    echo "NOT skipping tests, as the second argument is NOT 'true'"
fi

# Start up a new database on a random unused port, to be used by integration tests during the ARA build
echo "Starting integration database..."
CONTAINER_ID=$(docker run --rm -p0:3306 --detach ${TAG})
DB_PORT=$(docker port ${CONTAINER_ID} 3306 | cut -d ':' -f 2)
DB_URL=jdbc:mysql://localhost:${DB_PORT}/ara?useUnicode=yes\&characterEncoding=UTF-8
echo "Integration database started on ${DB_URL}"

# Make sure the database is stopped when using Ctrl+C or when terminating the script normally or with an error code
function stopDatabase {
    echo "Stopping integration database..."
    docker container stop ${CONTAINER_ID}
}
trap stopDatabase EXIT

cd ../..

mvn -Dliquibase.database.url=${DB_URL} \
    -Dspring.datasource.url=${DB_URL} \
    ${OPTIONS} \
    clean install \
    -Pdev_in
