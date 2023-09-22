function fn() {
    var config = { // base config JSON
        araBaseUrl: "http://127.0.0.1:7000",
        authToken: "b3RoZXItY2xpZW50Om90aGVyLWNsaWVudA==",
        authBaseUrl: "http://localhost:9000/oauth2/token"
    };

    karate.log('Karate config is:', config);

    return config;
}
