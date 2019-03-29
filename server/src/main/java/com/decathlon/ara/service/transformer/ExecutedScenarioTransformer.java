package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioDTO;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service provide transformation utilities (DTO - DO and DO - DTO) for the ExecutedScenario.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.0.1
 */
@Service
class ExecutedScenarioTransformer {

    @Autowired
    private ErrorTransformer errorTransformer;

    /**
     * Transform the given ExecutedScenario DO to a ExecutedScenarioDTO object.
     * <p>
     * Returns an empty ExecutedScenarioDTO if the parameter is null.
     *
     * @param scenario the DO to transform
     * @return the result DTO.
     */
    ExecutedScenarioDTO toDto(ExecutedScenario scenario) {
        ExecutedScenarioDTO result = new ExecutedScenarioDTO();
        result.setId(0L);
        if (null != scenario) {
            this.fillDto(result, scenario);
        }
        return result;
    }

    /**
     * Transform the given ExecutedScenario DO to a ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO object.
     *
     * Returns an empty ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO if the parameter is null.
     *
     * @param scenario the DO to transform
     * @return the result DTO.
     */
    ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO toFullyDetailledDto(ExecutedScenario scenario) {
        ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO result = new ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO();
        result.setId(0L);
        if (null != scenario) {
            this.fillDto(result, scenario);
            result.setHandling(scenario.getHandling());
            result.setErrors(errorTransformer.toDtos(scenario.getErrors()));
        }
        return result;
    }

    /**
     * Transform the given list of ExecutedScenario DO to a list of ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO.
     * <p>
     * Returns an empty list if the parameter is null or empty.
     *
     * @param scenarios the list of DO to transform
     * @return the list of resulting DTO.
     */
    List<ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO> toFullyDetailledDtos(Collection<ExecutedScenario> scenarios) {
        if (null == scenarios) {
            return new ArrayList<>();
        }
        return scenarios.stream()
                .map(this::toFullyDetailledDto)
                .collect(Collectors.toList());
    }

    private void fillDto(ExecutedScenarioDTO dto, ExecutedScenario scenario) {
        dto.setId(scenario.getId());
        dto.setFeatureFile(scenario.getFeatureFile());
        dto.setFeatureName(scenario.getFeatureName());
        dto.setFeatureTags(scenario.getFeatureTags());
        dto.setTags(scenario.getTags());
        dto.setSeverity(scenario.getSeverity());
        dto.setName(scenario.getName());
        dto.setCucumberId(scenario.getCucumberId());
        dto.setLine(scenario.getLine());
        dto.setContent(scenario.getContent());
        dto.setStartDateTime(scenario.getStartDateTime());
        dto.setScreenshotUrl(scenario.getScreenshotUrl());
        dto.setVideoUrl(scenario.getVideoUrl());
        dto.setLogsUrl(scenario.getLogsUrl());
        dto.setHttpRequestsUrl(scenario.getHttpRequestsUrl());
        dto.setJavaScriptErrorsUrl(scenario.getJavaScriptErrorsUrl());
        dto.setDiffReportUrl(scenario.getDiffReportUrl());
        dto.setCucumberReportUrl(scenario.getCucumberReportUrl());
        dto.setApiServer(scenario.getApiServer());
        dto.setSeleniumNode(scenario.getSeleniumNode());
    }
}
