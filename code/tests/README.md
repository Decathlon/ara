# TESTS

This project is used to run test ARA.

It contains:

* Karate tests

Run `make` to display help.

## Initialize the environment

Run the following command to instanciate an ara local environment:

```shell
make start-local
```

You can run tests with:

```shell
make test-karate
```

To clear local environment you can destroy it with:

```shell
make destroy-local
```

You can see application logs with:

```shell
make logs-local
```

You can also specify one service:

```shell
make logs-local SERVICE=ara-api
```

There is actually 4 services you can target:

* ara-api
* ara-db
* ara-web-ui
* oauth2-dev-server

This service correspond to declared service in `../docker-compose.yaml`

To test your current dev package:

in `..`

```shell
make build-api FULL=true
```
