package com.decathlon.ara.service.dto.executedscenario;

import com.decathlon.ara.domain.enumeration.Handling;
import com.decathlon.ara.service.dto.error.ErrorWithProblemsDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO extends ExecutedScenarioDTO {

    private Handling handling;

    /**
     * All the teams associated to the functionalities associated to the scenario
     */
    private Set<Long> teamIds;

    private List<ErrorWithProblemsDTO> errors = new ArrayList<>();

}
