/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

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
