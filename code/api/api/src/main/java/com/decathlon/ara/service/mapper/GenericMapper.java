package com.decathlon.ara.service.mapper;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

/**
 * Generic mapper to map an Object A to an Object B based on java property name.<br/>
 * This mapper use {@link ObjectMapper#convertValue(Object, JavaType)} with custom configuration and custom serializer.<br/>
 * Configuration disable annotation that change the name of the property in serialization/deserialization to only use java property name.
 * Custom serializer only serialize field that will be attempted to be deserialized, to avoid useless serialization.<br/>
 * When using this mapper, to map an Entity to a DTO, if a property OneToMany is defined in the entity and not exist in the DTO, the getter will not be called and no additional request is send to the database. 
 */
@Component
public class GenericMapper {

    @SuppressWarnings("rawtypes")
    private static final NullSupplier NULL_SUPPLIER = new NullSupplier();
    private ObjectMapper objectMapper;

    private ThreadLocal<Deque<JavaType>> typeDequeThreadLocal = new ThreadLocal<>();

    public GenericMapper() {
        objectMapper = Jackson2ObjectMapperBuilder.json().build();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        objectMapper.setAnnotationIntrospector(new IgnorePropertyNameOverloadAnnotationIntrospector());
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new OnlyNecessaryBeanSerializerModifier());
        objectMapper.registerModule(module);
    }

    public <E, F extends Collection<E>, T> List<T> mapCollection(F toMap, Class<T> targetClass) {
        return mapCollection(toMap, targetClass, null, nullSupplier());
    }

    public <E, F extends Collection<E>, T> List<T> mapCollection(F toMap, Class<T> targetClass, BiConsumer<E, T> additionalElementTransformation) {
        return mapCollection(toMap, targetClass, additionalElementTransformation, nullSupplier());
    }


    public <E, F extends Collection<E>, T> List<T> mapCollection(F toMap, Class<T> targetClass, BiConsumer<E, T> additionalElementTransformation, Supplier<List<T>> defaultWhenNullSupplier) {
        if (toMap == null) {
            return defaultWhenNullSupplier.get();
        }
        return toMap.stream().map(elementToMap -> map(elementToMap, targetClass, additionalElementTransformation)).toList();
    }

    public <F, T> T map(F toMap, Class<T> targetClass) {
        return map(toMap, targetClass, null);
    }

    public <F, T> T map(F toMap, TypeReference<T> targetTypeRef) {
        return map(toMap, targetTypeRef, null);
    }

    public <F, T> T map(F toMap, Class<T> targetClass, BiConsumer<F, T> additionalTransformation) {
        return map(toMap, objectMapper.constructType(targetClass), additionalTransformation);
    }

    public <F, T> T map(F toMap, TypeReference<T> targetTypeRef, BiConsumer<F, T> additionalTransformation) {
        return map(toMap, objectMapper.constructType(targetTypeRef), additionalTransformation);
    }

    private <F, T> T map(F toMap, JavaType targetType, BiConsumer<F, T> additionalTransformation) {
        if (toMap == null) {
            return null;
        }
        try {
            Deque<JavaType> typeDeque = new ArrayDeque<>();
            typeDeque.add(targetType);
            typeDequeThreadLocal.set(typeDeque);
            T mappedValue = objectMapper.convertValue(toMap, targetType);
            if (additionalTransformation != null) {
                additionalTransformation.accept(toMap, mappedValue);
            }
            return mappedValue;
        } finally {
            typeDequeThreadLocal.remove();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> NullSupplier<T> nullSupplier() {
        return NULL_SUPPLIER;
    }

    /**
     * JacksonAnnotationIntrospector that force using java property name
     */
    private static class IgnorePropertyNameOverloadAnnotationIntrospector extends JacksonAnnotationIntrospector {

        private static final long serialVersionUID = 1L;

        @Override
        public PropertyName findNameForDeserialization(Annotated a) {
            return null; // to always use java property name
        }

        @Override
        public PropertyName findNameForSerialization(Annotated a) {
            return null; // to always use java property name
        }
    }

    private static class NullSupplier<T> implements Supplier<T> {

        @Override
        public T get() {
            return null;
        }
    }

    /**
     * Custom BeanPropertyWriter that only write field that exists in the target type.
     */
    private class OnlyNecessaryBeanPropertyWriter extends BeanPropertyWriter {

        private static final long serialVersionUID = 1L;

        /**
         * Internal cache for performance
         */
        private final transient Map<JavaType, List<BeanPropertyDefinition>> propertyCache = new HashMap<>();

        public OnlyNecessaryBeanPropertyWriter(BeanPropertyWriter wrapped) {
            super(wrapped);
        }

        @Override
        public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
            Deque<JavaType> typeDeque = typeDequeThreadLocal.get();
            BeanPropertyDefinition targetProperty = null;
            List<BeanPropertyDefinition> properties = getProperties(prov.getConfig(), typeDeque.peek());
            if (properties.isEmpty()) {
                super.serializeAsField(bean, gen, prov);
                return;
            }
            for (BeanPropertyDefinition property : properties) {
                if (getName().equals(property.getName())) {
                    targetProperty = property;
                    break;
                }
            }
            if (targetProperty != null) {
                try {
                    JavaType targetPropertyType = targetProperty.getPrimaryType();
                    if (targetPropertyType.isCollectionLikeType() || targetPropertyType.isMapLikeType()) {
                        targetPropertyType = targetPropertyType.getContentType();
                    }
                    typeDeque.addFirst(targetPropertyType);
                    super.serializeAsField(bean, gen, prov);
                } finally {
                    typeDeque.poll();
                }
            }
        }

        private List<BeanPropertyDefinition> getProperties(SerializationConfig config, JavaType javaType) {
            return propertyCache.computeIfAbsent(javaType, key -> config.introspect(javaType).findProperties());
        }
    }

    private class OnlyNecessaryBeanSerializerModifier extends BeanSerializerModifier {
        @Override
        public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
                                                         List<BeanPropertyWriter> beanProperties) {
            return beanProperties.stream().map(writer -> (BeanPropertyWriter) new OnlyNecessaryBeanPropertyWriter(writer)).toList();
        }
    }

}
