FROM openjdk:8-alpine

ENV XMX=2048m

COPY default-config /opt/ara/default-config

COPY ara.jar /opt/ara/ara.jar

EXPOSE 8080/tcp

VOLUME /opt/ara/data

ENTRYPOINT [ "sh", "-c", "java -Xmx$XMX $0 $@", \
             "-Dspring.profiles.active=prod", \
             "-jar", \
             "/opt/ara/ara.jar", \
             "--spring.config.location=classpath:/,file:/opt/ara/default-config/,file:/opt/ara/data/config/" ]
