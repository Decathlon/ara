package com.decathlon.ara.postman.support;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

@AllArgsConstructor
public class StreamingHttpMessageConverter<R> implements HttpMessageConverter<R> {

    private final JsonFactory factory;
    private final JsonStreamer<R> jsonStreamer;

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
