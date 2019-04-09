#!/bin/bash
###############################################################################
# manage-db.sh
#
# This script provide utilities to manipulate the database's container of ARA.
# You may want to update the CONFIGURATION part of this script (bellow) to 
# adapt it to your specific installation.
#
# You can use this script with the following commands : 
# - create : Pull the mysql db, run the image and create the db's structure
# - destroy : Destroy the container builded by the image but leave the data
#             untouched.
# - start : Start the database's container.
# - stop : Stop the database's container.
# - mysqladmin : Run the `mysql` command in the container in interactive mode.
# - shell : Run an interactive shell in the container.
# - purge : Destroy the container, and remove the data persisted in the host.
###############################################################################


###############################################################################
# CONFIGURATION
###############################################################################
# Flag to set to 1 to enable the logging of the actual commands used.
LOGS=1
# The name of the container which will holds the db to manipulate
CONTAINER_NAME=ara-db
# The password of the Database (you should use a secret here instead).
PASSWORD=V*n6MxBq7mr?4P?M
# The container port exposed to the host.
PORT=3306


###############################################################################
# LOGIC
###############################################################################
function run {
	if [ $LOGS -eq 1 ]
		then
			echo $1
	fi
	eval $1
	return $?
}

function usage {
	echo "usage : manage-db.sh <cmd>"
	echo "Use the 'help' cmd to get a full list of all the available commands."
}


MYSQL_VERSION=5.5        # The inno_db conf prevents newer versions.
CONF_DIR=`pwd`/db-conf   # The path to the folder which contains custom configs
SQL_DIR=`pwd`/db-sql     # The path to the folder which contains db structure.

function stopContainer {
	echo "[ARA] Stopping $CONTAINER_NAME container..."
	run "docker container stop $CONTAINER_NAME"
	return $?
}

function startContainer {
	echo "[ARA] Starting $CONTAINER_NAME container..."
	run "docker container start $CONTAINER_NAME"
	return $?
}

function createContainer {
	dataDir="$1"
	echo "[ARA] Create the folder on host if not exists..."
	run "mkdir -p $dataDir"
	echo "[ARA] Pulling base mysql image..."
	run "docker pull mysql:$MYSQL_VERSION"
	echo "[ARA] Create the $CONTAINER_NAME container (data in $dataDir)"
	docker_cmd="docker run"
	docker_cmd="$docker_cmd --name $CONTAINER_NAME"
	docker_cmd="$docker_cmd -e MYSQL_ROOT_PASSWORD=$PASSWORD"
	docker_cmd="$docker_cmd -v /$dataDir:/var/lib/mysql" # Starting-slashes are for Windows compatibility
	docker_cmd="$docker_cmd -v /$CONF_DIR:/etc/mysql/conf.d"
	docker_cmd="$docker_cmd -v /$SQL_DIR:/docker-entrypoint-initdb.d"
	docker_cmd="$docker_cmd -p $PORT:3306"
	docker_cmd="$docker_cmd -d mysql:$MYSQL_VERSION"
	run "$docker_cmd"
	return $?
}

function destroy {
	echo "[ARA] Removing $CONTAINER_NAME container..."
	run "docker container rm $CONTAINER_NAME"
	return $?
}

################################ MAIN #########################################

if [ "$1" = "create" ]
	then
		if [ "$2" = "" ]
			then
				echo "[ARA] ERROR : a folder path is expected to persists the data."
				echo " "
				usage
				exit 1
		fi
		createContainer $2
		startContainer
		exit $?
fi

if [ "$1" = "start" ]
	then
		startContainer
		exit $?
fi

if [ "$1" = "stop" ]
	then
		stopContainer
		exit $?
fi

if [ "$1" = "mysqladmin" ]
	then
		run "docker exec -it $CONTAINER_NAME mysql -uroot -p$PASSWORD"
		exit $?
fi

if [ "$1" = "shell" ]
	then
		run "docker exec -it $CONTAINER_NAME /bin/sh"
		exit $?
fi

if [ "$1" = "destroy" ]
	then
		stopContainer
		destroy
		exit $?
fi

if [ "$1" = "purge" ]
	then
		s="Are you sure to delete your ARA's data (can't be undone) [y/N] ?  "
		read -p "$s" -n 1 -r
		echo
		if [ "$REPLY" = "Y" ] || [ "$REPLY" = "y" ] || [ "$REPLY" = "yes"] ;
			then
				cmd="docker inspect $CONTAINER_NAME"
				pattern="\"Destination\": \"/var/lib/mysql\""
				sep='"'
				cmd="$cmd | grep -B 1 '$pattern'"
				cmd="$cmd | head -n 1 "
				cmd="$cmd | cut -d '$sep' -f 4"
				result=`eval "$cmd"`
				stopContainer
				destroy
				rm -r $result
				exit $?
		fi
		exit 1
fi

if [ "$1" = "help" ]
	then
		echo "manage-db.sh <cmd> : Manage ARA database's container."
		echo "Where <cmd> can be "
		echo "  - create <data_dir> : Pull the mysql db, run the image and create the db's"
		echo "                        structure. Use <data_dir> as the folder to persists"
		echo "                        the data on the host system."
		echo "  - destroy : Destroy the container builded by the image but leave the data"
      echo "              untouched."
		echo "  - start : Start the database's container."
		echo "  - stop : Stop the database's container."
		echo "  - mysqladmin : Run the 'mysql' command in the container in interactive mode."
		echo "  - shell : Run an interactive shell in the container."
		echo "  - purge : Destroy the container, and remove the data persisted in the host."

		exit 0
fi

usage
exit 1
