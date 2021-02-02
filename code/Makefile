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

about-web-ui:
	@mkdir -p web-ui/static/version
	@SHA=$(shell git rev-parse --short HEAD) && \
	 WEB_VERSION=$(shell cat web-ui/package.json | docker container run --rm -i aligor/jq jq -r '.version') && \
	 docker container run --rm -i \
	 						-v $(shell pwd)/web-ui/build/template:/usr/src/app:ro \
                            -w /usr/src/app \
	 						aligor/jq \
							jq -n --arg version $$SHA \
								  --arg sha $$WEB_VERSION \
								  -f about.jq > \
								  web-ui/static/version/about.json

build-web-ui: about-web-ui ## Build ara-web-ui
	@echo '-- Build ${WEB_UI_IMAGE} --'
	@docker build -t ${WEB_UI_IMAGE} web-ui

about-api:
	@mkdir -p api/server/src/main/resources/static/api/version
	@SHA=$(shell git rev-parse --short HEAD) && \
	 API_VERSION=$(shell xmllint --xpath '/*[local-name()="project"]/*[local-name()="version"]/text()' api/pom.xml) && \
	 docker container run --rm -i \
    	 						-v $(shell pwd)/api/build/template:/usr/src/app:ro \
                                -w /usr/src/app \
    	 						aligor/jq \
    							jq -n --arg version $$SHA \
    								  --arg sha $$API_VERSION \
    								  -f about.jq > \
    								  api/server/src/main/resources/static/api/version/about.json

build-api: ## Build ara-api
	@echo '-- Build ${API_IMAGE} --'
	@docker build -t ${API_IMAGE} api

#####################################
##             UPDATER             ##
#####################################

# CREATE LIQUIBASE DIFF

liquibase-diff: build-api ## Create a liquibase diff file -- DIFF_LABEL required
	@$(MAKE) liquibase-all-bases-diff -j2 -k

liquibase-all-bases-diff: liquibase-diff-mysql liquibase-diff-postgresql

liquibase-diff-%: up-migration-% ## Create a liquibase diff file -- DIFF_LABEL required
	@docker container run -it --net=host --rm \
		--user $(shell id -u):$(shell id -g) \
		-v $${HOME}/.m2:/var/maven/.m2:rw \
		-e MAVEN_CONFIG=/var/maven/.m2 \
		-v $(shell pwd):$(shell pwd):rw \
		-w $(shell pwd) \
		maven:alpine \
		mvn -Pdb-$* -Duser.home=/var/maven -f api/pom.xml -pl database liquibase:diff -DdiffChangeLogFile=api/database/src/main/resources/db/changelog/changes/$*/${DATE_WITH_TIME}-${DIFF_LABEL}.yaml
	@echo "  - include:" >> api/database/src/main/resources/db/changelog/db.changelog-master-$*.yaml
	@echo "      file: classpath*:db/changelog/changes/$*/$(DATE_WITH_TIME)-$(DIFF_LABEL).yaml" >> api/database/src/main/resources/db/changelog/db.changelog-master-$*.yaml
	@$(MAKE) stop-migration-stack-$*

up-migration-%:
	@echo '-- Up migration stack --'
	@docker-compose -f api/migration-$*.yaml up -d ara-api
	@docker-compose -f api/migration-$*.yaml up -d ara-mig-api
	@docker-compose -f api/migration-$*.yaml run waiter-api
	@docker-compose -f api/migration-$*.yaml run waiter-mig-api

stop-migration-stack-%:
	@docker-compose -f api/migration-$*.yaml down

# UPDATE ARA VERSION

update-version: ## Update ARA version -- VERSION required
	@$(MAKE) update-all-version -j3 -k

update-all-version: update-chart-version update-maven-version update-front-version

update-chart-version:
	@echo '-- Update Helm Chart version --'
	@sed -i'.old' 's/^version:.*$\/version: $(VERSION)/' ../charts/stable/Chart.yaml

update-maven-version:
	@echo '-- Update Maven version --'
	@mvn --batch-mode -f api/pom.xml -s api/settings.xml release:update-versions -DautoVersionSubmodules=true -DdevelopmentVersion=$(VERSION)-SNAPSHOT
	@mvn --batch-mode -f api/pom.xml -s api/settings.xml versions:set -DremoveSnapshot -DprocessAllModules

update-front-version:
	@echo '-- Update Web-UI version --'
	@npm --prefix web-ui version $(VERSION)

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
