function fn() {
    karate.log('Karate config start reading...', karate.env); // get java system property 'karate.env'

    // don't waste time waiting for a connection or if servers don't respond within 5 seconds
    karate.configure('connectTimeout', 5000);
    karate.configure('readTimeout', 5000);
}
