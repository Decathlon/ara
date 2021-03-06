#####################################
##            VARIABLES            ##
#####################################

DOCKER_REGISTRY?=docker.io
# Forks may use name/ara for their tests
IMAGE_NAME?=decathlon/ara
API_VERSION?=latest
WEB_UI_VERSION?=latest

API_IMAGE=${DOCKER_REGISTRY}/${IMAGE_NAME}-api:${API_VERSION}
WEB_UI_IMAGE=${DOCKER_REGISTRY}/${IMAGE_NAME}-web-ui:${WEB_UI_VERSION}

#####################################
##           QUICK START           ##
#####################################

start: ## Start ARA
	@echo '-- Start ARA --'
	@export API_VERSION=$(API_VERSION); export WEB_UI_VERSION=$(WEB_UI_VERSION); docker-compose up -d

stop: ## Stop ARA
	@echo '-- Stop ARA --'
	@export API_VERSION=$(API_VERSION); export WEB_UI_VERSION=$(WEB_UI_VERSION); docker-compose down


# RUN BY MODULE

run-%: ## Run module
	@echo '-- Run $* --'
	@export API_VERSION=$(API_VERSION); export WEB_UI_VERSION=$(WEB_UI_VERSION); docker-compose up $(OPT) ara-$*

stop-%: ## Stop module
	@echo '-- Stop $* --'
	@docker-compose up $(OPT) ara-$*

#####################################
##            BUILD APP            ##
#####################################

build: ## Build ARA in parallel
	@$(MAKE) build-full -j2 -k

build-full: build-api build-web-ui

about-web-ui:
	@mkdir -p web-ui/static/version
	@SHA=$(shell git rev-parse --short HEAD) && \
	 WEB_VERSION=$(shell cat web-ui/package.json | docker container run --rm -i aligor/jq jq -r '.version') && \
	 docker container run --rm -i \
	 						-v $(shell pwd)/web-ui/build/template:/usr/src/app:ro \
                            -w /usr/src/app \
	 						aligor/jq \
							jq -n --arg version $$WEB_VERSION \
								  --arg sha $$SHA \
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
    							jq -n --arg version $$API_VERSION \
    								  --arg sha $$SHA \
    								  -f about.jq > \
    								  api/server/src/main/resources/static/api/version/about.json

build-api: about-api ## Build ara-api
	@echo '-- Build ${API_IMAGE} --'
	@docker build -t ${API_IMAGE} api

#####################################
##             UPDATER             ##
#####################################

# CREATE LIQUIBASE DIFF
DIFF_LABEL = "to-be-changed"
DATE_WITH_TIME := $(shell /bin/date "+%Y%m%d%H%M%S")
BASE?=h2

liquibase-diff: build-api up-migration ## Create a liquibase diff file -- DIFF_LABEL required, BASE to specify database
ifeq ($(BASE),h2)
	@$(MAKE) stop-migration-stack
endif
	@docker container run -it --net=host --rm \
		--user $(shell id -u):$(shell id -g) \
		-v $${HOME}/.m2:/var/maven/.m2:rw \
		-e MAVEN_CONFIG=/var/maven/.m2 \
		-v $(shell pwd):$(shell pwd):rw \
		-w $(shell pwd) \
		maven:alpine \
		mvn -Pdb-$(BASE) -Duser.home=/var/maven -f api/pom.xml -pl database liquibase:diff -DdiffChangeLogFile=api/database/src/main/resources/db/changelog/changes/$(BASE)/${DATE_WITH_TIME}-${DIFF_LABEL}.yaml
	@echo "  - include:" >> api/database/src/main/resources/db/changelog/db.changelog-master-$(BASE).yaml
	@echo "      file: classpath*:db/changelog/changes/$(BASE)/$(DATE_WITH_TIME)-$(DIFF_LABEL).yaml" >> api/database/src/main/resources/db/changelog/db.changelog-master-$(BASE).yaml
	@$(MAKE) stop-migration-stack

up-migration:
	@echo '-- Up migration stack --'
	@docker-compose -f api/migration-$(BASE).yaml up -d ara-api
	@docker-compose -f api/migration-$(BASE).yaml up -d ara-mig-api
	@docker-compose -f api/migration-$(BASE).yaml run waiter-api
	@docker-compose -f api/migration-$(BASE).yaml run waiter-mig-api

stop-migration-stack:
	@docker-compose -f api/migration-$(BASE).yaml down

# UPDATE ARA VERSION

get-versions: ## Give ARA version
	@echo -n 'chart stable: '
	@$(MAKE) get-chart-version TARGET_CHART=stable
	@echo -n 'chart candidate: '
	@$(MAKE) get-chart-version TARGET_CHART=candidate
	@echo -n 'api: '
	@$(MAKE) get-api-version
	@echo -n 'web-ui: '
	@$(MAKE) get-web-ui-version

TARGET_CHART?=stable
get-chart-version:
	@echo $$(cat ../charts/$(TARGET_CHART)/Chart.yaml | grep version | sed -e 's/version: //')

update-chart-version: ## Update chart version
	@echo '-- Update Helm Chart version --'
	@sed -i'.old' 's/^version:.*$\/version: $(VERSION)/' ../charts/$(TARGET_CHART)/Chart.yaml

get-api-version:
	@$(MAKE) -C api get-version

update-api-version: ## Update api version
	@echo '-- Update Maven version --'
	@mvn --batch-mode -f api/pom.xml -s api/settings.xml release:update-versions -DautoVersionSubmodules=true -DdevelopmentVersion=$(VERSION)-SNAPSHOT
	@mvn --batch-mode -f api/pom.xml -s api/settings.xml versions:set -DremoveSnapshot -DprocessAllModules

get-web-ui-version:
	@$(MAKE) -C web-ui get-version

update-web-ui-version: ## Update web-ui version
	@echo '-- Update Web-UI version --'
	@npm --prefix web-ui version $(VERSION)

#####################################
##            HELM PART            ##
#####################################

helm-tempate:
	@helm template ../charts/stable --debug

helm-install:
	@helm install --create-namespace --namespace ara ara-local ../charts/stable

helm-upgrade:
	@helm upgrade --namespace ara ara-local ../charts/stable

helm-uninstall:
	@helm uninstall --namespace ara ara-local

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.DEFAULT_GOAL := help
