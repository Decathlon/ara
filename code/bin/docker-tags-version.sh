#!/bin/bash

DOCKER_IMAGE=$1
VERSION=$2
PUBLISH=$3

docker tag "${DOCKER_IMAGE}:${VERSION}" "${DOCKER_IMAGE}:latest"

if [[ $PUBLISH == 'TRUE' ]]; then
  echo "Publish ${DOCKER_IMAGE}:${VERSION}"
  docker push "${DOCKER_IMAGE}:${VERSION}"
  echo "Publish ${DOCKER_IMAGE}:latest"
  docker push "${DOCKER_IMAGE}:latest"
fi

if [[ $VERSION =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
  MINOR=${VERSION%.*}
  MAJOR=${MINOR%.*}
  TAGS="${MINOR},${MAJOR}"

  for tag in $(echo "${TAGS}" | sed "s/,/ /g")
  do
      docker tag "${DOCKER_IMAGE}:${VERSION}" "${DOCKER_IMAGE}:${tag}"
      if [[ $PUBLISH == 'TRUE' ]]; then
        echo "Publish ${DOCKER_IMAGE}:${tag}"
        docker push "${DOCKER_IMAGE}:${tag}"
      fi
  done
fi
