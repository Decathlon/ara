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

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.decathlon.ara.domain.enumeration.DefectExistence;
import com.decathlon.ara.domain.enumeration.EffectiveProblemStatus;
import com.decathlon.ara.domain.enumeration.ProblemStatus;
import com.decathlon.ara.service.dto.execution.ExecutionDTO;
import com.decathlon.ara.service.dto.rootcause.RootCauseDTO;
import com.decathlon.ara.service.dto.team.TeamDTO;

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

    public ProblemDTO() {
    }

    public ProblemDTO(
            String name,
            String comment, TeamDTO blamedTeam,
            String defectId,
            RootCauseDTO rootCause) {
        this.name = name;
        this.comment = comment;
        this.blamedTeam = blamedTeam;
        this.defectId = defectId;
        this.rootCause = rootCause;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public ProblemStatus getStatus() {
        return status;
    }

    public void setStatus(ProblemStatus status) {
        this.status = status;
    }

    public EffectiveProblemStatus getEffectiveStatus() {
        return effectiveStatus;
    }

    public TeamDTO getBlamedTeam() {
        return blamedTeam;
    }

    public void setBlamedTeam(TeamDTO blamedTeam) {
        this.blamedTeam = blamedTeam;
    }

    public String getDefectId() {
        return defectId;
    }

    public void setDefectId(String defectId) {
        this.defectId = defectId;
    }

    public DefectExistence getDefectExistence() {
        return defectExistence;
    }

    public void setDefectExistence(DefectExistence defectExistence) {
        this.defectExistence = defectExistence;
    }

    public Date getClosingDateTime() {
        return closingDateTime;
    }

    public void setClosingDateTime(Date closingDateTime) {
        this.closingDateTime = closingDateTime;
    }

    public String getDefectUrl() {
        return defectUrl;
    }

    public void setDefectUrl(String defectUrl) {
        this.defectUrl = defectUrl;
    }

    public RootCauseDTO getRootCause() {
        return rootCause;
    }

    public void setRootCause(RootCauseDTO rootCause) {
        this.rootCause = rootCause;
    }

    public Date getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(Date creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public Date getFirstSeenDateTime() {
        return firstSeenDateTime;
    }

    public Date getLastSeenDateTime() {
        return lastSeenDateTime;
    }

}
