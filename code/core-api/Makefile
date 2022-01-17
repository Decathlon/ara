#####################################
##            VARIABLES            ##
#####################################

VERSION=$(shell make -s get-version)
DATABASE?=postgresql

#####################################
##             DEVELOP             ##
#####################################

# WITH JAVA MAVEN

install: ## Install project
	@./mvnw clean install -Pdev

tests: ## Run tests
	@env ARA_DATABASE_TARGET=h2 ./mvnw clean verify

dev: ## Run api in dev mode
	@env ARA_DATABASE_TARGET=$(DATABASE) ./mvnw -Pdev spring-boot:run

run: ## Run api
	@env ARA_DATABASE_TARGET=$(DATABASE) ./mvnw spring-boot:run

# DATABASE

create-postgresql: ## Create and run a postgresql database
	@docker container run --name ara-postgresql \
		-e POSTGRES_DB=ara \
		-e POSTGRES_USER=changeme \
		-e POSTGRES_PASSWORD=changeme \
		-p 5432:5432 -d \
		postgres:12

start-postgresql: ## Start postgresql database
	@docker container start ara-postgresql

stop-postgresql: ## Stop postgresql database
	@docker container stop ara-postgresql

#####################################
##             PACKAGE             ##
#####################################

build-image: ## Build image with maven
ifdef FULL_IMAGE
	@echo 'Building with specfic image name: $(FULL_IMAGE)'
endif
	@./mvnw spring-boot:build-image -Dmaven.test.skip=true
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
