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

package com.decathlon.ara.loader;

import com.decathlon.ara.Entities;
import com.decathlon.ara.domain.Communication;
import com.decathlon.ara.domain.enumeration.CommunicationType;
import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.security.service.user.UserAccountService;
import com.decathlon.ara.service.*;
import com.decathlon.ara.service.dto.communication.CommunicationDTO;
import com.decathlon.ara.service.dto.country.CountryDTO;
import com.decathlon.ara.service.dto.cycledefinition.CycleDefinitionDTO;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.dto.severity.SeverityDTO;
import com.decathlon.ara.service.dto.source.SourceDTO;
import com.decathlon.ara.service.dto.team.TeamDTO;
import com.decathlon.ara.service.dto.type.TypeWithSourceCodeDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.ForbiddenException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.exception.NotUniqueException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static com.decathlon.ara.loader.DemoLoaderConstants.*;

/**
 * Load project with its settings as the Demo project.
 */
@Service
@Transactional
public class DemoSettingsLoader {

    private static final String UL_START = "<ul style='list-style: none;'>";
    private static final String UL_STOP = "</ul>";

    private final ProjectService projectService;

    private final CommunicationService communicationService;

    private final SourceService sourceService;

    private final TypeService typeService;

    private final CountryService countryService;

    private final SeverityService severityService;

    private final CycleDefinitionService cycleDefinitionService;

    private final TeamService teamService;

    private final UserAccountService userAccountService;

    @Autowired
    public DemoSettingsLoader(
            ProjectService projectService,
            CommunicationService communicationService,
            SourceService sourceService,
            TypeService typeService,
            CountryService countryService,
            SeverityService severityService,
            CycleDefinitionService cycleDefinitionService,
            TeamService teamService,
            UserAccountService userAccountService
    ) {
        this.projectService = projectService;
        this.communicationService = communicationService;
        this.sourceService = sourceService;
        this.typeService = typeService;
        this.countryService = countryService;
        this.severityService = severityService;
        this.cycleDefinitionService = cycleDefinitionService;
        this.teamService = teamService;
        this.userAccountService = userAccountService;
    }

    public ProjectDTO createProjectWithCommunicationsAndRootCauses() throws NotUniqueException, ForbiddenException {
        var creationUser = userAccountService.getCurrentUserEntity().orElseThrow(() -> new ForbiddenException(Entities.PROJECT, "demo project creation"));
        var demoProject = new ProjectDTO(DEMO_PROJECT_CODE, DEMO_PROJECT_NAME, false);
        return projectService.create(demoProject, creationUser);
    }

    public void setCommunications(long projectId) throws NotFoundException {
        communicationService.update(projectId, new CommunicationDTO(
                Communication.EXECUTIONS,
                null,
                CommunicationType.HTML,
                "" +
                        "Project demoing all features of ARA." +
                        "<div style=\"margin-top: 4px; font-weight: bold;\">" +
                        "<i class=\"ivu-icon ivu-icon-md-compass\" " +
                        "   style=\"font-size: 32px; float: left; margin-right: 4px\"></i>\n" +
                        "FEELING LOST?<br>" +
                        "Read " +
                        link("https://github.com/Decathlon/ara/blob/main/doc/usage/demo/DemoWalkthrough.adoc",
                                "how to play with the demo project") +
                        " while learning how to use ARA and how it can help your team." +
                        "</div>"));

        communicationService.update(projectId, new CommunicationDTO(
                Communication.SCENARIO_WRITING_HELPS,
                null,
                CommunicationType.HTML,
                "<div style='text-align: center;'>" +
                        "<h2>Cucumber</h2>" +
                        UL_START +
                        linkLine("https://git.company.com/project/Steps.java", "Available steps") +
                        linkLine("https://git.company.com/project/features/", "Available scenarios") +
                        linkLine("https://git.company.com/project/warm-up.feature", "Warm-up scenarios") +
                        UL_STOP +
                        "<h2>Postman</h2>" +
                        UL_START +
                        linkLine("https://git.company.com/project/collections/", "Collections") +
                        linkLine("https://git.company.com/project/environments/", "Environment variables") +
                        UL_STOP +
                        "<h2>Our other Project Links</h2>" +
                        UL_START +
                        linkLine("https://wiki.company.com/project/best-practices", "Best practices") +
                        linkLine("https://wiki.company.com/project/documentation", "Custom documentation") +
                        UL_STOP +
                        "</div>"));

        communicationService.update(projectId, new CommunicationDTO(
                Communication.HOW_TO_ADD_SCENARIO,
                null,
                CommunicationType.TEXT,
                "Edit the scenario in Git and create a merge request."));
    }

