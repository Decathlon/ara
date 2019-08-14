#!/bin/sh


java -Xmx2048m -cp ara.jar:/tmp/ara-configs/ -Dspring.profiles.active=prod -Dloader.main=com.decathlon.ara.AraApplication org.springframework.boot.loader.PropertiesLauncher

java -Xmx$XMX \
    -cp /opt/ara/ara.jar:/opt/ara/configs/ \
    -Dspring.datasource.url="jdbc:mysql://$DATABASE_URL?useUnicode=yes&characterEncoding=UTF-8" \
    -Dspring.datasource.username="$DATABASE_USER" \
    -Dspring.datasource.password="$DATABASE_PASSWORD" \
    -Dspring.profiles.active="$ARA_PROFILE" \
    -Dloader.main=com.decathlon.ara.AraApplication \
    org.springframework.boot.loader.PropertiesLauncher