#####################################
##            VARIABLES            ##
#####################################

JAVA_VERSION=16
VERSION=$(shell make -s get-version)

#####################################
##             DEVELOP             ##
#####################################

# WITH JAVA MAVEN

mvn-install: ## Install project
	@./mvnw clean install -Pdev

mvn-tests: ## Run tests
	@docker compose -f ../docker-compose.dev.yaml up -d oauth2-dev-server
	@docker run --rm --network host aligor/wait-for-url --url http://oauth2.dev.localhost:9001/actuator/health
	@./mvnw clean verify

mvn-run: ## DEV - run api
	@./mvnw -Pdev spring-boot:run -pl api

# WITH DOCKER

dkr-install: ## DEV - install project with docker
	@docker container run -it --rm \
				--user $(shell id -u):$(shell id -g) \
				-v $${HOME}/.m2:/var/maven/.m2:rw \
				-e MAVEN_CONFIG=/var/maven/.m2 \
				-e MAVEN_OPTS='-Duser.home=/var/maven' \
				-v $(shell pwd):$(shell pwd):rw \
				-w $(shell pwd) \
		openjdk:$(JAVA_VERSION) \
		./mvnw -Dmaven.test.skip=true clean install

dkr-run: ## DEV - run api with docker
	@docker container run -it --name ara-api --rm \
				--network="host" \
				--user $(shell id -u):$(shell id -g) \
				-v $${HOME}/.m2:/var/maven/.m2:rw \
				-e MAVEN_CONFIG=/var/maven/.m2 \
				-e MAVEN_OPTS='-Duser.home=/var/maven' \
				-e ARA_CLIENTBASEURL='host.docker.internal:8081' \
				-v $(shell pwd):$(shell pwd):rw \
				-w $(shell pwd) \
		openjdk:$(JAVA_VERSION) \
		./mvnw -Pdev spring-boot:run -pl api

#####################################
##             PACKAGE             ##
#####################################

mvn-build-image: ## Build image with maven
ifdef FULL_IMAGE
	@echo 'Building with specfic image name: $(FULL_IMAGE)'
endif
	@./mvnw spring-boot:build-image -Dmaven.test.skip=true -pl api
	@docker tag docker.io/decathlon/ara-api:$(VERSION) $(FULL_IMAGE)

#####################################
##              UTILS              ##
#####################################

get-version: ## Get component version
	@echo $$(xmllint --xpath '/*[local-name()="project"]/*[local-name()="version"]/text()' pom.xml)

update-version: ## Update component version
	@./mvnw --batch-mode release:update-versions -DautoVersionSubmodules=true -DdevelopmentVersion=$(VERSION)-SNAPSHOT
	@./mvnw --batch-mode versions:set -DremoveSnapshot -DprocessAllModules

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.DEFAULT_GOAL := help
