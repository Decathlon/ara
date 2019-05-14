package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.enumeration.Result;
import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.CountryDeployment;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.service.dto.country.CountryDTO;
import com.decathlon.ara.service.dto.countrydeployment.CountryDeploymentDTO;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CountryDeploymentTransformerTest {

    @Mock
    private CountryTransformer countryTransformer;

    @Spy
    @InjectMocks
    private CountryDeploymentTransformer cut;

    @Test
    public void toDto_should_return_transformed_object() {
        // Given
        Country country = new Country(1L, 1L, "FR", "France");
        CountryDTO expectedCountry = new CountryDTO("FR", "France");
        Date start = new Date();
        CountryDeployment value = new CountryDeployment(
                1L, 23L, null,
                country, "ptf", "http://google.test", "/link",
                JobStatus.DONE, Result.FAILURE, start, 1000L, 500L
        );
        Mockito.doReturn(expectedCountry).when(countryTransformer).toDto(country);
        // When
        CountryDeploymentDTO result = cut.toDto(value);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(1L);
        Assertions.assertThat(result.getCountry()).isEqualTo(expectedCountry);
        Assertions.assertThat(result.getPlatform()).isEqualTo("ptf");
        Assertions.assertThat(result.getJobUrl()).isEqualTo("http://google.test");
        Assertions.assertThat(result.getStatus()).isEqualTo(JobStatus.DONE);
        Assertions.assertThat(result.getResult()).isEqualTo(Result.FAILURE);
        Assertions.assertThat(result.getStartDateTime()).isEqualTo(start);
        Assertions.assertThat(result.getEstimatedDuration()).isEqualTo(1000L);
        Assertions.assertThat(result.getDuration()).isEqualTo(500L);
    }

    @Test
    public void toDto_should_return_empty_when_country_deployment_is_null() {
        // When
        CountryDeploymentDTO result = cut.toDto(null);
        // Then
        Assertions.assertThat(result).isNotNull();
        Mockito.verify(countryTransformer, Mockito.never()).toDto(Mockito.any());
        Assertions.assertThat(result.getId()).isEqualTo(0L);
        Assertions.assertThat(result.getCountry()).isNull();
        Assertions.assertThat(result.getPlatform()).isNull();
        Assertions.assertThat(result.getJobUrl()).isNull();
        Assertions.assertThat(result.getStatus()).isNull();
        Assertions.assertThat(result.getResult()).isNull();
        Assertions.assertThat(result.getStartDateTime()).isNull();
        Assertions.assertThat(result.getEstimatedDuration()).isNull();
        Assertions.assertThat(result.getDuration()).isNull();
    }

    @Test
    public void toDtos_should_transform_all() {
        // Given
        List<CountryDeployment> values = Lists.list(
                new CountryDeployment(), new CountryDeployment(), new CountryDeployment()
        );
        // When
        List<CountryDeploymentDTO> result = cut.toDtos(values);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).hasSize(3);
        Mockito.verify(cut, Mockito.times(3)).toDto(Mockito.any());
        Assertions.assertThat(result.get(0)).isNotNull();
        Assertions.assertThat(result.get(1)).isNotNull();
        Assertions.assertThat(result.get(2)).isNotNull();

    }

    @Test
    public void toDtos_should_return_empty_list_on_empty_list() {
        // When
        List<CountryDeploymentDTO> result = cut.toDtos(new ArrayList<>());
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).hasSize(0);
        Mockito.verify(cut, Mockito.never()).toDto(Mockito.any());
    }

    @Test
    public void toDtos_should_return_empty_list_on_null() {
        // When
        List<CountryDeploymentDTO> result = cut.toDtos(null);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).hasSize(0);
        Mockito.verify(cut, Mockito.never()).toDto(Mockito.any());
    }
}