    private String link(String url, String name) {
        return "<a href='" + url + "' target=\"_blank\">" +
                "<i class=\"ivu-icon ivu-icon-md-open\"></i> " +
                name +
                "</a>";
    }

    private String linkLine(String url, String name) {
        return "<li>" + link(url, name) + "</li>";
    }

    public void createSources(long projectId) throws NotUniqueException {
        final String araRepositoryUrl = "https://github.com/decathlon/ara";
        sourceService.create(projectId, new SourceDTO(
                SOURCE_CODE_API,
                "API",
                "A",
                Technology.POSTMAN,
                araRepositoryUrl +
                        "/tree/{{branch}}/server/src/main/resources/demo/collections/",
                BRANCH_MASTER,
                true));
        sourceService.create(projectId, new SourceDTO(
                SOURCE_CODE_WEB,
                "Web",
                "W",
                Technology.CUCUMBER,
                araRepositoryUrl +
                        "/tree/{{branch}}/generated-cucumber-report/src/main/resources/ara/demo/features/",
                BRANCH_MASTER,
                false));
    }

    public void createTypes(long projectId) throws BadRequestException {
        typeService.create(projectId, new TypeWithSourceCodeDTO(
                "api", "Integ. APIs", false, false, SOURCE_CODE_API));
        typeService.create(projectId, new TypeWithSourceCodeDTO(
                TYPE_CODE_FIREFOX_DESKTOP, "HMI Desktop", true, false, SOURCE_CODE_WEB));
        typeService.create(projectId, new TypeWithSourceCodeDTO(
                "firefox-mobile", "HMI Mobile", true, true, SOURCE_CODE_WEB));
    }

    public void createCountries(long projectId) throws BadRequestException {
        countryService.create(projectId, new CountryDTO("fr", "France"));
        countryService.create(projectId, new CountryDTO("us", "United States"));
    }

    public void createSeverities(long projectId) throws NotUniqueException {
        severityService.create(projectId, new SeverityDTO(
                "sanity-check",
                Integer.valueOf(1),
                "Sanity Check",
                "Sanity Ch.",
                "S.C.",
                false));
        severityService.create(projectId, new SeverityDTO(
                "high",
                Integer.valueOf(2),
                "High",
                "High",
                "High",
                true));
        severityService.create(projectId, new SeverityDTO(
                "medium",
                Integer.valueOf(3),
                "Medium",
                "Medium",
                "Med.",
                false));
    }

    public List<CycleDefinitionDTO> createCycleDefinitions(long projectId) throws NotUniqueException {
        return Arrays.asList(
                cycleDefinitionService.create(projectId, new CycleDefinitionDTO(
                        null, BRANCH_DEVELOP, CYCLE_DAY, Integer.valueOf(1))),
                cycleDefinitionService.create(projectId, new CycleDefinitionDTO(
                        null, BRANCH_DEVELOP, CYCLE_NIGHT, Integer.valueOf(1))),
                cycleDefinitionService.create(projectId, new CycleDefinitionDTO(
                        null, BRANCH_MASTER, CYCLE_DAY, Integer.valueOf(2))));
    }

    public List<TeamDTO> createTeams(long projectId) throws NotUniqueException {
        return Arrays.asList(
                createTeam(projectId, "Marketing", true),
                createTeam(projectId, "Catalog", true),
                createTeam(projectId, "Buy", true),
                createTeam(projectId, "Account", true),
                createTeam(projectId, "Infrastructure", false)
        // Add new teams at end of list, as they will be accessed by index
        );
    }

    private TeamDTO createTeam(long projectId, String name, boolean assignableToFunctionalities)
            throws NotUniqueException {
        return teamService.create(projectId,
                new TeamDTO(null, name, true, assignableToFunctionalities));
    }

}
