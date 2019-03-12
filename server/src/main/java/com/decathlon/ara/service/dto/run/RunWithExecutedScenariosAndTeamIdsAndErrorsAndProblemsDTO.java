package com.decathlon.ara.service.dto.run;

import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO;
import java.util.List;
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
public class RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO extends RunDTO {

    private List<ExecutedScenarioWithTeamIdsAndErrorsAndProblemsDTO> executedScenarios;

}
