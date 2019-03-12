package com.decathlon.ara.web.rest;

import com.decathlon.ara.service.dto.severity.SeverityDTO;
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
@DatabaseSetup("/dbunit/severity.xml")
public class SeverityResourceIT {

    private static final String PROJECT_CODE = "p";

    @Autowired
    private SeverityResource cut;

    @Test
    public void getAll_ShouldReturnAllSeveritiesOfTheProject_WhenCallingWithAnExistingProject() {
        // WHEN
        ResponseEntity<List<SeverityDTO>> response = cut.getAll(PROJECT_CODE);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly( // Ordered by name ASC
                new SeverityDTO("code1", Integer.valueOf(1), "test", "P0", "A", true),
                new SeverityDTO("code2", Integer.valueOf(2), "test2", "P2", "C", false),
                new SeverityDTO("code3", Integer.valueOf(3), "test1", "P1", "B", false));
    }

    @Test
    public void create_ShouldInsertNewSeverity_WhenAllBusinessRulesAreMet() {
        // GIVEN
        final SeverityDTO newSeverity = new SeverityDTO("code4", Integer.valueOf(4), "test4", "P4", "G", false);

        // WHEN
        ResponseEntity<SeverityDTO> response = cut.create(PROJECT_CODE, newSeverity);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation().getPath()).isEqualTo("/api/projects/p/severities/code4");
        assertThat(response.getBody().getCode()).isEqualTo("code4");
        assertThat(response.getBody().getPosition()).isEqualTo(4);
        assertThat(response.getBody().getName()).isEqualTo("test4");
        assertThat(response.getBody().getShortName()).isEqualTo("P4");
        assertThat(response.getBody().getInitials()).isEqualTo("G");
        assertThat(response.getBody().isDefaultOnMissing()).isFalse();
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly( // Ordered by name ASC
                new SeverityDTO("code1", Integer.valueOf(1), "test", "P0", "A", true),
                new SeverityDTO("code2", Integer.valueOf(2), "test2", "P2", "C", false),
                new SeverityDTO("code3", Integer.valueOf(3), "test1", "P1", "B", false),
                new SeverityDTO(response.getBody().getCode(), Integer.valueOf(4), "test4", "P4", "G", false));
    }

    @Test
    public void create_ShouldFailAsNotUnique_WhenCreatingWithAnExistingCode() {
        // GIVEN
        final SeverityDTO newSeverityWithExistingCode = new SeverityDTO("code1", Integer.valueOf(4), "test4", "P0", "G", false);

        // WHEN
        ResponseEntity<SeverityDTO> response = cut.create(PROJECT_CODE, newSeverityWithExistingCode);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("severity");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A severity with this code already exists.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void create_ShouldFailAsNotUnique_WhenCreatingWithAnotherDefaultOnMissing() {
        // GIVEN
        final SeverityDTO newSeverityWithAnotherDefaultOnMissing = new SeverityDTO("code7", Integer.valueOf(7), "test7", "P7", PROJECT_CODE, true);

        // WHEN
        ResponseEntity<SeverityDTO> response = cut.create(PROJECT_CODE, newSeverityWithAnotherDefaultOnMissing);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("severity");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("There is already another default severity.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void create_ShouldFailAsNotUnique_WhenCreatingWithAnExistingShortName() {
        // GIVEN
        final SeverityDTO newSeverityWithExistingShortName = new SeverityDTO("code9", Integer.valueOf(10), "test10", "P2", "Z", true);

        // WHEN
        final ResponseEntity<SeverityDTO> response = cut.create(PROJECT_CODE, newSeverityWithExistingShortName);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("severity");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A severity with this short name already exists.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void create_ShouldFailAsNotUnique_WhenCreatingWithAnExistingName() {
        // GIVEN
        final SeverityDTO newSeverityWithExistingName = new SeverityDTO("code9", Integer.valueOf(10), "test1", "P10", "Z", false);

        // WHEN
        ResponseEntity<SeverityDTO> response = cut.create(PROJECT_CODE, newSeverityWithExistingName);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("severity");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A severity with this name already exists.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void create_ShouldFailAsNotUnique_WhenCreatingWithAnExistingPosition() {
        // GIVEN
        final SeverityDTO newSeverityWithExistingPosition = new SeverityDTO("code9", Integer.valueOf(1), "test10", "P10", "x", false);

        // WHEN
        ResponseEntity<SeverityDTO> response = cut.create(PROJECT_CODE, newSeverityWithExistingPosition);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("severity");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A severity with this position already exists.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void create_ShouldFailAsNotUnique_WhenCreatingWithAnExistingInitials() {
        // GIVEN
        final SeverityDTO newSeverityWithExistingInitials = new SeverityDTO("code9", Integer.valueOf(10), "test10", "P10", "A", false);

        // WHEN
        ResponseEntity<SeverityDTO> response = cut.create(PROJECT_CODE, newSeverityWithExistingInitials);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("severity");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A severity with these initials already exists.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void createOrUpdate_ShouldUpdateSeverity_WhenAllBusinessRulesAreMet() {
        // GIVEN
        final String existingSeverityCode = "code2";
        final SeverityDTO updatedProperties = new SeverityDTO("code2", Integer.valueOf(5), "updated1", "updated2", "updated3", false);

        // WHEN
        ResponseEntity<SeverityDTO> response = cut.createOrUpdate(PROJECT_CODE, existingSeverityCode, updatedProperties);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.severity.updated");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo(existingSeverityCode);
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly( // Ordered by name ASC
                new SeverityDTO("code1", Integer.valueOf(1), "test", "P0", "A", true),
                new SeverityDTO("code3", Integer.valueOf(3), "test1", "P1", "B", false),
                new SeverityDTO("code2", Integer.valueOf(5), "updated1", "updated2", "updated3", false));
    }

    @Test
    public void createOrUpdate_ShouldNotFailAsNotUnique_WhenChangingNothing() {
        // GIVEN
        final String existingSeverityCode = "code2";
        final SeverityDTO unchangedProperties = new SeverityDTO("code2", Integer.valueOf(2), "test2", "P2", "C", false);

        // WHEN
        ResponseEntity<SeverityDTO> response = cut.createOrUpdate(PROJECT_CODE, existingSeverityCode, unchangedProperties);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.severity.updated");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("code2");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void createOrUpdate_ShouldFailAsNotUnique_WhenCreatingWithAnExistingShortName() {
        // GIVEN
        final SeverityDTO severityWithExistingShortName = new SeverityDTO("code9", Integer.valueOf(10), "test10", "P2", "Z", false);

        // WHEN
        ResponseEntity<SeverityDTO> response = cut.createOrUpdate(PROJECT_CODE, "new", severityWithExistingShortName);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("severity");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A severity with this short name already exists.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void createOrUpdate_ShouldFailAsNotUnique_WhenCreatingWithAnExistingName() {
        // GIVEN
        final SeverityDTO severityWithExistingName = new SeverityDTO("code9", Integer.valueOf(10), "test1", "P10", "Z", false);

        // WHEN
        ResponseEntity<SeverityDTO> response = cut.createOrUpdate(PROJECT_CODE, "new", severityWithExistingName);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("severity");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A severity with this name already exists.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void createOrUpdate_ShouldFailAsNotUnique_WhenCreatingWithAnExistingPosition() {
        // GIVEN
        final SeverityDTO severityWithExistingPosition = new SeverityDTO("code9", Integer.valueOf(1), "test10", "P10", "x", false);

        // WHEN
        ResponseEntity<SeverityDTO> response = cut.createOrUpdate(PROJECT_CODE, "new", severityWithExistingPosition);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("severity");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A severity with this position already exists.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void createOrUpdate_ShouldFailAsNotUnique_WhenCreatingWithExistingInitials() {
        // GIVEN
        final SeverityDTO severityWithExistingInitials = new SeverityDTO("SEV-10", Integer.valueOf(10), "test10", "P10", "A", false);

        // WHEN
        ResponseEntity<SeverityDTO> response = cut.createOrUpdate(PROJECT_CODE, "new", severityWithExistingInitials);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("severity");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A severity with these initials already exists.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void delete_ShouldDeleteSeverity_WhenCodeIsFound() {
        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, "code3");

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.severity.deleted");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("code3");
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly( // Ordered by name ASC
                new SeverityDTO("code1", Integer.valueOf(1), "test", "P0", "A", true),
                new SeverityDTO("code2", Integer.valueOf(2), "test2", "P2", "C", false));
    }

    @Test
    public void delete_ShouldFailAsNotFound_WhenCodeIsNonexistent() {
        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, String.valueOf(NONEXISTENT));

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The cycle definition does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("severity");
        assertThatTableHasNotChangedInDataBase();
    }

    private void assertThatTableHasNotChangedInDataBase() {
        getAll_ShouldReturnAllSeveritiesOfTheProject_WhenCallingWithAnExistingProject();
    }

}
