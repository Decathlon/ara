package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.domain.Type;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.service.dto.country.CountryDTO;
import com.decathlon.ara.service.dto.run.RunDTO;
import com.decathlon.ara.service.dto.run.RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO;
import com.decathlon.ara.service.dto.type.TypeWithSourceDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RunTransformerTest {

    @Mock
    private CountryTransformer countryTransformer;

    @Mock
    private TypeTransformer typeTransformer;

    @Mock
    private ExecutedScenarioTransformer executedScenarioTransformer;

    @Spy
    @InjectMocks
    private RunTransformer cut;

    @Test
    public void toDto_should_transform_object() {
        // Given
        Country country = new Country(1L, 12L, "FR", "France");
        Type type = new Type(78L, 12L, "type", "TYPE", true, false, null);
        Date start = new Date();
        Run value = new Run(1L, 12L, null, country, type, "comment", "platform",
                "jobUrl", "jobLink", JobStatus.DONE, "FR", start, 1000L,
                100L, "tags", true, Collections.emptySet());
        Mockito.doReturn(new CountryDTO()).when(countryTransformer).toDto(country);
        Mockito.doReturn(new TypeWithSourceDTO()).when(typeTransformer).toDtoWithSource(type);
        // When
        RunDTO result = cut.toDto(value);
        // Then
        Assertions.assertThat(result).isNotNull();
        checkValidRunDTO(result, country, type, start);
    }

    @Test
    public void toDto_should_return_empty_object_on_null() {
        // When
        RunDTO result = cut.toDto(null);
        // Then
        Assertions.assertThat(result).isNotNull();
        checkDefaultRunDTO(result);
    }

    @Test
    public void toFullyDetailledDto_should_tranform_the_object() {
        // Given
        Country country = new Country(1L, 12L, "FR", "France");
        Type type = new Type(78L, 12L, "type", "TYPE", true, false, null);
        Date start = new Date();
        Set<ExecutedScenario> scenarios = Sets.newSet(new ExecutedScenario(), new ExecutedScenario());
        Run value = new Run(1L, 12L, null, country, type, "comment", "platform",
                "jobUrl", "jobLink", JobStatus.DONE, "FR", start, 1000L,
                100L, "tags", true, scenarios);
        Mockito.doReturn(new CountryDTO()).when(countryTransformer).toDto(country);
        Mockito.doReturn(new TypeWithSourceDTO()).when(typeTransformer).toDtoWithSource(type);
        Mockito.doReturn(new ArrayList<>()).when(executedScenarioTransformer).toFullyDetailledDtos(scenarios);
        // When
        RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO result = cut.toFullyDetailledDto(value);
        // Then
        Assertions.assertThat(result).isNotNull();
        checkValidRunDTO(result, country, type, start);
        Mockito.verify(executedScenarioTransformer).toFullyDetailledDtos(scenarios);
        Assertions.assertThat(result.getExecutedScenarios()).isNotNull();
    }

    @Test
    public void toFullyDetailledDto_should_return_empty_object_on_null() {
        // When
        RunDTO result = cut.toFullyDetailledDto(null);
        // Then
        Assertions.assertThat(result).isNotNull();
        checkDefaultRunDTO(result);
        Mockito.verify(executedScenarioTransformer, Mockito.never()).toFullyDetailledDtos(Mockito.anyCollection());
    }

    @Test
    public void toFullyDetailledDtos_should_transform_all() {
        // Given
        List<Run> runs = Lists.list(new Run(), new Run(), new Run());
        // When
        List<RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO> result = cut.toFullyDetailledDtos(runs);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).hasSize(3);
        Mockito.verify(cut, Mockito.times(3)).toFullyDetailledDto(Mockito.any());
    }

    @Test
    public void toFullyDetailledDtos_should_return_empty_list_on_empty_list() {
        // When
        List<RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO> result = cut.toFullyDetailledDtos(new ArrayList<>());
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    public void toFullyDetailledDtos_should_return_empty_list_on_null() {
        // When
        List<RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO> result = cut.toFullyDetailledDtos(null);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEmpty();
    }

    private void checkValidRunDTO(RunDTO result, Country country, Type type, Date start) {
        Assertions.assertThat(result.getId()).isEqualTo(1L);
        Mockito.verify(countryTransformer).toDto(country);
        Mockito.verify(typeTransformer).toDtoWithSource(type);
        Assertions.assertThat(result.getComment()).isEqualTo("comment");
        Assertions.assertThat(result.getPlatform()).isEqualTo("platform");
        Assertions.assertThat(result.getJobUrl()).isEqualTo("jobUrl");
        Assertions.assertThat(result.getStatus()).isEqualTo(JobStatus.DONE);
        Assertions.assertThat(result.getCountryTags()).isEqualTo("FR");
        Assertions.assertThat(result.getSeverityTags()).isEqualTo("tags");
        Assertions.assertThat(result.getIncludeInThresholds()).isEqualTo(true);
        Assertions.assertThat(result.getStartDateTime()).isEqualTo(start);
        Assertions.assertThat(result.getEstimatedDuration()).isEqualTo(1000L);
        Assertions.assertThat(result.getDuration()).isEqualTo(100L);
    }

    private void checkDefaultRunDTO(RunDTO result) {
        Assertions.assertThat(result.getId()).isEqualTo(0L);
        Mockito.verify(countryTransformer, Mockito.never()).toDto(Mockito.any());
        Mockito.verify(typeTransformer, Mockito.never()).toDtoWithSource(Mockito.any());
        Assertions.assertThat(result.getComment()).isNull();
        Assertions.assertThat(result.getPlatform()).isNull();
        Assertions.assertThat(result.getJobUrl()).isNull();
        Assertions.assertThat(result.getStatus()).isNull();
        Assertions.assertThat(result.getCountryTags()).isNull();
        Assertions.assertThat(result.getSeverityTags()).isNull();
        Assertions.assertThat(result.getIncludeInThresholds()).isNull();
        Assertions.assertThat(result.getStartDateTime()).isNull();
        Assertions.assertThat(result.getEstimatedDuration()).isNull();
        Assertions.assertThat(result.getDuration()).isNull();
    }

}