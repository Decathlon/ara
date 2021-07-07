#####################################
##            VARIABLES            ##
#####################################

NODE_VERSION=12
FULL_IMAGE?=decathlon/ara-web-ui

#####################################
##             DEVELOP             ##
#####################################

# WITH NODE

npm-install: ## DEV - install project
	@npm install

npm-run: ## DEV - run web-ui
	@npm run dev

# WITH DOCKER

dkr-install: ## DEV - install project with docker
	@docker container run -it --rm \
				--user $(shell id -u):$(shell id -g) \
        -v $${HOME}/.ara-web:/var/node:rw \
        -e HOME=/var/node \
        -e NPM_CONFIG_PREFIX=/var/node/.npm-global \
				-v $(shell pwd):$(shell pwd):rw \
				-w $(shell pwd) \
		node:$(NODE_VERSION) \
		npm install

dkr-run: ## DEV - run web-ui with docker
	@docker container run -it --name ara-web-ui --rm \
        -p 8081:8081 \
				--user $(shell id -u):$(shell id -g) \
        -v $${HOME}/.ara-web:/var/node:rw \
        -e HOME=/var/node \
        -e NPM_CONFIG_PREFIX=/var/node/.npm-global \
        -e ARA_API_HOST=host.docker.internal \
        -e ARA_API_PORT=8080 \
				-v $(shell pwd):$(shell pwd):rw \
				-w $(shell pwd) \
		node:$(NODE_VERSION) \
		npm run dev

dkr-shell: ## Start a shell in docker node container
	@docker container run -it --rm --name ara-node-$(NODE_VERSION)  \
			-p 8081:8081 \
			--user $(shell id -u):$(shell id -g) \
			-v $${HOME}/.ara-web:/var/node:rw \
			-e HOME=/var/node \
			-e NPM_CONFIG_PREFIX=/var/node/.npm-global \
			-e ARA_API_HOST=host.docker.internal \
      -e ARA_API_PORT=8080 \
			-v $(shell pwd):$(shell pwd):rw \
      -w $(shell pwd) \
      node:$(NODE_VERSION) \
      /bin/sh

dkr-build: ## DEV - build web-ui with docker
	@docker container run -it --rm \
   				--user $(shell id -u):$(shell id -g) \
           -v $${HOME}/.ara-web:/var/node:rw \
           -e HOME=/var/node \
           -e NPM_CONFIG_PREFIX=/var/node/.npm-global \
   				-v $(shell pwd):$(shell pwd):rw \
   				-w $(shell pwd) \
		node:$(NODE_VERSION) \
		npm run build

#####################################
##             PACKAGE             ##
#####################################

dkr-build-image:
	@docker build -t $(FULL_IMAGE) .

#####################################
##              UTILS              ##
#####################################

get-version:
	@cat package.json | jq -r '.version'

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.DEFAULT_GOAL := help
