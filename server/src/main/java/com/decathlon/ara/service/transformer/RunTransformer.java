package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.Run;
import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO;
import com.decathlon.ara.service.dto.run.RunDTO;
import com.decathlon.ara.service.dto.run.RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service provide transformation utilities (DTO - DO and DO - DTO) for the Run.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.0.1
 */
@Service
class RunTransformer {

    @Autowired
    CountryTransformer countryTransformer;

    @Autowired
    TypeTransformer typeTransformer;

    @Autowired
    ExecutedScenarioTransformer executedScenarioTransformer;

    /**
     * Transform the given Run DO to a RunDTO object.
     * <p>
     * Returns an empty RunDTO if the parameter is null.
     *
     * @param run the DO to transform
     * @return the result DTO.
     */
    RunDTO toDto(Run run) {
        RunDTO result = new RunDTO();
        result.setId(0L);
        if (null != run) {
            this.fillDto(result, run);
        }
        return result;
    }

    /**
     * Transform the given Run DO to a RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO object.
     * <p>
     * Returns an empty RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO if the parameter is null.
     *
     * @param run the DO to transform
     * @return the result DTO.
     */
    RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO toFullyDetailledDto(Run run) {
        RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO result = new RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO();
        result.setId(0L);
        if (null != run) {
            this.fillDto(result, run);
            List<ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO> executedScenarios = executedScenarioTransformer.toFullyDetailledDtos(run.getExecutedScenarios());
            result.setExecutedScenarios(executedScenarios);
        }
        return result;
    }

    /**
     * Transform the given list of Run DO to a list of RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO.
     * <p>
     * Returns an empty list if the parameter is null or empty.
     *
     * @param runs the list of DO to transform
     * @return the list of resulting DTO.
     */
    List<RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO> toFullyDetailledDtos(Collection<Run> runs) {
        if (null == runs) {
            return new ArrayList<>();
        }
        return runs.stream()
                .map(this::toFullyDetailledDto)
                .collect(Collectors.toList());
    }

    private void fillDto(RunDTO dto, Run run) {
        dto.setId(run.getId());
        dto.setCountry(countryTransformer.toDto(run.getCountry()));
        dto.setType(typeTransformer.toDtoWithSource(run.getType()));
        dto.setComment(run.getComment());
        dto.setPlatform(run.getPlatform());
        dto.setJobUrl(run.getJobUrl());
        dto.setStatus(run.getStatus());
        dto.setCountryTags(run.getCountryTags());
        dto.setSeverityTags(run.getSeverityTags());
        dto.setIncludeInThresholds(run.getIncludeInThresholds());
        dto.setStartDateTime(run.getStartDateTime());
        dto.setEstimatedDuration(run.getEstimatedDuration());
        dto.setDuration(run.getDuration());
    }
}
