package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.Source;
import com.decathlon.ara.domain.Type;
import com.decathlon.ara.service.dto.source.SourceDTO;
import com.decathlon.ara.service.dto.type.TypeWithSourceDTO;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TypeTransformerTest {

    @Mock
    private SourceTransformer sourceTransformer;

    @InjectMocks
    private TypeTransformer cut;

    @Test
    public void toDtoWithSource_should_transform_type() {
        // Given
        Source source = new Source();
        Type value = new Type(1L, 66L, "code", "name", true, true, source);
        Mockito.doReturn(new SourceDTO()).when(sourceTransformer).toDto(source);
        // When
        TypeWithSourceDTO result = cut.toDtoWithSource(value);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getCode()).isEqualTo("code");
        Assertions.assertThat(result.getName()).isEqualTo("name");
        Assertions.assertThat(result.isBrowser()).isTrue();
        Assertions.assertThat(result.isMobile()).isTrue();
        Mockito.verify(sourceTransformer).toDto(source);
    }

    @Test
    public void toDtoWithSource_should_return_empty_object_on_null() {
        // When
        TypeWithSourceDTO result = cut.toDtoWithSource(null);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getCode()).isNull();
        Assertions.assertThat(result.getName()).isNull();
        Assertions.assertThat(result.isBrowser()).isFalse();
        Assertions.assertThat(result.isMobile()).isFalse();
        Mockito.verify(sourceTransformer, Mockito.never()).toDto(Mockito.any());
    }
}