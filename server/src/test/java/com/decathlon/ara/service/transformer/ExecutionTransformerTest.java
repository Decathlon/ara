package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.enumeration.Result;
import com.decathlon.ara.domain.CountryDeployment;
import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.domain.enumeration.ExecutionAcceptance;
import com.decathlon.ara.domain.enumeration.JobStatus;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.service.dto.execution.ExecutionDTO;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionTransformerTest {

    @Mock
    private QualityThresholdTransformer qualityThresholdTransformer;

    @Mock
    private QualitySeverityTransformer qualitySeverityTransformer;

    @Mock
    private CountryDeploymentTransformer countryDeploymentTransformer;

    @Mock
    private RunTransformer runTransformer;

    @InjectMocks
    private ExecutionTransformer cut;

    @Test
    public void toDto_should_transform_the_do() {
        // Given
        Date build = new Date();
        Date test = new Date(build.getTime() + 1000L);
        Set<Run> runs = Sets.newSet(new Run());
        Set<CountryDeployment> countries = Sets.newSet(new CountryDeployment());
        Execution value = new Execution(1L, "branch", "name", "release", "version", build,
                test, "jobUrl", "jobLink", JobStatus.PENDING, Result.ABORTED, ExecutionAcceptance.ACCEPTED,
                "discardReason", null, true, "qualityThresholds", QualityStatus.FAILED, "qualitySeverities", 10L,
                100L, runs, countries);
        Mockito.doReturn(new HashMap<>()).when(qualityThresholdTransformer).toMap("qualityThresholds");
        Mockito.doReturn(new ArrayList<>()).when(qualitySeverityTransformer).toDtos("qualitySeverities");
        // When
        ExecutionDTO result = cut.toDto(value);
        // Then
        Assertions.assertThat(result).isNotNull();
        checkValidExecutionDto(result, build, test);
    }

    @Test
    public void toDto_should_return_empty_dto_on_null_value() {
        // When
        ExecutionDTO result = cut.toDto(null);
        // Then
        Assertions.assertThat(result).isNotNull();
        checkDefaultExecutionDto(result);
    }

    @Test
    public void toFullyDetailledDto_should_transform_the_do() {
        // Given
        Date build = new Date();
        Date test = new Date(build.getTime() + 1000L);
        Set<Run> runs = Sets.newSet(new Run());
        Set<CountryDeployment> countries = Sets.newSet(new CountryDeployment());
        Execution value = new Execution(1L, "branch", "name", "release", "version", build,
                test, "jobUrl", "jobLink", JobStatus.PENDING, Result.ABORTED, ExecutionAcceptance.ACCEPTED,
                "discardReason", null, true, "qualityThresholds", QualityStatus.FAILED, "qualitySeverities", 10L,
                100L, runs, countries);
        Mockito.doReturn(new HashMap<>()).when(qualityThresholdTransformer).toMap("qualityThresholds");
        Mockito.doReturn(new ArrayList<>()).when(qualitySeverityTransformer).toDtos("qualitySeverities");
        Mockito.doReturn(new ArrayList<>()).when(countryDeploymentTransformer).toDtos(countries);
        Mockito.doReturn(new ArrayList<>()).when(runTransformer).toFullyDetailledDtos(runs);
        // When
        ExecutionDTO result = cut.toFullyDetailledDto(value);
        // Then
        Assertions.assertThat(result).isNotNull();
        checkValidExecutionDto(result, build, test);
        Mockito.verify(countryDeploymentTransformer).toDtos(countries);
        Mockito.verify(runTransformer).toFullyDetailledDtos(runs);
    }

    @Test
    public void toFullyDetailledDto_should_return_empty_dto_on_null_value() {
        // When
        ExecutionDTO result = cut.toFullyDetailledDto(null);
        // Then
        Assertions.assertThat(result).isNotNull();
        checkDefaultExecutionDto(result);
        Mockito.verify(countryDeploymentTransformer, Mockito.never()).toDtos(Mockito.anyCollection());
        Mockito.verify(runTransformer, Mockito.never()).toFullyDetailledDtos(Mockito.anyCollection());
    }

    private void checkValidExecutionDto(ExecutionDTO result, Date build, Date test) {
        Assertions.assertThat(result.getId()).isEqualTo(1L);
        Assertions.assertThat(result.getBranch()).isEqualTo("branch");
        Assertions.assertThat(result.getName()).isEqualTo("name");
        Assertions.assertThat(result.getRelease()).isEqualTo("release");
        Assertions.assertThat(result.getVersion()).isEqualTo("version");
        Assertions.assertThat(result.getBuildDateTime()).isEqualTo(build);
        Assertions.assertThat(result.getTestDateTime()).isEqualTo(test);
        Assertions.assertThat(result.getJobUrl()).isEqualTo("jobUrl");
        Assertions.assertThat(result.getStatus()).isEqualTo(JobStatus.PENDING);
        Assertions.assertThat(result.getResult()).isEqualTo(Result.ABORTED);
        Assertions.assertThat(result.getAcceptance()).isEqualTo(ExecutionAcceptance.ACCEPTED);
        Assertions.assertThat(result.getDiscardReason()).isEqualTo("discardReason");
        Assertions.assertThat(result.isBlockingValidation()).isTrue();
        Mockito.verify(qualityThresholdTransformer).toMap("qualityThresholds");
        Assertions.assertThat(result.getQualityStatus()).isEqualTo(QualityStatus.FAILED);
        Mockito.verify(qualitySeverityTransformer).toDtos("qualitySeverities");
        Assertions.assertThat(result.getDuration()).isEqualTo(10L);
        Assertions.assertThat(result.getEstimatedDuration()).isEqualTo(100L);
    }

    private void checkDefaultExecutionDto(ExecutionDTO result) {
        Assertions.assertThat(result.getId()).isEqualTo(0L);
        Assertions.assertThat(result.getBranch()).isNull();
        Assertions.assertThat(result.getName()).isNull();
        Assertions.assertThat(result.getRelease()).isNull();
        Assertions.assertThat(result.getVersion()).isNull();
        Assertions.assertThat(result.getBuildDateTime()).isNull();
        Assertions.assertThat(result.getTestDateTime()).isNull();
        Assertions.assertThat(result.getJobUrl()).isNull();
        Assertions.assertThat(result.getStatus()).isNull();
        Assertions.assertThat(result.getResult()).isNull();
        Assertions.assertThat(result.getAcceptance()).isNull();
        Assertions.assertThat(result.getDiscardReason()).isNull();
        Assertions.assertThat(result.isBlockingValidation()).isFalse();
        Mockito.verify(qualityThresholdTransformer, Mockito.never()).toMap(Mockito.anyString());
        Assertions.assertThat(result.getQualityStatus()).isNull();
        Mockito.verify(qualitySeverityTransformer, Mockito.never()).toDtos(Mockito.anyString());
        Assertions.assertThat(result.getDuration()).isNull();
        Assertions.assertThat(result.getEstimatedDuration()).isNull();
    }
}