package com.decathlon.ara.ci.util;

import com.fasterxml.jackson.core.JsonParser;
import java.io.IOException;

@FunctionalInterface
public interface JsonParserConsumer {

    void accept(JsonParser jsonParser) throws IOException;

}
