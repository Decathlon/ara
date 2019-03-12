package com.decathlon.ara.service.dto.execution;

import com.decathlon.ara.service.dto.run.ExecutedScenarioHandlingCountsDTO;
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
public class ExecutionWithHandlingCountsDTO extends ExecutionDTO {

    private ExecutedScenarioHandlingCountsDTO scenarioCounts;

}
