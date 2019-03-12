package com.decathlon.ara.service.dto.problem;

import com.decathlon.ara.domain.enumeration.DefectExistence;
import com.decathlon.ara.domain.enumeration.ProblemStatusFilter;
import com.decathlon.ara.domain.filter.ProblemFilter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

/**
 * Input of the problem filtering API.<br>
 * Same as {@link ProblemFilter} but without {@code projectId}, as this field is provided in REST API URL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class ProblemFilterDTO {

    private String name;

    private ProblemStatusFilter status;

    private Long blamedTeamId;

    private String defectId;

    private DefectExistence defectExistence;

    private Long rootCauseId;

}
