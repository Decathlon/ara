#####################################
##            VARIABLES            ##
#####################################

DOCKER_REGISTRY?=docker.io
# Forks may use name/ara for their tests
IMAGE_NAME?=decathlon/ara
export API_VERSION?=$(shell make -s -C api get-version)$(SUFFIX)
export WEB_UI_VERSION?=$(shell make -s -C web-ui get-version)$(SUFFIX)
export GW_VERSION?=$(shell make -s -C gateway get-version)$(SUFFIX)
export OADS_VERSION?=$(shell make -s -C oauth2-dev-server get-version)$(SUFFIX)

API_SHORT_IMAGE=${IMAGE_NAME}-api
API_IMAGE=${DOCKER_REGISTRY}/${API_SHORT_IMAGE}

WEB_UI_SHORT_IMAGE=${IMAGE_NAME}-web-ui
WEB_UI_IMAGE=${DOCKER_REGISTRY}/${WEB_UI_SHORT_IMAGE}

GW_SHORT_IMAGE=${IMAGE_NAME}-gw
GW_IMAGE=${DOCKER_REGISTRY}/${GW_SHORT_IMAGE}

OADS_SHORT_IMAGE=${IMAGE_NAME}-oads
OADS_IMAGE=${DOCKER_REGISTRY}/${OADS_SHORT_IMAGE}

PUBLISH?=FALSE

#####################################
##           QUICK START           ##
#####################################

debug:
	echo $(API_VERSION)

## Start ARA
start:
	@echo '-- Start ARA --'
	@docker compose up -d

stop: ## Stop ARA
	@echo '-- Stop ARA --'
	@docker compose down


# RUN BY MODULE

run-%: ## Run module
	@echo '-- Run $* --'
	@docker compose up $(OPT) ara-$*

stop-%: ## Stop module
	@echo '-- Stop $* --'
	@docker compose stop $(OPT) ara-$*

#####################################
##            BUILD APP            ##
#####################################

build: ## Build ARA in parallel
	@$(MAKE) build-full -j4 -k

build-full: build-api build-web-ui build-gw build-oads

build-web-ui: ## Build ara-web-ui
	@echo '-- Build WEB-UI image --'
	@$(eval FULL_IMAGE = $(WEB_UI_IMAGE):$(WEB_UI_VERSION))
	$(MAKE) -C web-ui dkr-build-image FULL_IMAGE=$(FULL_IMAGE)
	@./bin/docker-tags-version.sh "$(WEB_UI_IMAGE)" "$(WEB_UI_VERSION)" "$(PUBLISH)"

build-api: ## Build ara-api
	@echo '-- Build API image --'
	@$(eval FULL_IMAGE = $(API_IMAGE):$(API_VERSION))
	$(MAKE) -C api mvn-install
	$(MAKE) -C api mvn-build-image FULL_IMAGE=$(FULL_IMAGE)
	@./bin/docker-tags-version.sh "$(API_IMAGE)" "$(API_VERSION)" "$(PUBLISH)"

build-gw: ## Build the ara gateway
	@echo '-- Build GATEWAY image'
	@$(eval FULL_IMAGE = $(GW_IMAGE):$(GW_VERSION))
	$(MAKE) -C gateway mvn-install
	$(MAKE) -C gateway mvn-build-image FULL_IMAGE=$(FULL_IMAGE)
	@./bin/docker-tags-version.sh "$(GW_IMAGE)" "$(GW_VERSION)" "$(PUBLISH)"

build-oads: ## Build the ara oauth2-dev-server
	@echo '-- Build OADS image'
	@$(eval FULL_IMAGE = $(OADS_IMAGE):$(OADS_VERSION))
	$(MAKE) -C oauth2-dev-server mvn-install
	$(MAKE) -C oauth2-dev-server mvn-build-image FULL_IMAGE=$(FULL_IMAGE)
	@./bin/docker-tags-version.sh "$(OADS_IMAGE)" "$(OADS_VERSION)" "$(PUBLISH)"

check-web-ui-image:
	@$(MAKE) check-image CHECK_IMAGE=$(WEB_UI_SHORT_IMAGE) CHECK_TAG=$(WEB_UI_VERSION)

