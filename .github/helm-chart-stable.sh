#!/bin/bash

STABLE_CHART=$(cat charts/stable/Chart.yaml | grep version | sed -e 's/version: //')
STABLE_CHART_BASE=$(echo "${DEV_CHART}" | sed -e 's/-rc.*//')

sed -i'.old' "s/^version:.*$/version: ${STABLE_CHART_BASE}/" charts/stable/Chart.yaml
