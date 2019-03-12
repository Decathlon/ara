package com.decathlon.ara.service.dto.problem;

import com.decathlon.ara.service.dto.problempattern.ProblemPatternDTO;
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
public class ProblemWithPatternsAndAggregateTDO extends ProblemWithAggregateDTO {

    private List<ProblemPatternDTO> patterns;

}
