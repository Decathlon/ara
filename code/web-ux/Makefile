#####################################
##            VARIABLES            ##
#####################################

NODE_VERSION=16
FULL_IMAGE?=decathlon/ara-web-ux
VERSION?=$(shell make -s get-version)

#####################################
##             DEVELOP             ##
#####################################

# WITH NODE

npm-install: ## DEV - install project
	@npm install

npm-run: ## DEV - run web-ui
	@npm run dev

#####################################
##             PACKAGE             ##
#####################################

build: ## Build docker image
	@docker build -t $(FULL_IMAGE) .

push: ## Push docker image. You can use VERSION to specify one.
	@docker image tag $(FULL_IMAGE) $(FULL_IMAGE):$(VERSION)
	@docker image push $(FULL_IMAGE):$(VERSION)

#####################################
##              UTILS              ##
#####################################

get-version: ## Get component version
	@cat package.json | jq -r '.version'

update-version: ## Update component version
	@npm version $(VERSION)

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.DEFAULT_GOAL := help
