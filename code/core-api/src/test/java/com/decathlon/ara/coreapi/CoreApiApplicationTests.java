package com.decathlon.ara.coreapi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CoreApiApplicationTests {

    @Value("${info.app.name}")
    private String appName;

    @Test
    void contextLoads() {
        Assertions.assertEquals("ARA core-api", appName);
    }

}
