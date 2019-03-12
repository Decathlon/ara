package com.decathlon.ara.domain.filter;

import com.decathlon.ara.domain.enumeration.DefectExistence;
import com.decathlon.ara.domain.enumeration.ProblemStatusFilter;
import com.decathlon.ara.service.dto.problem.ProblemFilterDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

/**
 * Input of the problem filtering repository query.<br>
 * Same as {@link ProblemFilterDTO} but with {@code projectId}, provided by the REST API URL.
 */
@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class ProblemFilter {

    private long projectId;

    private String name;

    private ProblemStatusFilter status;

    private Long blamedTeamId;

    private String defectId;

    private DefectExistence defectExistence;

    private Long rootCauseId;

}
