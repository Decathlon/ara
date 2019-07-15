COMMIT_REF=$(shell git rev-parse HEAD 2>/dev/null || echo "latest")
DOCKER_PUSH=docker push

SERVER_IMAGE=ara-server
SERVER_TAG=$(DOCKER_REGISTRY)/$(SERVER_IMAGE):$(COMMIT_REF)
SERVER_PORT=8080

SKIP_TESTS=false

DATA_DIR=../data

prepare: ## Check precondition of the build & push steps.
ifeq ("$(DOCKER_REGISTRY)/test", "/test")
	@echo "Error : You should call the make command with the param DOCKER_REGISTRY=<address_of_your_registry>"
	@exit 1
else
	@echo "Using registry $(DOCKER_REGISTRY)"
endif

build: build-server ## Build ara-server image


build-server: prepare ## Build ara-server image
	@echo "----- $(currentTarget) -----"
	@cd $(SERVER_IMAGE) && ./build-image.sh $(SERVER_TAG)

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

init-config: ## Make sure the configuration file is present, or ask for some recommended settings in order to create it
	@echo "----- $(currentTarget) -----"
	@cd init-config && ./init-config.sh $(DATA_DIR)

package-server: ## Build the ARA JAR to be integrated into the ara-server Docker image (quite long, so it needs to be called manually, and you can speed it up with SKIP_TESTS=true)
	@echo "----- $(currentTarget) -----"
	cd package-server && ./package-server.sh $(DB_TAG) $(SKIP_TESTS)

push: push-server ## Build image and push it to Docker registry

push-server: prepare build-server ## Build and push ara-server image to Docker registry
	@echo "----- $(currentTarget) -----"
	cd $(SERVER_IMAGE) && $(DOCKER_PUSH) $(SERVER_TAG)

run: build run-version ## Build ara-server and run ara-db & ara-server images

run-server: prepare build-server init-config ## Build and run ara-server image
	@echo "----- $(currentTarget) -----"
	cd $(SERVER_IMAGE) && ./run-image.sh $(SERVER_TAG) $(SERVER_PORT)

run-version: prepare init-config ## Run pre-existing ara-db & ara-server images with docker-compose (use COMMIT_REF=... to select the version to run)
	@echo "----- $(currentTarget) -----"
	cd ara && ./run-ara.sh $(DB_TAG) $(SERVER_TAG) $(DB_PORT) $(SERVER_PORT) $(DATA_DIR)

%: currentTarget = $@

.PHONY: all prepare build build-db build-server help init-config package-server push push-db push-server run run-db run-server run-version

.DEFAULT_GOAL := help
