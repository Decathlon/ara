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

package com.decathlon.ara.service.dto.functionality;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class FunctionalityDTO {

    private Long id;

    private Long parentId;

    private double order;

    private String type;

    // NOTE: For this DTO, most validations are done in the Service, as it can represents a functionality OR folder
    // (folders have far fewer required fields)

    @NotNull(message = "The name is required.")
    @Size(min = 1, max = 512, message = "The name is required and must not exceed {max} characters.")
    private String name;

    @Size(max = 128)
    private String countryCodes;

    private Long teamId;

    private String severity;

    @Size(max = 10, message = "The created date or release must not exceed {max} characters.")
    private String created;

    private Boolean started;

    private Boolean notAutomatable;

    private Integer coveredScenarios;

    private String coveredCountryScenarios;

    private Integer ignoredScenarios;

    private String ignoredCountryScenarios;

    private String comment;

    public FunctionalityDTO() {
    }

    public FunctionalityDTO(Long id, Long parentId, double order, String type,
            String name,
            String countryCodes, Long teamId, String severity,
            String created,
            Boolean started, Boolean notAutomatable, Integer coveredScenarios, String coveredCountryScenarios,
            Integer ignoredScenarios, String ignoredCountryScenarios, String comment) {
        this.id = id;
        this.parentId = parentId;
        this.order = order;
        this.type = type;
        this.name = name;
        this.countryCodes = countryCodes;
        this.teamId = teamId;
        this.severity = severity;
        this.created = created;
        this.started = started;
        this.notAutomatable = notAutomatable;
        this.coveredScenarios = coveredScenarios;
        this.coveredCountryScenarios = coveredCountryScenarios;
        this.ignoredScenarios = ignoredScenarios;
        this.ignoredCountryScenarios = ignoredCountryScenarios;
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public double getOrder() {
        return order;
    }

    public void setOrder(double order) {
        this.order = order;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryCodes() {
        return countryCodes;
    }

    public void setCountryCodes(String countryCodes) {
        this.countryCodes = countryCodes;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public Boolean getStarted() {
        return started;
    }

    public void setStarted(Boolean started) {
        this.started = started;
    }

    public Boolean getNotAutomatable() {
        return notAutomatable;
    }

    public void setNotAutomatable(Boolean notAutomatable) {
        this.notAutomatable = notAutomatable;
    }

    public Integer getCoveredScenarios() {
        return coveredScenarios;
    }

    public void setCoveredScenarios(Integer coveredScenarios) {
        this.coveredScenarios = coveredScenarios;
    }

    public String getCoveredCountryScenarios() {
        return coveredCountryScenarios;
    }

    public void setCoveredCountryScenarios(String coveredCountryScenarios) {
        this.coveredCountryScenarios = coveredCountryScenarios;
    }

    public Integer getIgnoredScenarios() {
        return ignoredScenarios;
    }

    public void setIgnoredScenarios(Integer ignoredScenarios) {
        this.ignoredScenarios = ignoredScenarios;
    }

    public String getIgnoredCountryScenarios() {
        return ignoredCountryScenarios;
    }

    public void setIgnoredCountryScenarios(String ignoredCountryScenarios) {
        this.ignoredCountryScenarios = ignoredCountryScenarios;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
