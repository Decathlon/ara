package com.decathlon.ara.web.rest;

import com.decathlon.ara.service.dto.rootcause.RootCauseDTO;
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
@DatabaseSetup("/dbunit/root-cause.xml")
public class RootCauseResourceIT {

    private static final String PROJECT_CODE = "p";

    @Autowired
    private RootCauseResource cut;

    @Test
    public void create_ShouldInsertEntity_WhenAllRulesAreRespected() {
        // GIVEN
        final RootCauseDTO rootCause = new RootCauseDTO(null, " A Trimmed Root Cause \t ");

        // WHEN
        ResponseEntity<RootCauseDTO> response = cut.create(PROJECT_CODE, rootCause);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation().getPath()).isEqualTo("/api/projects/p/root-causes/" + response.getBody().getId());
        assertThat(response.getBody().getId()).isGreaterThan(3);
        assertThat(response.getBody().getName()).isEqualTo("A Trimmed Root Cause");
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly( // Ordered by name ASC
                new RootCauseDTO(response.getBody().getId(), "A Trimmed Root Cause"),
                new RootCauseDTO(Long.valueOf(1), "Root Cause A"),
                new RootCauseDTO(Long.valueOf(3), "Root Cause B"),
                new RootCauseDTO(Long.valueOf(2), "Root Cause C"));
    }

    @Test
    public void create_ShouldFailAsBadRequest_WhenIdProvided() {
        // GIVEN
        final RootCauseDTO rootCauseWithId = new RootCauseDTO(NONEXISTENT, "Id should not be provided");

        // WHEN
        ResponseEntity<RootCauseDTO> response = cut.create(PROJECT_CODE, rootCauseWithId);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.id_exists");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("root-cause");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A new root-cause cannot already have an ID.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void create_ShouldFailAsNotUnique_WhenNameAlreadyExists() {
        // GIVEN
        final RootCauseDTO rootCauseWithExistingName = new RootCauseDTO(null, "Root Cause A");

        // WHEN
        ResponseEntity<RootCauseDTO> response = cut.create(PROJECT_CODE, rootCauseWithExistingName);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("root-cause");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The name is already used by another root cause.");
        assertThat(header(response, HeaderUtil.DUPLICATE_PROPERTY_NAME)).isEqualTo("name");
        assertThat(header(response, HeaderUtil.OTHER_ENTITY_KEY)).isEqualTo("1");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void getAll_ShouldReturnAllEntitiesOrderedByName() {
        // WHEN
        ResponseEntity<List<RootCauseDTO>> response = cut.getAll(PROJECT_CODE);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly( // Ordered by name ASC
                new RootCauseDTO(Long.valueOf(1), "Root Cause A"),
                new RootCauseDTO(Long.valueOf(3), "Root Cause B"),
                new RootCauseDTO(Long.valueOf(2), "Root Cause C"));
    }

    @Test
    public void update_ShouldUpdateEntity_WhenAllRulesAreRespected() {
        // GIVEN
        final Long existingId = Long.valueOf(1);
        final RootCauseDTO rootCause = new RootCauseDTO(null, "Renamed");

        // WHEN
        ResponseEntity<RootCauseDTO> response = cut.update(PROJECT_CODE, existingId, rootCause);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.root-cause.updated");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("1");
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly( // Ordered by name ASC
                new RootCauseDTO(Long.valueOf(1), "Renamed"),
                new RootCauseDTO(Long.valueOf(3), "Root Cause B"),
                new RootCauseDTO(Long.valueOf(2), "Root Cause C"));
    }

    @Test
    public void update_ShouldNotFailAsNameNotUnique_WhenUpdatingWithoutAnyChange() {
        // GIVEN
        Long existingId = Long.valueOf(1);
        final RootCauseDTO rootCause = new RootCauseDTO(existingId, "Root Cause A");

        // WHEN
        ResponseEntity<RootCauseDTO> response = cut.update(PROJECT_CODE, existingId, rootCause);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.root-cause.updated");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("1");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void update_ShouldFailAsNotFound_WhenUpdatingNonexistentEntity() {
        // GIVEN
        final RootCauseDTO anyRootCause = new RootCauseDTO(null, "Trying to update nonexistent");

        // WHEN
        ResponseEntity<RootCauseDTO> response = cut.update(PROJECT_CODE, NONEXISTENT, anyRootCause);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The root cause does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("root-cause");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void update_ShouldFailAsNotUnique_WhenNameAlreadyExists() {
        // GIVEN
        final Long id = Long.valueOf(2);
        final RootCauseDTO rootCauseWithExistingName = new RootCauseDTO(null, "Root Cause A");

        // WHEN
        ResponseEntity<RootCauseDTO> response = cut.update(PROJECT_CODE, id, rootCauseWithExistingName);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("root-cause");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The name is already used by another root cause.");
        assertThat(header(response, HeaderUtil.DUPLICATE_PROPERTY_NAME)).isEqualTo("name");
        assertThat(header(response, HeaderUtil.OTHER_ENTITY_KEY)).isEqualTo("1");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void delete_ShouldDeleteEntity_WhenRulesAreRespected() {
        // GIVEN
        final long existingId = 1;

        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, existingId);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.root-cause.deleted");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("1");
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly( // Ordered by name ASC
                new RootCauseDTO(Long.valueOf(3), "Root Cause B"),
                new RootCauseDTO(Long.valueOf(2), "Root Cause C"));
    }

    @Test
    public void delete_ShouldFailAsNotFound_WhenDeletingNonexistentEntity() {
        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, NONEXISTENT.longValue());

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The root cause does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("root-cause");
        assertThatTableHasNotChangedInDataBase();
    }

    private void assertThatTableHasNotChangedInDataBase() {
        getAll_ShouldReturnAllEntitiesOrderedByName();
    }

}