check-api-image:
	@$(MAKE) check-image CHECK_IMAGE=$(API_SHORT_IMAGE) CHECK_TAG=$(API_VERSION)

check-gw-image:
	@$(MAKE) check-image CHECK_IMAGE=$(GW_SHORT_IMAGE) CHECK_TAG=$(GW_VERSION)

check-oads-image:
	@$(MAKE) check-image CHECK_IMAGE=$(OADS_SHORT_IMAGE) CHECK_TAG=$(OADS_VERSION)

check-image:
	@curl --silent -f -L https://hub.docker.com/v2/repositories/$(CHECK_IMAGE)/tags/$(CHECK_TAG)/ > /dev/null && echo true || echo false

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
	@docker compose -f api/migration-$(BASE).yaml up -d ara-$(BASE)-api
	@docker compose -f api/migration-$(BASE).yaml up -d ara-$(BASE)-mig-api
	@docker compose -f api/migration-$(BASE).yaml run waiter-$(BASE)-api
	@docker compose -f api/migration-$(BASE).yaml run waiter-$(BASE)-mig-api

stop-migration-stack:
	@docker compose -f api/migration-$(BASE).yaml down

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
	@echo -n 'gateway: '
	@$(MAKE) get-gateway-version
	@echo -n 'oads: '
	@$(MAKE) get-oauth2-dev-server-version

TARGET_CHART?=stable
get-chart-version:
	@echo $$(cat ../charts/$(TARGET_CHART)/Chart.yaml | grep version | sed -e 's/version: //')

update-chart-version: ## Update chart version. VERSION required TARGET_CHART optional default stable
	@echo '-- Update Helm Chart version --'
	@sed -i'.old' 's/^version:.*$\/version: $(VERSION)/' ../charts/$(TARGET_CHART)/Chart.yaml
	@$(MAKE) get-chart-version TARGET_CHART=$(TARGET_CHART)

copy-candidate-to-stable: ## Copy Candidate chart to Stable chart
	@echo -n 'Copy '
	@$(MAKE) get-chart-version TARGET_CHART=candidate
	@echo '... to stable chart'
	@rm -rf ../charts/stable && cp -r ../charts/candidate ../charts/stable
	@$(eval CHART_VERSION=$(shell make get-chart-version TARGET_CHART=candidate | sed -e 's/-rc.*//'))
	@$(MAKE) update-chart-version VERSION=$(CHART_VERSION) TARGET_CHART=stable

get-%-version: ## Get module version
	@$(MAKE) -C $* get-version

update-%-version: ## Update module version. VERSION required
	@echo '-- Update $* version --'
	@$(MAKE) -C $* update-version VERSION=$(VERSION)

#####################################
##            HELM PART            ##
#####################################

# Helm template
# examples:
# make helm-install => templates from the stable chart
# make helm-install TARGET_CHART=candidate => templates from the candidate chart
# make helm-install LOCAL_VALUES=true TARGET_CHART=candidate => templates from the candidate chart and use custom values
helm-template: 
	$(eval CMD_LOCAL_VALUES := $(shell test "$(LOCAL_VALUES)" = "true" && echo "-f ../charts/local-values.yaml"))
	helm template $(CMD_LOCAL_VALUES) ../charts/$(TARGET_CHART) --debug

# Helm install ( same options as template )
helm-install: 
	$(eval CMD_LOCAL_VALUES := $(shell test "$(LOCAL_VALUES)" = "true" && echo "-f ../charts/local-values.yaml"))
	helm install $(CMD_LOCAL_VALUES) --create-namespace --namespace ara ara-local ../charts/$(TARGET_CHART)

# Helm upgrade ( same options as template )
helm-upgrade:
	$(eval CMD_LOCAL_VALUES := $(shell test "$(LOCAL_VALUES)" = "true" && echo "-f ../charts/local-values.yaml"))
	helm upgrade $(CMD_LOCAL_VALUES) --namespace ara ara-local ../charts/$(TARGET_CHART)

helm-uninstall:
	@helm uninstall --namespace ara ara-local

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.DEFAULT_GOAL := help
