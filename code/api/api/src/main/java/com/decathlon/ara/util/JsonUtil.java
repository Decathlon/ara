package com.decathlon.ara.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class JsonUtil {

    private static final ObjectMapper MAPPER = Jackson2ObjectMapperBuilder.json().build();

    private JsonUtil() {
    }

    public static <T> T parse(String json, Class<T> target) throws JsonProcessingException {
        return MAPPER.readValue(json, target);
    }
    
    public static <T> T parse(String json, TypeReference<T> typeRef) throws JsonProcessingException {
        return MAPPER.readValue(json, typeRef);
    }

    public static String toString(Object object) throws JsonProcessingException {
        return MAPPER.writeValueAsString(object);
    }

    public abstract static class StringToMapDeserializer<K, V> extends InnerStringDeserializer<Map<K, V>> {

        private static final long serialVersionUID = 1L;

        protected StringToMapDeserializer(TypeReference<Map<K, V>> typeReference) {
            super(typeReference, JsonToken.START_OBJECT);
        }
    }

    public abstract static class StringToListDeserializer<T> extends InnerStringDeserializer<List<T>> {

        private static final long serialVersionUID = 1L;

        protected StringToListDeserializer(TypeReference<List<T>> typeReference) {
            super(typeReference, JsonToken.START_ARRAY);
        }
    }

    /**
     * StdDeserializer that allow conversion from String to T. <br/>
     * By default, when a String is attempted to be deserialized to an Object,
     * the deserializer check if a constructor with a String argument exist and use it, 
     * an exception is thrown when the constructor does'nt exist.<br/>
     * This deserializer check if the string is a json representation of the target type,
     * and delegate to the default deserializer. 
     * @param <T>
     */
    private abstract static class InnerStringDeserializer<T> extends StdDeserializer<T> {

        private static final long serialVersionUID = 1L;

        private JsonToken expectedToken;

        protected InnerStringDeserializer(TypeReference<T> typeReference, JsonToken expectedToken) {
            super(MAPPER.constructType(typeReference));
            this.expectedToken = expectedToken;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonDeserializer<Object> defaultDeserializer = ctxt.findRootValueDeserializer(_valueType);
            if (p.currentToken() == JsonToken.VALUE_STRING) {
                try (JsonParser innerParser = MAPPER.createParser(p.getValueAsString())) {
                    if (innerParser.nextToken() == expectedToken) {
                        return (T) defaultDeserializer.deserialize(innerParser, ctxt);
                    }
                }
            }
            return (T) defaultDeserializer.deserialize(p, ctxt);
        }

    }

}
