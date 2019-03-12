package com.decathlon.ara.service.dto.error;

import com.decathlon.ara.service.dto.executedscenario.ExecutedScenarioWithRunAndExecutionDTO;
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
public class ErrorWithExecutedScenarioAndRunAndExecutionDTO extends ErrorDTO {

    private ExecutedScenarioWithRunAndExecutionDTO executedScenario;

}
