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
	@gcloud --project=build-factory-qualif-47lt secrets versions access latest --secret=karate-ara-qualif-config > secrets/karate-config-qualif.js

# WITH JAVA MAVEN

test-karate: ## Run tests. You can specify TARGET_ENV to change tests env.
	@echo '-- Run tests With env: $(TARGET_ENV) --'
	-@mvn test -DargLine="-Dkarate.env=$(TARGET_ENV) -Dkarate.config.dir=secrets"
	@open file:///$$PWD/target/karate-reports/karate-summary.html


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
