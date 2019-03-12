package com.decathlon.ara.service.dto.execution;

import com.decathlon.ara.service.dto.countrydeployment.CountryDeploymentDTO;
import com.decathlon.ara.service.dto.run.RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO;
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
public class ExecutionWithCountryDeploymentsAndRunsAndExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO extends ExecutionDTO {

    private List<CountryDeploymentDTO> countryDeployments;

    private List<RunWithExecutedScenariosAndTeamIdsAndErrorsAndProblemsDTO> runs;

}
