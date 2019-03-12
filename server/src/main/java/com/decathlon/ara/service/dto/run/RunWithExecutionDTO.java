package com.decathlon.ara.service.dto.run;

import com.decathlon.ara.service.dto.execution.ExecutionDTO;
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
public class RunWithExecutionDTO extends RunDTO {

    private ExecutionDTO execution;

}
