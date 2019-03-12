package com.decathlon.ara.service.dto.executedscenario;

import com.decathlon.ara.domain.enumeration.Handling;
import com.decathlon.ara.service.dto.run.RunWithExecutionDTO;
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
public class ExecutedScenarioWithRunAndExecutionDTO extends ExecutedScenarioDTO {

    private Handling handling;

    private RunWithExecutionDTO run;

}
