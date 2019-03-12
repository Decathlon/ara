package com.decathlon.ara.service.dto.error;

import com.decathlon.ara.service.dto.problem.ProblemDTO;
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
public class ErrorWithProblemsDTO extends ErrorDTO {

    /**
     * All the Problems of the associated ProblemPatterns
     */
    private List<ProblemDTO> problems;

}
