package com.decathlon.ara.coreapi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MainClassTests {

    @Test
    void checkMainRun() {
        Assertions.assertDoesNotThrow(() -> CoreApiApplication.main(new String[]{}));
    }
}
