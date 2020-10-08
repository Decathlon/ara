MIGRATION_LABEL = "to-be-changed"
DATE_WITH_TIME := $(shell /bin/date "+%Y%m%d%H%M%S")

run-profile: ## Run with PROFILE
	mvn -P$(PROFILE) spring-boot:run -pl server

makeMigration: ## Create a migration file
	mvn -pl database liquibase:diff -DdiffChangeLogFile=database/src/main/resources/db/changelog/changes/${DATE_WITH_TIME}-${MIGRATION_LABEL}.yaml
	@echo "  - include:" >> database/src/main/resources/db/changelog/db.changelog-master.yaml
	@echo "      file: classpath*:db/changelog/changes/$(DATE_WITH_TIME)-$(MIGRATION_LABEL).yaml" >> database/src/main/resources/db/changelog/db.changelog-master.yaml

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.DEFAULT_GOAL := help
