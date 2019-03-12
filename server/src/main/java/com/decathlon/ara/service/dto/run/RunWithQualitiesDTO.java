package com.decathlon.ara.service.dto.run;

import com.decathlon.ara.domain.Severity;
import com.decathlon.ara.domain.Team;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class RunWithQualitiesDTO extends RunDTO {

    /**
     * Key: {@link Severity#code};<br>
     * Value: the totals of scenarios for this severity for the run.
     */
    private Map<String, ExecutedScenarioHandlingCountsDTO> qualitiesPerSeverity;

    /**
     * Key 1: {@link Team#id} as a String;<br>
     * Key 2: {@link Severity#code};<br>
     * Value: the totals of scenarios for this team and severity for the run.
     */
    private Map<String, Map<String, ExecutedScenarioHandlingCountsDTO>> qualitiesPerTeamAndSeverity;

}
