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

import com.decathlon.ara.SpringApplicationContext;
import com.decathlon.ara.domain.enumeration.DefectExistence;
import com.decathlon.ara.domain.enumeration.EffectiveProblemStatus;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.service.DefectService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.dto.execution.ExecutionDTO;
import com.decathlon.ara.service.dto.rootcause.RootCauseDTO;
import com.decathlon.ara.service.dto.team.TeamDTO;
import com.decathlon.ara.service.support.Settings;
import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class ProblemDTO {

    private Long id;

    @NotNull(message = "The name is required.")
    @Size(min = 1, max = 256, message = "The name is required and must not exceed {max} characters.")
    private String name;

    private String comment;

    private ProblemStatus status;

    private EffectiveProblemStatus effectiveStatus;

    private TeamDTO blamedTeam;

    @Size(max = 32, message = "The defect ID must not exceed {max} characters.")
    private String defectId;

    private DefectExistence defectExistence;

    private Date closingDateTime;

    private String defectUrl;

    private RootCauseDTO rootCause;

    private Date creationDateTime;

    /**
     * The {@link ExecutionDTO#testDateTime} of the first error occurrence for this problem,
     * or null if the problem never appeared.
     */
    private Date firstSeenDateTime;

    /**
     * The {@link ExecutionDTO#testDateTime} of the last error occurrence for this problem,
     * or null if the problem never appeared.
     */
    private Date lastSeenDateTime;

}
