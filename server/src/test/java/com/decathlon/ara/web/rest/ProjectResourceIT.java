package com.decathlon.ara.web.rest;

import com.decathlon.ara.domain.Communication;
import com.decathlon.ara.domain.RootCause;
import com.decathlon.ara.repository.CommunicationRepository;
import com.decathlon.ara.repository.RootCauseRepository;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static com.decathlon.ara.util.TestUtil.NONEXISTENT;
import static com.decathlon.ara.util.TestUtil.header;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
@DatabaseSetup("/dbunit/project.xml")
public class ProjectResourceIT {

    @Autowired
    private ProjectResource cut;

    @Autowired
    private CommunicationRepository communicationRepository;

    @Autowired
    private RootCauseRepository rootCauseRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void create_ShouldInsertEntity_WhenAllRulesAreRespected() {
        // GIVEN
        final ProjectDTO project = new ProjectDTO(null, "new-code", "New name", false);

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
                new ProjectDTO(Long.valueOf(1), "project-y", "Project A", false),
                new ProjectDTO(Long.valueOf(3), "project-z", "Project B", false),
                new ProjectDTO(Long.valueOf(2), "project-x", "Project C", true));
    }

    @Test
    public void create_ShouldFailAsBadRequest_WhenIdProvided() {
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
    public void create_ShouldFailAsNotUnique_WhenCodeAlreadyExists() {
        // GIVEN
        final ProjectDTO projectWithExistingCode = new ProjectDTO(null, "project-y", "any", false);

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
    public void create_ShouldFailAsNotUnique_WhenNameAlreadyExists() {
        // GIVEN
        final ProjectDTO projectWithExistingName = new ProjectDTO(null, "new-code", "Project A", false);

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
    public void create_ShouldInsertCommunications_WhenCreatingAProject() {
        // GIVEN
        final ProjectDTO project = new ProjectDTO(null, "new-code", "any", false);

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
    public void create_ShouldInsertDefaultRootCauses_WhenCreatingAProject() {
        // GIVEN
        final ProjectDTO project = new ProjectDTO(null, "new-code", "any", false);

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
    public void getAll_ShouldReturnAllEntitiesOrderedByName() {
        // WHEN
        List<ProjectDTO> projects = cut.getAll();

        // THEN
        assertThat(projects).containsExactly( // Ordered by name ASC
                new ProjectDTO(Long.valueOf(1), "project-y", "Project A", false),
                new ProjectDTO(Long.valueOf(3), "project-z", "Project B", false),
                new ProjectDTO(Long.valueOf(2), "project-x", "Project C", true));
    }

    @Test
    public void update_ShouldUpdateEntity_WhenAllRulesAreRespected() {
        // GIVEN
        final Long existingId = Long.valueOf(1);
        final ProjectDTO project = new ProjectDTO(null, "renamed-code", "Renamed name", false);

        // WHEN
        ResponseEntity<ProjectDTO> response = cut.update(existingId, project);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.project.updated");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("1");
        assertThat(cut.getAll()).containsExactly( // Ordered by name ASC
                new ProjectDTO(Long.valueOf(3), "project-z", "Project B", false),
                new ProjectDTO(Long.valueOf(2), "project-x", "Project C", true),
                new ProjectDTO(Long.valueOf(1), "renamed-code", "Renamed name", false));
    }

    @Test
    public void update_ShouldNotFailAsNameNotUnique_WhenUpdatingWithoutAnyChange() {
        // GIVEN
        Long existingId = Long.valueOf(1);
        final ProjectDTO project = new ProjectDTO(existingId, "project-y", "Project A", false);

        // WHEN
        ResponseEntity<ProjectDTO> response = cut.update(existingId, project);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.project.updated");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("1");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void update_ShouldFailAsNotFound_WhenUpdatingNonexistentEntity() {
        // GIVEN
        final ProjectDTO anyProject = new ProjectDTO(null, "Trying to...", "... update nonexistent", false);

        // WHEN
        ResponseEntity<ProjectDTO> response = cut.update(NONEXISTENT, anyProject);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The project does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("project");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void update_ShouldFailAsNotUnique_WhenCodeAlreadyExists() {
        // GIVEN
        final Long id = Long.valueOf(2);
        final ProjectDTO projectWithExistingCode = new ProjectDTO(null, "project-y", "any", false);

        // WHEN
        ResponseEntity<ProjectDTO> response = cut.update(id, projectWithExistingCode);

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
    public void update_ShouldFailAsNotUnique_WhenNameAlreadyExists() {
        // GIVEN
        final Long id = Long.valueOf(2);
        final ProjectDTO projectWithExistingName = new ProjectDTO(null, "any", "Project A", false);

        // WHEN
        ResponseEntity<ProjectDTO> response = cut.update(id, projectWithExistingName);

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
    public void update_ShouldNotDeleteCommunications_WhenCalled() {
        // GIVEN
        final Long id = Long.valueOf(1);
        final ProjectDTO updatedProjectProperties = new ProjectDTO(null, "any", "any", false);

        // WHEN
        cut.update(id, updatedProjectProperties);

        // THEN
        assertThat(communicationRepository.findAllByProjectIdOrderByCode(id.longValue())).hasSize(1);
    }

    private void assertThatTableHasNotChangedInDataBase() {
        getAll_ShouldReturnAllEntitiesOrderedByName();
    }

}
