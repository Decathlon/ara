#!/usr/bin/env bash

if [[ "$#" -ne 3 && "$#" -ne 4 ]]; then
    echo "Usage: run-newman.sh <countryCode> <environment> <faillingLevel> [local]" 1>&2
    echo 1>&2
    echo "  * <countryCode> and <environment> arguments are mandatory and using lowercase" 1>&2
    echo "  * <failingLevel> argument is mandatory and equals 0 (no failure) or 1 (failing)" 1>&2
    echo "  * By default, it will run on a Docker image having npm and newman installed." 1>&2
    echo "    To bypass and use a local npm installation, use the \"local\" third " 1>&2
    echo "    argument to make the script install newman first and use it." 1>&2
    echo 1>&2
    echo "Eg.: run-newman.sh us integ 1" 1>&2
    exit
fi

country=$1
environment=$2
failingLevel=$3

if [[ "$3" = "local" ]]; then
    echo "Installing Newman using NPM"
    npm install --global newman
    newmanCommand=newman
else
    echo "Pulling Newman Docker image"
    docker pull postman/newman;
    newmanCommand="docker run --rm --user ${UID} --mount type=bind,source=/$(pwd),target=/etc/newman postman/newman"
fi

echo "Running Postman tests for country ${country} on environment ${environment}"

echo "Clean workspace"
rm -f newman-reports-${failingLevel}/*

if [[ $(uname | grep MINGW) ]]; then
    jq='lib/jq-win64'
else
    jq='lib/jq-linux64'
    chmod +x ${jq}
fi

failed=0
for collection in collections/*.json; do
    filename=$(basename "$collection")
    filename="${filename%.*}"
    folders=$(cat "$collection" | ./${jq} .item[].name | tr '"' '+' | grep "+all+\|+${country}+" | tr -d '\r')
    for folder in ${folders}; do
        folder="${folder:1:${#folder}-2}" # Remove first and last '+' in '+all+be+cn+'
        echo "Run Postman folder ${folder} in collection ${collection}"
        ${newmanCommand} \
            run "$collection" \
            --environment environments-${failingLevel}/${environment}_${country}.postman_environment.json \
            --folder ${folder} \
            --reporters cli,json \
            --reporter-json-export "newman-reports-${failingLevel}/${filename}_${folder}.json"
        lastResult=$?
        if [[ ${lastResult} -ne 0 ]]; then
            failed=${lastResult}
        fi
    done
done

if [[ ${failed} -eq 0 ]]; then
    result=SUCCESS
else
    result=FAILURE
fi

# Let aggregation tools (ARA...) know Newman ran all collections up to the end,
# as they have no way to know how many JSON report files were expected
# and if all report files are here or some are missing
echo -n ${result} > newman-reports-${failingLevel}/result.txt

echo "Done all collections: ${result}"
exit ${failed}
