#####################################
##            VARIABLES            ##
#####################################

JAVA_VERSION=16

#####################################
##             DEVELOP             ##
#####################################

# WITH JAVA MAVEN

mvn-install: ## DEV - install project
	@./mvnw -Dmaven.test.skip=true clean install

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
ifdef $(FULL_IMAGE)
	@echo 'Building with specfic image name: $(FULL_IMAGE)'
endif
	@./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=$(FULL_IMAGE) -pl api

#####################################
##              UTILS              ##
#####################################

get-version:
	@echo $$(xmllint --xpath '/*[local-name()="project"]/*[local-name()="version"]/text()' pom.xml)

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.DEFAULT_GOAL := help
