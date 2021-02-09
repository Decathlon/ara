#!/bin/bash

STABLE_CHART=$(cat charts/stable/Chart.yaml | grep version | sed -e 's/version: //')
DEV_CHART=$(cat charts/dev/Chart.yaml | grep version | sed -e 's/version: //')
DEV_CHART_BASE=$(echo "${DEV_CHART}" | sed -e 's/-rc.*//')

if [ "$STABLE_CHART" = "$DEV_CHART_BASE" ]; then
  >&2 echo "ERROR: Same chart version for dev and stable (${STABLE_CHART})"
  exit 1
fi

rm -rf charts/stable
cp -r charts/dev charts/stable

sed -i'.old' "s/^version:.*$/version: ${DEV_CHART_BASE}/" charts/stable/Chart.yaml
