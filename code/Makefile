JAVA_VERSION=14
DOCKER_REGISTRY?=docker.io
# Forks may use name/ara for their tests
IMAGE_NAME?=ara
VERSION?=latest

API_IMAGE=${DOCKER_REGISTRY}/${IMAGE_NAME}-api:${VERSION}
WEB_UI_IMAGE=${DOCKER_REGISTRY}/${IMAGE_NAME}-web-ui:${VERSION}

MIGRATION_LABEL = "to-be-changed"
DATE_WITH_TIME := $(shell /bin/date "+%Y%m%d%H%M%S")

install: ## Install project for dev
	@mvn -Dmaven.test.skip=true -f api/pom.xml clean install

makeMigration: ## Create a migration file
	mvn -f api/pom.xml -pl database liquibase:diff -DdiffChangeLogFile=api/database/src/main/resources/db/changelog/changes/${DATE_WITH_TIME}-${MIGRATION_LABEL}.yaml
	@echo "  - include:" >> api/database/src/main/resources/db/changelog/db.changelog-master.yaml
	@echo "      file: classpath*:db/changelog/changes/$(DATE_WITH_TIME)-$(MIGRATION_LABEL).yaml" >> api/database/src/main/resources/db/changelog/db.changelog-master.yaml

cache-build-api:
	@echo '-- Build java ara-api with local cache --'
	@docker container run -v "$$PWD/api":/root/ara -w /root/ara maven:3-jdk-$(JAVA_VERSION) mvn -s settings.xml clean package dependency:go-offline

build-api: ## Build ara-api
	@echo '-- Build ${API_IMAGE} --'
	@docker build -t ${API_IMAGE} api

build-web-ui: ## Build ara-web-ui
	@echo '-- Build ${WEB_UI_IMAGE} --'
	@docker build -t ${WEB_UI_IMAGE} web-ui

run-web-ui: ## Run web-ui
	@echo '-- Run ara-web-ui --'
	@export VERSION=$(VERSION); docker-compose up ara-web-ui

run-api: ## Run api
	@echo '-- Run ara-api --'
	@export VERSION=$(VERSION); docker-compose up ara-api

run-db: ## Run database
	@echo '-- Run database --'
	@export VERSION=$(VERSION); docker-compose up ara-db

start-db: ## Start database
	@echo '-- Run database --'
	@export VERSION=$(VERSION); docker-compose up -d ara-db

build-full: build-api build-web-ui ## Build ARA

build: ## Build ARA in parallel
	@$(MAKE) build-full -j2 -k

start: ## Start ARA
	@echo '-- Start ARA --'
	@export VERSION=$(VERSION); docker-compose up -d

stop: ## Stop ARA
	@echo '-- Stop ARA --'
	@export VERSION=$(VERSION); docker-compose down

update-maven-version:
	@echo '-- Update Maven version --'
	@mvn --batch-mode -f api/pom.xml -s api/settings.xml release:update-versions -DautoVersionSubmodules=true -DdevelopmentVersion=$(VERSION)-SNAPSHOT
	@mvn --batch-mode -f api/pom.xml -s api/settings.xml versions:set -DremoveSnapshot -DprocessAllModules

update-chart-version:
	@echo '-- Update Helm Chart version --'
	@sed -i'.old' 's/^version:.*$\/version: $(VERSION)/' ../charts/stable/Chart.yaml

commit-version:
	@echo '-- Commit new version --'
	@git add -A && git commit -m "⬆️  Upgrade to $(VERSION)" && git push

update-version: update-chart-version update-maven-version commit-version ## Update project version

helm-tempate:
	@helm template ../charts/stable --debug

helm-uninstall:
	@helm uninstall --namespace ara ara-local

helm-install:
	@helm install --create-namespace --namespace ara ara-local ../charts/stable

helm-upgrade:
	@helm upgrade --namespace ara ara-local ../charts/stable

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.DEFAULT_GOAL := help
