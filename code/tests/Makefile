#####################################
##            VARIABLES            ##
#####################################

JAVA_VERSION=17

TARGET_ENV?=local

#####################################
##             DEVELOP             ##
#####################################

# Get ara tests secrets

secrets-import: ## Import secrets from GCP
	@mkdir -p secrets
	@gcloud secrets versions access latest --secret=karate-ara-qualif-config > secrets/karate-config-qualif.js

start-local: ## Start local environment
	@docker compose -f ../docker-compose.yaml up -d

destroy-local: ## Destroy local environment
	@docker compose -f ../docker-compose.yaml down

logs-local: ## Display logs for local, SERVICE can be set to choose service to log
	@docker compose -f ../docker-compose.yaml logs $(SERVICE) -f --tail 200

test-karate: ## Run tests. You can specify TARGET_ENV to change tests env.
	@echo '-- Run tests With env: $(TARGET_ENV) --'
	@echo 'Check app availability'
	@docker compose -f docker-compose-tests.yaml run available-check
ifeq ($(TARGET_ENV),local)
	@docker compose -f docker-compose-tests.yaml run load-data && echo 'Test data initialized'
endif
	@mvn clean verify -DargLine="-Dkarate.env=$(TARGET_ENV) -Dkarate.config.dir=secrets" && echo 'SUCCESS' >> target/results.txt || echo 'FAILURE' >> target/results.txt
ifneq ($(BATCH),true)
	@open file:///$$PWD/target/karate-reports/karate-summary.html
endif


#####################################
##             PACKAGE             ##
#####################################


#####################################
##              UTILS              ##
#####################################

get-version: ## Get project version
	@echo $$(xmllint --xpath '/*[local-name()="project"]/*[local-name()="version"]/text()' pom.xml)

update-version: ## Update project version. VERSION required
	@mvn --batch-mode release:update-versions -DautoVersionSubmodules=true -DdevelopmentVersion=$(VERSION)-SNAPSHOT
	@mvn --batch-mode versions:set -DremoveSnapshot -DprocessAllModules

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.DEFAULT_GOAL := help
