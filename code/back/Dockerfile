### BUILD image
ARG JAVA_VERSION=11
FROM maven:3-jdk-$JAVA_VERSION as builder
WORKDIR /build
#COPY settings.xml settings.xml
#COPY lib/pom.xml lib/pom.xml
#COPY generated-cucumber-report/pom.xml generated-cucumber-report/pom.xml
#COPY database/pom.xml database/pom.xml
#COPY server/pom.xml server/pom.xml
#COPY only-server.xml only-server.xml
#Download all required dependencies into one layer
#COPY .m2 .m2
#RUN mvn -s settings.xml -f only-server.xml -B dependency:go-offline dependency:resolve-plugins
#Copy source code
#COPY src src
#COPY lib lib
#COPY generated-cucumber-report generated-cucumber-report
#COPY server server
# Build application
COPY . .
RUN mvn -Pprod -s settings.xml clean package dependency:go-offline

FROM openjdk:$JAVA_VERSION-jre-slim as runtime
EXPOSE 8080
#Set app home folder
ENV APP_HOME /app

ENV JAVA_OPTS=-Xmx2048m

ENV DATABASE_URL=ara-db:3306/ara-dev
ENV DATABASE_USER=changeIt
ENV DATABASE_PASSWORD=password_to_change
ENV ARA_PROFILE=dev
ENV CLIENT_URL=http://ara-front
ENV DATA_DB_MODE=validate

#Create base app folder
WORKDIR $APP_HOME
#Create folder to save configuration files and application logs
RUN mkdir config log
VOLUME $APP_HOME/log
VOLUME $APP_HOME/config

#Copy executable jar file from the builder image
COPY --from=builder /build/server/target/ara-server.jar app.jar
ENTRYPOINT [ "sh", "-c", "java $JAVAOPS \
        -Djava.security.egd=file:/dev/./urandom \
        -Dspring.datasource.url=\"jdbc:mariadb://$DATABASE_URL?useUnicode=yes&characterEncoding=UTF-8\" \
        -Dspring.datasource.username=\"$DATABASE_USER\" \
        -Dspring.datasource.password=\"$DATABASE_PASSWORD\" \
        -Dspring.profiles.active=\"$ARA_PROFILE\" \
        -Dara.clientBaseUrl=\"$CLIENT_URL\" \
        -Dspring.jpa.properties.hibernate.hbm2ddl.auto=\"$DATA_DB_MODE\" \
        -jar app.jar"]
#Second option using shell form:
#ENTRYPOINT exec java $JAVA_OPTS -jar app.jar $0 $@
