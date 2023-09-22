package com.decathlon.ara.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@ExtendWith(MockitoExtension.class)
class JsonUtilTest {

    private static class TestMapDeserializer extends JsonUtil.StringToMapDeserializer<String, String> {

        private static final long serialVersionUID = 1L;

        protected TestMapDeserializer() {
            super(new TypeReference<Map<String, String>>() {
            });
        }

    }

    private static class TestListDeserializer extends JsonUtil.StringToListDeserializer<String> {

        private static final long serialVersionUID = 1L;

        protected TestListDeserializer() {
            super(new TypeReference<List<String>>() {
            });
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class TestPOJO {

        @JsonDeserialize(using = TestMapDeserializer.class)
        private Map<String, String> map;
        @JsonDeserialize(using = TestListDeserializer.class)
        private List<String> list;

        public Map<String, String> getMap() {
            return map;
        }

        public List<String> getList() {
            return list;
        }

    }

    @Test
    void shouldSuccessToParseNaturallySerializedMap() throws JsonProcessingException {
        String json = "{\"map\":{\"a\":\"b\",\"c\":\"d\"}}";
        TestPOJO parsedObject = JsonUtil.parse(json, TestPOJO.class);
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("a", "b");
        expectedMap.put("c", "d");
        Assertions.assertEquals(expectedMap, parsedObject.getMap());
        Assertions.assertEquals(json, JsonUtil.toString(parsedObject));
    }

    @Test
    void shouldSuccessToParseStringContainingJsonOfMap() throws JsonProcessingException {
        String json = "{\"map\":\"{\\\"a\\\":\\\"b\\\",\\\"c\\\":\\\"d\\\"}\"}";
        TestPOJO parsedObject = JsonUtil.parse(json, TestPOJO.class);
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("a", "b");
        expectedMap.put("c", "d");
        Assertions.assertEquals(expectedMap, parsedObject.getMap());
    }

    @Test
    void shouldSuccessToParseNaturallySerializedList() throws JsonProcessingException {
        String json = "{\"list\":[\"a\",\"b\"]}";
        TestPOJO parsedObject = JsonUtil.parse(json, TestPOJO.class);
        List<String> expectedList = new ArrayList<>();
        expectedList.add("a");
        expectedList.add("b");
        Assertions.assertEquals(expectedList, parsedObject.getList());
        Assertions.assertEquals(json, JsonUtil.toString(parsedObject));
    }

    @Test
    void shouldSuccessToParseStringContainingJsonOfList() throws JsonProcessingException {
        String json = "{\"list\":\"[\\\"a\\\",\\\"b\\\"]\"}";
        TestPOJO parsedObject = JsonUtil.parse(json, TestPOJO.class);
        List<String> expectedList = new ArrayList<>();
        expectedList.add("a");
        expectedList.add("b");
        Assertions.assertEquals(expectedList, parsedObject.getList());
    }

}
