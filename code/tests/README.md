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
