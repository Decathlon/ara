#####################################
##            VARIABLES            ##
#####################################

JAVA_VERSION=14

#####################################
##             DEVELOP             ##
#####################################

# WITH JAVA MAVEN

mvn-install: ## DEV - install project
	@mvn -Dmaven.test.skip=true clean install

mvn-run: ## DEV - run api
	@mvn -Pdev spring-boot:run -pl server

# WITH DOCKER

dkr-install: ## DEV - install project with docker
	@docker container run -it --rm \
				--user $(shell id -u):$(shell id -g) \
				-v $${HOME}/.m2:/var/maven/.m2:rw \
				-e MAVEN_CONFIG=/var/maven/.m2 \
				-e MAVEN_OPTS='-Duser.home=/var/maven' \
				-v $(shell pwd):$(shell pwd):rw \
				-w $(shell pwd) \
		maven:3-jdk-$(JAVA_VERSION) \
		mvn -Dmaven.test.skip=true -s settings.xml clean install

start-db-pg: ## DEV - start pg db
	@docker network create ara-net > /dev/null 2>&1 || echo 'Network ara-net already created'
	@docker container run -d --name ara-db --rm \
			--network="ara-net" \
			--net-alias="ara-db" \
			-p 5432:5432 \
			-e POSTGRES_DB=ara \
		 	-e POSTGRES_USER=changeme \
		 	-e POSTGRES_PASSWORD=changeme \
		 	postgres:9.6

start-db-mysql: ## DEV - start mysql db
	@docker network create ara-net > /dev/null 2>&1 || echo 'Network ara-net already created'
	@docker container run -d --name ara-db-mysql --rm \
			--network="ara-net" \
			--net-alias="ara-db-mysql" \
			-p 3306:3306 \
			-e MYSQL_DATABASE=ara \
            -e MYSQL_USER=changeme \
            -e MYSQL_PASSWORD=changeme \
            -e MYSQL_ROOT_PASSWORD=to_change \
		 	mysql:5.7

stop-db-pg: ## DEV - remove pg db
	@docker container rm -f ara-db

stop-db-mysql: ## DEV - remove mysql db
	@docker container rm -f ara-db-mysql

dkr-run: ## DEV - run api with docker
	@docker network create ara-net > /dev/null 2>&1 || echo 'Network ara-net already created'
	@docker container run -it --name ara-api --rm \
				--network="ara-net" \
				--net-alias="ara-api" \
				--user $(shell id -u):$(shell id -g) \
				-v $${HOME}/.m2:/var/maven/.m2:rw \
				-e MAVEN_CONFIG=/var/maven/.m2 \
				-e MAVEN_OPTS='-Duser.home=/var/maven' \
				-p 8080:8080 \
				-v $(shell pwd):$(shell pwd):rw \
				-w $(shell pwd) \
		maven:3-jdk-$(JAVA_VERSION) \
		mvn -Pint -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:postgresql://ara-db:5432/ara" spring-boot:run -pl server

#####################################
##              UTILS              ##
#####################################

get-version:
	@echo $$(xmllint --xpath '/*[local-name()="project"]/*[local-name()="version"]/text()' pom.xml)

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.DEFAULT_GOAL := help
