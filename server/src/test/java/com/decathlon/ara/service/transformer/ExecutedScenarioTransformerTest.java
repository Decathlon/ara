package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.enumeration.Handling;
import com.decathlon.ara.service.dto.error.ErrorWithProblemsDTO;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioDTO;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO;
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
public class ExecutedScenarioTransformerTest {

    @Mock
    private ErrorTransformer errorTransformer;

    @Spy
    @InjectMocks
    private ExecutedScenarioTransformer cut;

    @Test
    public void toDto_should_return_executed_scenario() {
        // Given
        Date start = new Date();
        ExecutedScenario value = new ExecutedScenario(
                1L, 57L, null, "FeatureFile", "FeatureName", "FeatureTags",
                "tags", "severity", "name", "cucumberId", 1, "content", start,
                "screenshotUrl", "videoUrl", "logsUrl", "httpRequestsUrl",
                "javaScriptErrorsUrl", "diffReportUrl", "cucumberReportUrl",
                "apiServer", "seleniumNode", Collections.emptySet());
        // When
        ExecutedScenarioDTO result = cut.toDto(value);
        // Then
        Assertions.assertThat(result).isNotNull();
        checkValidScenario(result, start);
    }

    @Test
    public void toDto_should_return_empty_scenario_if_null() {
        // When
        ExecutedScenarioDTO result = this.cut.toDto(null);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(0L);
        checkDefaultValues(result);
    }

    @Test
    public void toFullyDetailledDto_should_return_executed_scenario_with_errors() {
        // Given
        Date start = new Date();
        Set<Error> errors = Sets.newSet(new Error());
        ErrorWithProblemsDTO expectedError = new ErrorWithProblemsDTO();
        expectedError.setId(1L);
        ExecutedScenario value = new ExecutedScenario(
                1L, 57L, null, "FeatureFile", "FeatureName", "FeatureTags",
                "tags", "severity", "name", "cucumberId", 1, "content", start,
                "screenshotUrl", "videoUrl", "logsUrl", "httpRequestsUrl",
                "javaScriptErrorsUrl", "diffReportUrl", "cucumberReportUrl",
                "apiServer", "seleniumNode", errors);
        Mockito.doReturn(Lists.list(expectedError)).when(errorTransformer).toDtos(errors);
        // When
        ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO result = cut.toFullyDetailledDto(value);
        // Then
        Assertions.assertThat(result).isNotNull();
        checkValidScenario(result, start);
        Assertions.assertThat(result.getHandling()).isEqualTo(Handling.UNHANDLED);
        Mockito.verify(errorTransformer).toDtos(errors);
        Assertions.assertThat(result.getErrors()).hasSize(1);
        Assertions.assertThat(result.getErrors().get(0)).isNotNull();
        Assertions.assertThat(result.getErrors().get(0).getId()).isEqualTo(1L);
    }

    @Test
    public void toFullyDetailledDto_should_return_empty_executed_scenario_if_null() {
        // When
        ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO result = cut.toFullyDetailledDto(null);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(0L);
        checkDefaultValues(result);
        Assertions.assertThat(result.getHandling()).isNull();
        Assertions.assertThat(result.getErrors()).isEmpty();
    }

    @Test
    public void toFullyDetailledDtos_should_transform_alls_scenarios() {
        // Given
        List<ExecutedScenario> values = Lists.list(
                new ExecutedScenario(), new ExecutedScenario(), new ExecutedScenario()
        );
        // When
        List<ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO> result = cut.toFullyDetailledDtos(values);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).hasSize(3);
        Mockito.verify(cut, Mockito.times(3)).toFullyDetailledDto(Mockito.any());
        Assertions.assertThat(result.get(0)).isNotNull();
        Assertions.assertThat(result.get(1)).isNotNull();
        Assertions.assertThat(result.get(2)).isNotNull();
    }

    @Test
    public void toFullyDetailledDtos_should_return_empty_list_on_empty_param() {
        // When
        List<ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO> result = cut.toFullyDetailledDtos(new ArrayList<>());
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    public void toFullyDetailledDtos_should_return_empty_list_on_null() {
        // When
        List<ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO> result = cut.toFullyDetailledDtos(null);
        // Then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEmpty();
    }

    private void checkValidScenario(ExecutedScenarioDTO result, Date start) {
        Assertions.assertThat(result.getId()).isEqualTo(1L);
        Assertions.assertThat(result.getFeatureFile()).isEqualTo("FeatureFile");
        Assertions.assertThat(result.getFeatureName()).isEqualTo("FeatureName");
        Assertions.assertThat(result.getFeatureTags()).isEqualTo("FeatureTags");
        Assertions.assertThat(result.getTags()).isEqualTo("tags");
        Assertions.assertThat(result.getSeverity()).isEqualTo("severity");
        Assertions.assertThat(result.getName()).isEqualTo("name");
        Assertions.assertThat(result.getCucumberId()).isEqualTo("cucumberId");
        Assertions.assertThat(result.getLine()).isEqualTo(1);
        Assertions.assertThat(result.getContent()).isEqualTo("content");
        Assertions.assertThat(result.getStartDateTime()).isEqualTo(start);
        Assertions.assertThat(result.getScreenshotUrl()).isEqualTo("screenshotUrl");
        Assertions.assertThat(result.getVideoUrl()).isEqualTo("videoUrl");
        Assertions.assertThat(result.getLogsUrl()).isEqualTo("logsUrl");
        Assertions.assertThat(result.getHttpRequestsUrl()).isEqualTo("httpRequestsUrl");
        Assertions.assertThat(result.getJavaScriptErrorsUrl()).isEqualTo("javaScriptErrorsUrl");
        Assertions.assertThat(result.getDiffReportUrl()).isEqualTo("diffReportUrl");
        Assertions.assertThat(result.getCucumberReportUrl()).isEqualTo("cucumberReportUrl");
        Assertions.assertThat(result.getApiServer()).isEqualTo("apiServer");
        Assertions.assertThat(result.getSeleniumNode()).isEqualTo("seleniumNode");
    }

    private void checkDefaultValues(ExecutedScenarioDTO result) {
        Assertions.assertThat(result.getFeatureFile()).isNull();
        Assertions.assertThat(result.getFeatureName()).isNull();
        Assertions.assertThat(result.getFeatureTags()).isNull();
        Assertions.assertThat(result.getTags()).isNull();
        Assertions.assertThat(result.getSeverity()).isNull();
        Assertions.assertThat(result.getName()).isNull();
        Assertions.assertThat(result.getCucumberId()).isNull();
        Assertions.assertThat(result.getLine()).isEqualTo(0);
        Assertions.assertThat(result.getContent()).isNull();
        Assertions.assertThat(result.getStartDateTime()).isNull();
        Assertions.assertThat(result.getScreenshotUrl()).isNull();
        Assertions.assertThat(result.getVideoUrl()).isNull();
        Assertions.assertThat(result.getLogsUrl()).isNull();
        Assertions.assertThat(result.getHttpRequestsUrl()).isNull();
        Assertions.assertThat(result.getJavaScriptErrorsUrl()).isNull();
        Assertions.assertThat(result.getDiffReportUrl()).isNull();
        Assertions.assertThat(result.getCucumberReportUrl()).isNull();
        Assertions.assertThat(result.getApiServer()).isNull();
        Assertions.assertThat(result.getSeleniumNode()).isNull();
    }
}