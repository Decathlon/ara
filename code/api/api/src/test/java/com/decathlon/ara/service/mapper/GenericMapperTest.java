package com.decathlon.ara.service.mapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.util.TestUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class GenericMapperTest {

    private GenericMapper mapper = new GenericMapper();

    private static class From {
        @JsonProperty("modified_field_a")
        private String fieldA;
        private String fieldB;
        private String fieldC;
        private List<FromElement> elements;
        private Map<String, FromElement> elementMap;

        public String getFieldA() {
            return fieldA;
        }

        public String getFieldB() {
            return fieldB;
        }

        public String getFieldC() {
            return fieldC;
        }

        public List<FromElement> getElements() {
            return elements;
        }

        public Map<String, FromElement> getElementMap() {
            return elementMap;
        }
    }

    private static class FromElement {
        private String elementFieldA;
        private String elementFieldB;
        private String elementFieldC;
        private FromElementElement elementWithNoMapping;

        @SuppressWarnings("unused") //used by reflection
        public String getElementFieldA() {
            return elementFieldA;
        }

        @SuppressWarnings("unused") //used by reflection
        public String getElementFieldB() {
            return elementFieldB;
        }

        public String getElementFieldC() {
            return elementFieldC;
        }

        @SuppressWarnings("unused") //used by reflection
        public FromElementElement getElementWithNoMapping() {
            return elementWithNoMapping;
        }

    }

    private static class FromElementElement {
        private String fieldE;

        @SuppressWarnings("unused") //used by reflection
        public String getFieldE() {
            return fieldE;
        }
    }

    private static class To {
        private String fieldA;
        @JsonProperty("modified_field_b")
        private String fieldB;
        private String fieldZ;
        private List<ToElement> elements;
        private Map<String, ToElement> elementMap;

        public String getFieldA() {
            return fieldA;
        }

        public String getFieldB() {
            return fieldB;
        }

        public String getFieldZ() {
            return fieldZ;
        }

        public List<ToElement> getElements() {
            return elements;
        }

        public Map<String, ToElement> getElementMap() {
            return elementMap;
        }
    }

    private static class ToElement {
        private String elementFieldC;
        private String elementFieldZ;

        public String getElementFieldC() {
            return elementFieldC;
        }

        public String getElementFieldZ() {
            return elementFieldZ;
        }
    }

    private From contructFrom(String propertyBaseValue, int elementSize, String... elementMapKeys) {
        int i = 0;
        From from = new From();
        from.fieldA = propertyBaseValue + i++;
        from.fieldB = propertyBaseValue + i++;
        from.fieldC = propertyBaseValue + i++;
        from.elements = new ArrayList<>();
        for (int j = 0; j < elementSize; j++) {
            FromElement element = new FromElement();
            element.elementFieldA = propertyBaseValue + i++;
            element.elementFieldB = propertyBaseValue + i++;
            element.elementFieldC = propertyBaseValue + i++;
            element.elementWithNoMapping = new FromElementElement();
            element.elementWithNoMapping.fieldE = propertyBaseValue + i++;
            from.elements.add(element);
        }
        if (elementMapKeys != null) {
            from.elementMap = new HashMap<>();
            for (String elementMapKey : elementMapKeys) {
                FromElement element = new FromElement();
                element.elementFieldA = propertyBaseValue + i++;
                element.elementFieldB = propertyBaseValue + i++;
                element.elementFieldC = propertyBaseValue + i++;
                element.elementWithNoMapping = new FromElementElement();
                element.elementWithNoMapping.fieldE = propertyBaseValue + i++;
                from.elementMap.put(elementMapKey, element);
            }
        }
        return from;
    }

    @Test
    void shouldReturnNullWhenMapIsCalledWithNullObject() {
        Object mapped = mapper.map(null, Object.class);
        Assertions.assertNull(mapped);
    }

    @Test
    void shouldReturnNullWhenMapCollectionIsCalledWithNullObjectWithoutSupplier() {
        List<?> mapped = mapper.mapCollection(null, Object.class);
        Assertions.assertNull(mapped);
    }

    @Test
    void shouldReturnSupplierResultWhenMapCollectionIsCalledWithNullObjectWithSupplier() {
        List<?> mapped = mapper.mapCollection(null, Object.class, null, Collections::emptyList);
        Assertions.assertEquals(Collections.emptyList(), mapped);
    }

    @Test
    void shouldIgnoreAnnotationThatChangePropertyNameAndMapFieldByJavaPropertyName() {
        From from = contructFrom("a", 0);
        from.fieldA = "a";
        from.fieldB = "b";
        To mapped = mapper.map(from, To.class);
        Assertions.assertEquals(from.getFieldA(), mapped.getFieldA());
        Assertions.assertEquals(from.getFieldB(), mapped.getFieldB());
    }

    @Test
    void shouldMapAllCorrespondingFieldWhenTargetTypeHasSomeField() {
        From from = contructFrom("a", 2, "toto", "titi");
        To mapped = mapper.map(from, To.class);
        Assertions.assertEquals(from.getFieldA(), mapped.getFieldA());
        Assertions.assertEquals(from.getFieldB(), mapped.getFieldB());
        Assertions.assertNull(mapped.getFieldZ());
        Assertions.assertEquals(from.getElements().size(), mapped.getElements().size());
        Assertions.assertEquals(from.getElements().get(0).getElementFieldC(), mapped.getElements().get(0).getElementFieldC());
        Assertions.assertNull(mapped.getElements().get(0).getElementFieldZ());
        Assertions.assertEquals(from.getElements().get(1).getElementFieldC(), mapped.getElements().get(1).getElementFieldC());
        Assertions.assertNull(mapped.getElements().get(1).getElementFieldZ());
        Assertions.assertEquals(from.getElementMap().size(), mapped.getElementMap().size());
        Assertions.assertEquals(from.getElementMap().get("toto").getElementFieldC(), mapped.getElementMap().get("toto").getElementFieldC());
        Assertions.assertNull(mapped.getElementMap().get("toto").getElementFieldZ());
        Assertions.assertEquals(from.getElementMap().get("titi").getElementFieldC(), mapped.getElementMap().get("titi").getElementFieldC());
        Assertions.assertNull(mapped.getElementMap().get("titi").getElementFieldZ());
    }

    @Test
    void shouldApplyAdditionalTransformationAfterMapping() {
        From from = contructFrom("a", 0);
        To mapped = mapper.map(from, To.class, (toMap, to) -> to.fieldA = "b");
        Assertions.assertEquals("b", mapped.getFieldA());
        Assertions.assertEquals(from.getFieldB(), mapped.getFieldB());
        Assertions.assertNull(mapped.getFieldZ());
    }

    @Test
    void shouldApplyAdditionalTransformationOnEachElementForMapCollections() {
        From from = contructFrom("a", 2);
        List<ToElement> mapped = mapper.mapCollection(from.getElements(), ToElement.class, (toMap, to) -> to.elementFieldZ = toMap.elementFieldA + "b");
        Assertions.assertEquals(from.getElements().size(), mapped.size());
        Assertions.assertEquals(from.getElements().get(0).getElementFieldC(), mapped.get(0).getElementFieldC());
        Assertions.assertEquals(from.getElements().get(0).getElementFieldA() + "b", mapped.get(0).getElementFieldZ());
        Assertions.assertEquals(from.getElements().get(1).getElementFieldC(), mapped.get(1).getElementFieldC());
        Assertions.assertEquals(from.getElements().get(1).getElementFieldA() + "b", mapped.get(1).getElementFieldZ());
    }

    @Test
    void shouldWriteAllFieldWhenTargetTypeHasNoField() {
        From from = contructFrom("a", 2, "toto", "titi");
        Map<String, Object> mapped = mapper.map(from, new TypeReference<Map<String, Object>>() {
        });
        Map<String, Object> expected = Map.of("fieldA", "a0", "fieldB", "a1", "fieldC", "a2", "elements",
                List.of(Map.of("elementFieldA", "a3", "elementFieldB", "a4", "elementFieldC", "a5", "elementWithNoMapping", Map.of("fieldE", "a6")),
                        Map.of("elementFieldA", "a7", "elementFieldB", "a8", "elementFieldC", "a9", "elementWithNoMapping", Map.of("fieldE", "a10"))),
                "elementMap", Map.of("toto", Map.of("elementFieldA", "a11", "elementFieldB", "a12", "elementFieldC", "a13", "elementWithNoMapping", Map.of("fieldE", "a14")),
                        "titi", Map.of("elementFieldA", "a15", "elementFieldB", "a16", "elementFieldC", "a17", "elementWithNoMapping", Map.of("fieldE", "a18"))));
        Assertions.assertEquals(expected, mapped);
    }

    @Test
    void shouldNotWritePropertyWithNoCorrespondingPropertyInTargetType() throws JsonProcessingException {
        From from = contructFrom("a", 2, "toto", "titi");
        ObjectMapper objectMapper = TestUtil.getField(mapper, "objectMapper");
        ThreadLocal<Deque<JavaType>> typeDequeThreadLocal = TestUtil.getField(mapper, "typeDequeThreadLocal");
        Deque<JavaType> typeDeque = new ArrayDeque<>();
        typeDeque.add(objectMapper.constructType(To.class));
        typeDequeThreadLocal.set(typeDeque);
        Assertions.assertEquals("{\"fieldA\":\"a0\",\"fieldB\":\"a1\",\"elements\":[{\"elementFieldC\":\"a5\"},{\"elementFieldC\":\"a9\"}],\"elementMap\":{\"toto\":{\"elementFieldC\":\"a13\"},\"titi\":{\"elementFieldC\":\"a17\"}}}", objectMapper.writeValueAsString(from));
    }

}
