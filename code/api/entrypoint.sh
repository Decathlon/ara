#!/bin/bash

DATABASE_PARAMS=''

case $DATABASE_TYPE in
    mysql)
        echo "ARA configured with mysql database"
        DATABASE_PARAMS="${DATABASE_PARAMS}-Dspring.datasource.url=jdbc:mysql://$DATABASE_HOST/$DATABASE_NAME?sessionVariables=sql_mode='STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' "
        ;;
    h2)
        echo "ARA configured with his h2 embedded database"
        DATABASE_PARAMS="${DATABASE_PARAMS}-Dspring.datasource.url=jdbc:h2:$DATABASE_HOST:$DATABASE_NAME;DB_CLOSE_ON_EXIT=FALSE "
        ;;
    *)
        echo "ERROR: please specify a DATABASE_TYPE env var with the target database type"
        exit 1
        ;;
esac

if [[ ! -z $DATABASE_USER ]]
then
    DATABASE_PARAMS="${DATABASE_PARAMS}-Dspring.datasource.username=$DATABASE_USER "
fi

if [[ ! -z $DATABASE_PASSWORD ]]
then
    DATABASE_PARAMS="${DATABASE_PARAMS}-Dspring.datasource.password=$DATABASE_PASSWORD "
fi

if [[ ! -z $LIQUIBASE_ACTIVE ]]
then
    DATABASE_PARAMS="${DATABASE_PARAMS}-Dspring.liquibase.enabled=$LIQUIBASE_ACTIVE "
fi

if [[ ! -z $HBM2DDL ]]
then
    DATABASE_PARAMS="${DATABASE_PARAMS}-Dspring.jpa.properties.hibernate.hbm2ddl.auto=$HBM2DDL "
fi

java $JAVAOPS \
        -Djava.security.egd=file:/dev/./urandom \
        -Dspring.profiles.active=db-$DATABASE_TYPE \
        $DATABASE_PARAMS \
        -Dara.clientBaseUrl="$CLIENT_URL" \
        -jar app.jar
