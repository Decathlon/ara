package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.RootCause;
import com.decathlon.ara.service.dto.rootcause.RootCauseDTO;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RootCauseTransformerTest {

    @InjectMocks
    private RootCauseTransformer cut;

    @Test
    public void toDto_should_transform_object() {
        // Given
        RootCause value = new RootCause(1L, 23L, "name", null);
        // When
        RootCauseDTO rootCauseDTO = cut.toDto(value);
        // Then
        Assertions.assertThat(rootCauseDTO).isNotNull();
        Assertions.assertThat(rootCauseDTO.getId()).isEqualTo(1L);
        Assertions.assertThat(rootCauseDTO.getName()).isEqualTo("name");
    }

    @Test
    public void toDto_should_return_empty_object_on_null_value() {
        // When
        RootCauseDTO rootCauseDTO = cut.toDto(null);
        // Then
        Assertions.assertThat(rootCauseDTO).isNotNull();
        Assertions.assertThat(rootCauseDTO.getId()).isEqualTo(0L);
        Assertions.assertThat(rootCauseDTO.getName()).isNull();
    }
}