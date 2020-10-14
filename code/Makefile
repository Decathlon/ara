#####################################
##            VARIABLES            ##
#####################################

DOCKER_REGISTRY?=docker.io
# Forks may use name/ara for their tests
IMAGE_NAME?=decathlon/ara
VERSION?=latest

API_IMAGE=${DOCKER_REGISTRY}/${IMAGE_NAME}-api:${VERSION}
WEB_UI_IMAGE=${DOCKER_REGISTRY}/${IMAGE_NAME}-web-ui:${VERSION}

DIFF_LABEL = "to-be-changed"
DATE_WITH_TIME := $(shell /bin/date "+%Y%m%d%H%M%S")

#####################################
##           QUICK START           ##
#####################################

start: ## Start ARA
	@echo '-- Start ARA --'
	@export VERSION=$(VERSION); docker-compose up

stop: ## Stop ARA
	@echo '-- Stop ARA --'
	@export VERSION=$(VERSION); docker-compose down


# RUN BY MODULE

run-db: ## Run database
	@echo '-- Run database --'
	@export VERSION=$(VERSION); docker-compose up ara-db

run-api: ## Run api
	@echo '-- Run ara-api --'
	@export VERSION=$(VERSION); docker-compose up ara-api

run-web-ui: ## Run web-ui
	@echo '-- Run ara-web-ui --'
	@export VERSION=$(VERSION); docker-compose up ara-web-ui

start-db: ## Start database in background
	@echo '-- Start database in background --'
	@export VERSION=$(VERSION); docker-compose up -d ara-db

#####################################
##            BUILD APP            ##
#####################################

build: ## Build ARA in parallel
	@$(MAKE) build-full -j2 -k

build-full: build-api build-web-ui ## Build ARA

build-web-ui: ## Build ara-web-ui
	@echo '-- Build ${WEB_UI_IMAGE} --'
	@docker build -t ${WEB_UI_IMAGE} web-ui

build-api: ## Build ara-api
	@echo '-- Build ${API_IMAGE} --'
	@docker build -t ${API_IMAGE} api

#####################################
##             UPDATER             ##
#####################################

# CREATE LIQUIBASE DIFF

liquibase-diff: compose-migration wait-migration-stack ## Create a liquibase diff file -- DIFF_LABEL required
	@docker container run -it --net=host --rm \
		--user $(shell id -u):$(shell id -g) \
		-v $${HOME}/.m2:/var/maven/.m2:rw \
		-e MAVEN_CONFIG=/var/maven/.m2 \
		-v $(shell pwd):$(shell pwd):rw \
		-w $(shell pwd) \
		maven:alpine \
		mvn -Duser.home=/var/maven -f api/pom.xml -pl database liquibase:diff -DdiffChangeLogFile=api/database/src/main/resources/db/changelog/changes/${DATE_WITH_TIME}-${DIFF_LABEL}.yaml
	@echo "  - include:" >> api/database/src/main/resources/db/changelog/db.changelog-master.yaml
	@echo "      file: classpath*:db/changelog/changes/$(DATE_WITH_TIME)-$(DIFF_LABEL).yaml" >> api/database/src/main/resources/db/changelog/db.changelog-master.yaml
	@$(MAKE) stop-migration-stack

compose-migration:
	@echo '-- Up migration stack --'
	@docker-compose -f api/migration.yaml build
	@docker-compose -f api/migration.yaml up -d

wait-migration-stack:
	docker container run --rm --net=host aligor/wait-for-url --url http://localhost:9980/actuator/info --retries 20 --delay 3000
	docker container run --rm --net=host aligor/wait-for-url -url http://localhost:9981/actuator/info --retries 20 --delay 3000

stop-migration-stack:
	@docker-compose -f api/migration.yaml down

# UPDATE ARA VERSION

update-version: update-chart-version update-maven-version commit-version ## Update ARA version -- VERSION required

update-chart-version:
	@echo '-- Update Helm Chart version --'
	@sed -i'.old' 's/^version:.*$\/version: $(VERSION)/' ../charts/stable/Chart.yaml

update-maven-version:
	@echo '-- Update Maven version --'
	@mvn --batch-mode -f api/pom.xml -s api/settings.xml release:update-versions -DautoVersionSubmodules=true -DdevelopmentVersion=$(VERSION)-SNAPSHOT
	@mvn --batch-mode -f api/pom.xml -s api/settings.xml versions:set -DremoveSnapshot -DprocessAllModules

commit-version:
	@echo '-- Commit new version --'
	@git add -A && git commit -m "⬆️  Upgrade to $(VERSION)" && git push


#####################################
##            HELM PART            ##
#####################################

helm-tempate: ## Helm template
	@helm template ../charts/stable --debug

helm-install: ## Helm install ara-local
	@helm install --create-namespace --namespace ara ara-local ../charts/stable

helm-upgrade: ## Helm upgrade ara-local
	@helm upgrade --namespace ara ara-local ../charts/stable

helm-uninstall: ## Helm uninstall ara-local
	@helm uninstall --namespace ara ara-local

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.DEFAULT_GOAL := help
