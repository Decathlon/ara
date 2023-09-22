package com.decathlon.ara.util.builder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Assertions;
import org.springframework.core.GenericTypeResolver;

import com.decathlon.ara.util.TestUtil;

public class GenericTestDataBuilder<T> {

    protected T builded;

    @SuppressWarnings("unchecked")
    public GenericTestDataBuilder() {
        Class<?>[] resolveTypeArguments = GenericTypeResolver.resolveTypeArguments(this.getClass(), GenericTestDataBuilder.class);
        Constructor<T> constructor;
        try {
            constructor = ((Class<T>) resolveTypeArguments[0]).getConstructor();
            builded = constructor.newInstance();
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            Assertions.fail();
            throw new RuntimeException(e);// should not be reached
        }

    }

    protected void setField(String fieldName, Object value) {
        TestUtil.setField(builded, fieldName, value);
    }

    public T build() {
        return builded;
    }

}
