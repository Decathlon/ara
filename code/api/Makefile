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
		mvn -Dmaven.test.skip=true clean install

dkr-run: ## DEV - run api with docker
	@docker network create ara-net > /dev/null 2>&1 || echo 'Network ara-net already created'
	@docker container run -it --name ara-api --rm \
				--network="ara-net" \
				--user $(shell id -u):$(shell id -g) \
				-v $${HOME}/.m2:/var/maven/.m2:rw \
				-e MAVEN_CONFIG=/var/maven/.m2 \
				-e MAVEN_OPTS='-Duser.home=/var/maven' \
				-p 8080:8080 \
				-v $(shell pwd):$(shell pwd):rw \
				-w $(shell pwd) \
		maven:3-jdk-$(JAVA_VERSION) \
		mvn -Pdev spring-boot:run -pl server

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.DEFAULT_GOAL := help
