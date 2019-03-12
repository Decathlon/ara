package com.decathlon.ara.service.dto.problem;

import com.decathlon.ara.service.dto.problempattern.ProblemPatternDTO;
import com.decathlon.ara.service.dto.rootcause.RootCauseDTO;
import com.decathlon.ara.service.dto.team.TeamDTO;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ProblemWithPatternsDTO extends ProblemDTO {

    private List<ProblemPatternDTO> patterns;

    public ProblemWithPatternsDTO(String name,
                                  String comment,
                                  TeamDTO blamedTeam,
                                  String defectId,
                                  RootCauseDTO rootCause,
                                  List<ProblemPatternDTO> patterns) {
        this.setName(name);
        this.setComment(comment);
        this.setBlamedTeam(blamedTeam);
        this.setDefectId(defectId);
        this.setRootCause(rootCause);
        this.setPatterns(patterns);
    }

}
