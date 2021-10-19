#!  /bin/bash
set -x

BIN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cd ${BIN_PATH}/..
# Stop previous version except if asked ( NO_DOWN var set )
test -z "$NO_DOWN" && docker-compose -f docker-compose.dev.yml down --remove-orphans
docker-compose -f docker-compose.dev.yml up -d $@
cd -
