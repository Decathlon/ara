#!/bin/bash

STABLE_CHART=$(cat charts/stable/Chart.yaml | grep version | sed -e 's/version: //')
STABLE_CHART_BASE=$(echo "${STABLE_CHART}" | sed -e 's/-rc.*//')

if [[ $STABLE_CHART == *-rc* ]]; then
  STABLE_CHART_NUMBER=$(echo "${STABLE_CHART}" | sed -e 's/.*-rc\.//')
  STABLE_CHART_NUMBER=$((STABLE_CHART_NUMBER+1))
else
  STABLE_CHART_NUMBER=0
fi
STABLE_CHART="${STABLE_CHART_BASE}-rc.${STABLE_CHART_NUMBER}"

sed -i'.old' "s/^version:.*$/version: ${STABLE_CHART}/" charts/stable/Chart.yaml
