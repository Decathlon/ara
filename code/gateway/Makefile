#####################################
##            VARIABLES            ##
#####################################

JAVA_VERSION=17

#####################################
##             DEVELOP             ##
#####################################

# WITH JAVA MAVEN

mvn-install: ## Install project
	@./mvnw clean install

mvn-run: ## DEV - run gw
	@../bin/up-with-clean.sh

# WITH DOCKER

dkr-install: ## DEV - install project with docker
#	@echo "Not yet implemented, ensure its needed before"

## DEV - run gw with docker
dkr-run: export NO_DOWN = true
dkr-run: 
	@../bin/up.sh ara-gw

#####################################
##             PACKAGE             ##
#####################################

mvn-build-image: ## Build image with maven
ifdef $(FULL_IMAGE)
	@echo 'Building with specfic image name: $(FULL_IMAGE)'
endif
	@./mvnw spring-boot:build-image -Dmaven.test.skip=true -Dspring-boot.build-image.imageName=$(FULL_IMAGE)

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
