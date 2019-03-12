package com.decathlon.ara.postman.support;

import com.fasterxml.jackson.core.JsonParser;
import java.io.IOException;

@FunctionalInterface
public interface JsonStreamer<R> {

    /**
     * Parse the given JSON stream, process it, and optionally return an object.<br>
     * The returned object can represent a downsized parsed version of the stream, or the result of a map/reduce processing, or null...
     *
     * @param jsonParser the parser to use while streaming JSON for processing
     * @return the optional result of the process (can be {@link Void} if processing returns nothing)
     * @throws IOException on streaming problem (you are also strongly encouraged to throw HttpMessageNotReadableException on parsing error)
     */
    R stream(JsonParser jsonParser) throws IOException;

}
