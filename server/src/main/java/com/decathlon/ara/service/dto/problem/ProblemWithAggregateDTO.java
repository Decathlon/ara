package com.decathlon.ara.service.dto.problem;

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
public class ProblemWithAggregateDTO extends ProblemDTO {

    private ProblemAggregateDTO aggregate;

}
