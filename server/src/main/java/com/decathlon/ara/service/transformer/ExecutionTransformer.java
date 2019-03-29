package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.service.dto.execution.ExecutionDTO;
import com.decathlon.ara.service.dto.execution.ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service provide transformation utilities (DTO - DO and DO - DTO) for the Execution.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.0.1
 */
@Service
public class ExecutionTransformer {

    @Autowired
    private QualityThresholdTransformer qualityThresholdTransformer;

    @Autowired
    private QualitySeverityTransformer qualitySeverityTransformer;

    @Autowired
    private CountryDeploymentTransformer countryDeploymentTransformer;

    @Autowired
    private RunTransformer runTransformer;

    /**
     * Transform the given Execution DO to a ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO object.
     * <p>
     * Returns an empty ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO if the parameter is null.
     *
     * @param execution the DO to transform
     * @return the result DTO.
     */
    public ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO toFullyDetailledDto(Execution execution) {
        ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO result = new ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO();
        result.setId(0L);
        if (null != execution) {
            this.fillDto(result, execution);
            result.setCountryDeployments(countryDeploymentTransformer.toDtos(execution.getCountryDeployments()));
            result.setRuns(runTransformer.toFullyDetailledDtos(execution.getRuns()));
        }
        return result;
    }

    /**
     * Transform the given Execution DO to a ExecutionDTO object.
     *
     * Returns an empty ExecutionDTO if the parameter is null.
     *
     * @param execution the DO to transform
     * @return the result DTO.
     */
    public ExecutionDTO toDto(Execution execution) {
        ExecutionDTO result = new ExecutionDTO();
        result.setId(0L);
        if (null != execution) {
            this.fillDto(result, execution);
        }
        return result;
    }

    private void fillDto(ExecutionDTO dto, Execution execution) {
        dto.setId(execution.getId());
        dto.setBranch(execution.getBranch());
        dto.setName(execution.getName());
        dto.setRelease(execution.getRelease());
        dto.setVersion(execution.getVersion());
        dto.setBuildDateTime(execution.getBuildDateTime());
        dto.setTestDateTime(execution.getTestDateTime());
        dto.setJobUrl(execution.getJobUrl());
        dto.setStatus(execution.getStatus());
        dto.setResult(execution.getResult());
        dto.setAcceptance(execution.getAcceptance());
        dto.setDiscardReason(execution.getDiscardReason());
        dto.setBlockingValidation((null == execution.getBlockingValidation()) ? false : execution.getBlockingValidation());
        dto.setQualityThresholds(qualityThresholdTransformer.toMap(execution.getQualityThresholds()));
        dto.setQualityStatus(execution.getQualityStatus());
        dto.setQualitySeverities(qualitySeverityTransformer.toDtos(execution.getQualitySeverities()));
        dto.setDuration(execution.getDuration());
        dto.setEstimatedDuration(execution.getEstimatedDuration());
    }
}
