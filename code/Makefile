ifdef DOCKER_REPO
	DOCKER_REGISTRY=$(DOCKER_REPO)/
endif
VERSION?=latest

cache-build-back:
	@echo '-- Build java ara-back with local cache --'
	@docker container run -v "$$PWD/back":/root/ara -w /root/ara maven:3-jdk-11 mvn -Pprod -s settings.xml clean package dependency:go-offline

build-db: ## Build ara-db
	@echo '-- Build $(DOCKER_REGISTRY)ara-db:$(VERSION) --'
	@docker build -t $(DOCKER_REGISTRY)ara-db:$(VERSION) data/instance

build-back: ## Build ara-back
	@echo '-- Build $(DOCKER_REGISTRY)ara-back:$(VERSION) --'
	@docker build -t $(DOCKER_REGISTRY)ara-back:$(VERSION) back

build-front: ## Build ara-front
	@echo '-- Build $(DOCKER_REGISTRY)ara-front:$(VERSION) --'
	@docker build -t $(DOCKER_REGISTRY)ara-front:$(VERSION) front

run-front: ## Run front
	@echo '-- Run ara-front --'
	@docker-compose up ara-front

run-back: ## Run back
	@echo '-- Run ara-back --'
	@docker-compose up ara-back

run-db: ## Run database
	@echo '-- Run database --'
	@docker-compose up ara-db

build: build-back build-front build-db ## Build ARA

build-parallel: ## Build ARA in parallel
	@$(MAKE) build -j3 -k

start: ## Start ARA
	@echo '-- Start ARA --'
	@docker-compose up

stop: ## Stop ARA
	@echo '-- Stop ARA --'
	@docker-compose down

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.DEFAULT_GOAL := help
