/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

package com.decathlon.ara.scenario.postman.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

public class StreamingHttpMessageConverter<R> implements HttpMessageConverter<R> {

    private final JsonFactory factory;
    private final JsonStreamer<R> jsonStreamer;

    public StreamingHttpMessageConverter(JsonFactory factory, JsonStreamer<R> jsonStreamer) {
        this.factory = factory;
        this.jsonStreamer = jsonStreamer;
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return MediaType.APPLICATION_JSON.isCompatibleWith(mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false; // We only support reading from an InputStream
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Collections.singletonList(MediaType.APPLICATION_JSON);
    }

    @Override
    public R read(Class<? extends R> clazz, HttpInputMessage inputMessage) throws IOException {
        try (InputStream inputStream = inputMessage.getBody();
             JsonParser parser = factory.createParser(inputStream)) {
            return jsonStreamer.stream(parser);
        }
    }

    @Override
    public void write(R result, MediaType contentType, HttpOutputMessage outputMessage) {
        throw new UnsupportedOperationException();
    }

}
