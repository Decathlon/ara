package com.decathlon.ara.web.rest;

import com.decathlon.ara.service.dto.team.TeamDTO;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import java.util.List;
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
@DatabaseSetup("/dbunit/team.xml")
public class TeamResourceIT {

    private static final String PROJECT_CODE = "p";

    @Autowired
    private TeamResource cut;

    @Test
    public void create_ShouldInsertEntity_WhenAllRulesAreRespected() {
        // GIVEN
        final TeamDTO team = new TeamDTO(null, " A Trimmed Team \t ", true, false);

        // WHEN
        ResponseEntity<TeamDTO> response = cut.create(PROJECT_CODE, team);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation().getPath()).isEqualTo("/api/projects/p/teams/" + response.getBody().getId());
        assertThat(response.getBody().getId()).isGreaterThan(3);
        assertThat(response.getBody().getName()).isEqualTo("A Trimmed Team");
        assertThat(response.getBody().isAssignableToProblems()).isTrue();
        assertThat(response.getBody().isAssignableToFunctionalities()).isFalse();
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly( // Ordered by name ASC
                new TeamDTO(response.getBody().getId(), "A Trimmed Team", true, false),
                new TeamDTO(Long.valueOf(1), "Team A", true, true),
                new TeamDTO(Long.valueOf(3), "Team B", true, true),
                new TeamDTO(Long.valueOf(2), "Team C", true, true));
    }

    @Test
    public void create_ShouldFailAsBadRequest_WhenIdProvided() {
        // GIVEN
        final TeamDTO teamWithId = new TeamDTO(NONEXISTENT, "Id should not be provided", true, true);

        // WHEN
        ResponseEntity<TeamDTO> response = cut.create(PROJECT_CODE, teamWithId);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.id_exists");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("team");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A new team cannot already have an ID.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void create_ShouldFailAsNotUnique_WhenNameAlreadyExists() {
        // GIVEN
        final TeamDTO teamWithExistingName = new TeamDTO(null, "Team A", false, false);

        // WHEN
        ResponseEntity<TeamDTO> response = cut.create(PROJECT_CODE, teamWithExistingName);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("team");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The name is already used by another team.");
        assertThat(header(response, HeaderUtil.DUPLICATE_PROPERTY_NAME)).isEqualTo("name");
        assertThat(header(response, HeaderUtil.OTHER_ENTITY_KEY)).isEqualTo("1");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void getAll_ShouldReturnAllEntitiesOrderedByName() {
        // WHEN
        ResponseEntity<List<TeamDTO>> response = cut.getAll(PROJECT_CODE);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly( // Ordered by name ASC
                new TeamDTO(Long.valueOf(1), "Team A", true, true),
                new TeamDTO(Long.valueOf(3), "Team B", true, true),
                new TeamDTO(Long.valueOf(2), "Team C", true, true));
    }

    @Test
    public void update_ShouldUpdateEntity_WhenAllRulesAreRespected() {
        // GIVEN
        final Long existingId = Long.valueOf(1);
        final TeamDTO team = new TeamDTO(null, "Renamed", false, false);

        // WHEN
        ResponseEntity<TeamDTO> response = cut.update(PROJECT_CODE, existingId, team);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.team.updated");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("1");
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly( // Ordered by name ASC
                new TeamDTO(Long.valueOf(1), "Renamed", false, false),
                new TeamDTO(Long.valueOf(3), "Team B", true, true),
                new TeamDTO(Long.valueOf(2), "Team C", true, true));
    }

    @Test
    public void update_ShouldNotFailAsNameNotUnique_WhenUpdatingWithoutAnyChange() {
        // GIVEN
        Long existingId = Long.valueOf(1);
        final TeamDTO team = new TeamDTO(existingId, "Team A", true, true);

        // WHEN
        ResponseEntity<TeamDTO> response = cut.update(PROJECT_CODE, existingId, team);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.team.updated");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("1");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void update_ShouldFailAsNotFound_WhenUpdatingNonexistentEntity() {
        // GIVEN
        final TeamDTO anyTeam = new TeamDTO(null, "Trying to update nonexistent", false, true);

        // WHEN
        ResponseEntity<TeamDTO> response = cut.update(PROJECT_CODE, NONEXISTENT, anyTeam);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The team does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("team");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void update_ShouldFailAsNotUnique_WhenNameAlreadyExists() {
        // GIVEN
        final Long id = Long.valueOf(2);
        final TeamDTO teamWithExistingName = new TeamDTO(null, "Team A", true, false);

        // WHEN
        ResponseEntity<TeamDTO> response = cut.update(PROJECT_CODE, id, teamWithExistingName);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("team");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The name is already used by another team.");
        assertThat(header(response, HeaderUtil.DUPLICATE_PROPERTY_NAME)).isEqualTo("name");
        assertThat(header(response, HeaderUtil.OTHER_ENTITY_KEY)).isEqualTo("1");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void update_ShouldFailAsBadRequest_WhenRemovingAssignableToFunctionalitiesButWithOneAssignation() {
        // GIVEN
        final Long id = Long.valueOf(3);
        final TeamDTO teamWithRemovedAssignableToFunctionalities =
                new TeamDTO(null, "Team B", true, false);

        // WHEN
        ResponseEntity<TeamDTO> response = cut.update(PROJECT_CODE, id, teamWithRemovedAssignableToFunctionalities);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.has_assigned_functionalities");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("team");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("There are functionalities assigned to this team: please remove or change assignations before forbidding functionality assignation with this team.");
    }

    @Test
    public void update_ShouldFailAsBadRequest_WhenRemovingAssignableToProblemsButWithOneAssignation() {
        // GIVEN
        final Long id = Long.valueOf(3);
        final TeamDTO teamWithRemovedAssignableToProblems = new TeamDTO(null, "Team B", false, true);

        // WHEN
        ResponseEntity<TeamDTO> response = cut.update(PROJECT_CODE, id, teamWithRemovedAssignableToProblems);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.has_assigned_problems");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("team");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("There are problems assigned to this team: please remove or change assignations before forbidding problem assignation with this team.");
    }

    @Test
    public void delete_ShouldDeleteEntity_WhenRulesAreRespected() {
        // GIVEN
        final long existingId = 1;

        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, existingId);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.team.deleted");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("1");
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly( // Ordered by name ASC
                new TeamDTO(Long.valueOf(3), "Team B", true, true),
                new TeamDTO(Long.valueOf(2), "Team C", true, true));
    }

    @Test
    public void delete_ShouldFailAsNotFound_WhenDeletingNonexistentEntity() {
        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, NONEXISTENT.longValue());

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The team does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("team");
        assertThatTableHasNotChangedInDataBase();
    }

    private void assertThatTableHasNotChangedInDataBase() {
        getAll_ShouldReturnAllEntitiesOrderedByName();
    }

}
