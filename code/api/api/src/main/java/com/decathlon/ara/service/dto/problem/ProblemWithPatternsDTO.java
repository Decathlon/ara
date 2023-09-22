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

import java.util.List;

import com.decathlon.ara.service.dto.problempattern.ProblemPatternDTO;
import com.decathlon.ara.service.dto.rootcause.RootCauseDTO;
import com.decathlon.ara.service.dto.team.TeamDTO;

public class ProblemWithPatternsDTO extends ProblemDTO {

    private List<ProblemPatternDTO> patterns;

    public ProblemWithPatternsDTO() {
    }

    public ProblemWithPatternsDTO(String name,
            String comment,
            TeamDTO blamedTeam,
            String defectId,
            RootCauseDTO rootCause,
            List<ProblemPatternDTO> patterns) {
        super(name, comment, blamedTeam, defectId, rootCause);
        this.patterns = patterns;
    }

    public List<ProblemPatternDTO> getPatterns() {
        return patterns;
    }

}
