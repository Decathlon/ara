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

import com.decathlon.ara.domain.enumeration.DefectExistence;
import com.decathlon.ara.domain.enumeration.ProblemStatusFilter;
import com.decathlon.ara.domain.filter.ProblemFilter;

/**
 * Input of the problem filtering API.<br>
 * Same as {@link ProblemFilter} but without {@code projectId}, as this field is provided in REST API URL.
 */
public class ProblemFilterDTO {

    private String name;

    private ProblemStatusFilter status;

    private Long blamedTeamId;

    private String defectId;

    private DefectExistence defectExistence;

    private Long rootCauseId;

    public String getName() {
        return name;
    }

    public ProblemStatusFilter getStatus() {
        return status;
    }

    public Long getBlamedTeamId() {
        return blamedTeamId;
    }

    public String getDefectId() {
        return defectId;
    }

    public DefectExistence getDefectExistence() {
        return defectExistence;
    }

    public Long getRootCauseId() {
        return rootCauseId;
    }

}
