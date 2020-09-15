### BUILD image
ARG JAVA_VERSION=14
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
COPY pom.xml settings.xml ./
COPY src src
COPY lib lib
COPY generated-cucumber-report generated-cucumber-report
COPY database database
COPY server server
COPY settings.xml .m2/* .m2/
RUN mvn -s settings.xml clean install

FROM openjdk:$JAVA_VERSION-slim as runtime
EXPOSE 8080
#Set app home folder
ENV APP_HOME /app

ENV JAVA_OPTS=-Xmx2048m

ENV DATABASE_TYPE=h2
ENV DATABASE_URL="mem:ara-db"
ENV CLIENT_URL=http://ara-front

#Create base app folder
WORKDIR $APP_HOME
#Create folder to save configuration files and application logs
RUN mkdir config log
VOLUME $APP_HOME/log
VOLUME $APP_HOME/config

#Copy executable jar file from the builder image
COPY --from=builder /build/server/target/ara-server.jar app.jar
COPY entrypoint.sh .
ENTRYPOINT [ "./entrypoint.sh"]
#Second option using shell form:
#ENTRYPOINT exec java $JAVA_OPTS -jar app.jar $0 $@
