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

package com.decathlon.ara.web.rest;

import com.decathlon.ara.domain.Communication;
import com.decathlon.ara.domain.RootCause;
import com.decathlon.ara.repository.CommunicationRepository;
import com.decathlon.ara.repository.RootCauseRepository;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static com.decathlon.ara.util.TestUtil.NONEXISTENT;
import static com.decathlon.ara.util.TestUtil.header;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@SpringBootTest
@TestExecutionListeners({
    TransactionalTestExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DbUnitTestExecutionListener.class
})
@TestPropertySource(
		locations = "classpath:application-db-h2.properties")
@Transactional
@DatabaseSetup("/dbunit/project.xml")
class ProjectResourceIT {

    @Autowired
    private ProjectResource cut;

    @Autowired
    private CommunicationRepository communicationRepository;

    @Autowired
    private RootCauseRepository rootCauseRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void create_ShouldInsertEntity_WhenAllRulesAreRespected() {
        // GIVEN
        final ProjectDTO project = new ProjectDTO("new-code", "New name", false);

        // WHEN
        ResponseEntity<ProjectDTO> response = cut.create(project);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation().getPath()).isEqualTo("/api/projects/" + response.getBody().getId());
        assertThat(response.getBody().getId()).isGreaterThan(3);
        assertThat(response.getBody().getCode()).isEqualTo("new-code");
        assertThat(response.getBody().getName()).isEqualTo("New name");
        assertThat(response.getBody().isDefaultAtStartup()).isFalse();
        assertThat(cut.getAll()).containsExactly( // Ordered by name ASC
                new ProjectDTO(response.getBody().getId(), "new-code", "New name", false),
                new ProjectDTO(1L, "project-y", "Project A", false),
                new ProjectDTO(3L, "project-z", "Project B", false),
                new ProjectDTO(2L, "project-x", "Project C", true));
    }

    @Test
    void create_ShouldFailAsBadRequest_WhenIdProvided() {
        // GIVEN
        final ProjectDTO projectWithId = new ProjectDTO(NONEXISTENT, "is...", "...provided", false);

        // WHEN
        ResponseEntity<ProjectDTO> response = cut.create(projectWithId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.id_exists");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("project");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A new project cannot already have an ID.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    void create_ShouldFailAsNotUnique_WhenCodeAlreadyExists() {
        // GIVEN
        final ProjectDTO projectWithExistingCode = new ProjectDTO("project-y", "any", false);

        // WHEN
        ResponseEntity<ProjectDTO> response = cut.create(projectWithExistingCode);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("project");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The code is already used by another project.");
        assertThat(header(response, HeaderUtil.DUPLICATE_PROPERTY_NAME)).isEqualTo("code");
        assertThat(header(response, HeaderUtil.OTHER_ENTITY_KEY)).isEqualTo("1");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    void create_ShouldFailAsNotUnique_WhenNameAlreadyExists() {
        // GIVEN
        final ProjectDTO projectWithExistingName = new ProjectDTO("new-code", "Project A", false);

        // WHEN
        ResponseEntity<ProjectDTO> response = cut.create(projectWithExistingName);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("project");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The name is already used by another project.");
        assertThat(header(response, HeaderUtil.DUPLICATE_PROPERTY_NAME)).isEqualTo("name");
        assertThat(header(response, HeaderUtil.OTHER_ENTITY_KEY)).isEqualTo("1");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    void create_ShouldInsertCommunications_WhenCreatingAProject() {
        // GIVEN
        final ProjectDTO project = new ProjectDTO("new-code", "any", false);

        // WHEN
        ResponseEntity<ProjectDTO> response = cut.create(project);
        entityManager.flush();

        // THEN
        final long createdProjectId = response.getBody().getId().longValue();
        final List<Communication> communications = communicationRepository.findAllByProjectIdOrderByCode(createdProjectId);
        assertThat(communications).hasSize(3);
        assertThat(communications.stream().map(Communication::getCode)).containsOnly(
                "executions",
                "scenario-writing-helps",
                "how" + "to-add-scenario");
    }

    @Test
    void create_ShouldInsertDefaultRootCauses_WhenCreatingAProject() {
        // GIVEN
        final ProjectDTO project = new ProjectDTO("new-code", "any", false);

        // WHEN
        ResponseEntity<ProjectDTO> response = cut.create(project);
        entityManager.flush();

        // THEN
        final long createdProjectId = response.getBody().getId().longValue();
        final List<RootCause> rootCauses = rootCauseRepository.findAllByProjectIdOrderByName(createdProjectId);
        assertThat(rootCauses.stream().map(RootCause::getName)).containsExactly(
                "Fragile test",
                "Network issue",
                "Regression",
                "Test to update");
    }

    @Test
    void getAll_ShouldReturnAllEntitiesOrderedByName() {
        // WHEN
        List<ProjectDTO> projects = cut.getAll();

        // THEN
        assertThat(projects).containsExactly( // Ordered by name ASC
                new ProjectDTO(1L, "project-y", "Project A", false),
                new ProjectDTO(3L, "project-z", "Project B", false),
                new ProjectDTO(2L, "project-x", "Project C", true));
    }

    @Test
    void update_ShouldUpdateEntity_WhenAllRulesAreRespected() {
        // GIVEN
        final String projectCode = "project-y";
        final ProjectDTO project = new ProjectDTO("renamed-code", "Renamed name", false);

        // WHEN
        ResponseEntity<ProjectDTO> response = cut.update(projectCode, project);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.project.updated");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("1");
        assertThat(cut.getAll()).containsExactly( // Ordered by name ASC
                new ProjectDTO(3L, "project-z", "Project B", false),
                new ProjectDTO(2L, "project-x", "Project C", true),
                new ProjectDTO(1L, "renamed-code", "Renamed name", false));
    }

    @Test
    void update_ShouldNotFailAsNameNotUnique_WhenUpdatingWithoutAnyChange() {
        // GIVEN
        Long existingId = 1L;
        String projectCode = "project-y";
        final ProjectDTO project = new ProjectDTO(existingId, "project-y", "Project A", false);

        // WHEN
        ResponseEntity<ProjectDTO> response = cut.update(projectCode, project);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.project.updated");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("1");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    void update_ShouldFailAsNotFound_WhenUpdatingNonexistentEntity() {
        // GIVEN
        final ProjectDTO anyProject = new ProjectDTO("Trying to...", "... update nonexistent", false);

        // WHEN
        ResponseEntity<ProjectDTO> response = cut.update("NONEXISTENT_PROJECT", anyProject);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The project does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("project");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    void update_ShouldFailAsNotUnique_WhenCodeAlreadyExists() {
        // GIVEN
        final String code = "project-y";
        final ProjectDTO projectWithExistingCode = new ProjectDTO("project-y", "any", false);

        // WHEN
        ResponseEntity<ProjectDTO> response = cut.update(code, projectWithExistingCode);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("project");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The code is already used by another project.");
        assertThat(header(response, HeaderUtil.DUPLICATE_PROPERTY_NAME)).isEqualTo("code");
        assertThat(header(response, HeaderUtil.OTHER_ENTITY_KEY)).isEqualTo("1");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    void update_ShouldFailAsNotUnique_WhenNameAlreadyExists() {
        // GIVEN
        final String code = "project-x";
        final ProjectDTO projectWithExistingName = new ProjectDTO("any", "Project A", false);

        // WHEN
        ResponseEntity<ProjectDTO> response = cut.update(code, projectWithExistingName);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("project");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The name is already used by another project.");
        assertThat(header(response, HeaderUtil.DUPLICATE_PROPERTY_NAME)).isEqualTo("name");
        assertThat(header(response, HeaderUtil.OTHER_ENTITY_KEY)).isEqualTo("1");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    void update_ShouldNotDeleteCommunications_WhenCalled() {
        // GIVEN
        final Long id = 1L;
        final String code = "project-y";
        final ProjectDTO updatedProjectProperties = new ProjectDTO("any", "any", false);

        // WHEN
        cut.update(code, updatedProjectProperties);

        // THEN
        assertThat(communicationRepository.findAllByProjectIdOrderByCode(id)).hasSize(1);
    }

    private void assertThatTableHasNotChangedInDataBase() {
        getAll_ShouldReturnAllEntitiesOrderedByName();
    }

}
