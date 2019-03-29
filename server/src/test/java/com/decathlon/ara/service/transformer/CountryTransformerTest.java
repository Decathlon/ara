package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.Country;
import com.decathlon.ara.service.dto.country.CountryDTO;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CountryTransformerTest {

    @InjectMocks
    private CountryTransformer cut;

    @Test
    public void toDto_should_transform_the_do() {
        // Given
        Country value = new Country(1L, 1L, "FR", "France");
        // When
        CountryDTO result = cut.toDto(value);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getCode()).isEqualTo("FR");
        Assertions.assertThat(result.getName()).isEqualTo("France");
    }

    @Test
    public void toDto_should_return_empty_object_if_do_is_null() {
        // When
        CountryDTO result = cut.toDto(null);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getCode()).isNull();
        Assertions.assertThat(result.getName()).isNull();
    }
}