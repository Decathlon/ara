#!  /bin/bash

BIN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

OPTIONAL_COMMAND="mvn clean install -Pdev" ${BIN_PATH}/up.sh $@
