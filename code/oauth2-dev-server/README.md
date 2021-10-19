# oauth2-dev-server

This project may be used for dev only to allow oauth2 authentication with all components.

This project use `java` version `17` which is an **LTS** version

You can install this project with:

```shell
./mvnw clean install
```

You can run it with:

```
./mvnw spring-boot:run
```

**AND** add to `/etc/hosts` (Linux OS) or to `/private/etc/hosts` (MacOS) `127.0.0.1 oauth2-dev-server`

Use:

* port: `10 000`
* management port: `10 001`

## With Makefile

`make` or `make help` to display available command and a short description.

## Configuration

This is a spring boot project to mock call for oauth2.

It contains parameters to lightly configure this component:

|ENV VAR|Description|Default value|
|-|-|-|
|SERVER_PORT|The server port of this Oauth2 authorization server|10000|
|PROVIDER_HOSTNAME|The provider hostname for OIDC|oauth2-dev-server|
|CLIENT_HOSTNAME|The client hostname used for redirect uri|127.0.0.1|
CLIENT_PORT|The client port used for redirect uri|9000|

Notice: the client id and client secret is `ara-gw` for both

