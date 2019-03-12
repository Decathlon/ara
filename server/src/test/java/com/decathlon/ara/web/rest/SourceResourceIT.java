package com.decathlon.ara.web.rest;

import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.service.dto.source.SourceDTO;
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

import static com.decathlon.ara.util.TestUtil.header;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
@DatabaseSetup("/dbunit/source.xml")
public class SourceResourceIT {

    private static final String PROJECT_CODE = "p";

    @Autowired
    private SourceResource cut;

    @Test
    public void getAll_ShouldReturnAllSourcesOfTheProject_WhenCallingWithAnExistingProject() {
        // WHEN
        ResponseEntity<List<SourceDTO>> response = cut.getAll(PROJECT_CODE);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly( // Ordered by name ASC
                new SourceDTO("code1", "Source A", "A", Technology.CUCUMBER, "vcsUrl1", "develop", true),
                new SourceDTO("code2", "Source B", "B", Technology.CUCUMBER, "vcsUrl2", "develop", true),
                new SourceDTO("code3", "Source C", "C", Technology.CUCUMBER, "vcsUrl3", "develop", true));
    }

    @Test
    public void create_ShouldInsertNewSource_WhenAllBusinessRulesAreMet() {
        // GIVEN
        final SourceDTO newSource = new SourceDTO("code4", "New source", "l", Technology.CUCUMBER, "vcsUrl4", "develop", false);

        // WHEN
        ResponseEntity<SourceDTO> response = cut.create(PROJECT_CODE, newSource);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation().getPath()).isEqualTo("/api/projects/p/sources/code4");
        assertThat(response.getBody().getCode()).isEqualTo("code4");
        assertThat(response.getBody().getName()).isEqualTo("New source");
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly( // Ordered by name ASC
                new SourceDTO(response.getBody().getCode(), "New source", "l", Technology.CUCUMBER, "vcsUrl4", "stab", true),
                new SourceDTO("code1", "Source A", "A", Technology.CUCUMBER, "vcsUrl1", "develop", true),
                new SourceDTO("code2", "Source B", "B", Technology.CUCUMBER, "vcsUrl2", "develop", true),
                new SourceDTO("code3", "Source C", "C", Technology.CUCUMBER, "vcsUrl3", "develop", true));
    }

    @Test
    public void create_ShouldFailAsNotUnique_WhenCreatingWithAnExistingCode() {
        // GIVEN
        final SourceDTO newSourceWithExistingCode = new SourceDTO("code2", "any", "l", Technology.CUCUMBER, "any", "any", false);

        // WHEN
        ResponseEntity<SourceDTO> response = cut.create(PROJECT_CODE, newSourceWithExistingCode);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("source");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The code is already used by another source.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void create_ShouldFailAsNotUnique_WhenCreatingWithAnExistingName() {
        // GIVEN
        SourceDTO sourceWithExistingName = new SourceDTO("any", "Source A", "Z", Technology.POSTMAN, "any", "any", false);

        // WHEN
        ResponseEntity<SourceDTO> response = cut.create(PROJECT_CODE, sourceWithExistingName);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("source");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The name is already used by another source.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void create_ShouldFailAsNotUnique_WhenCreatingWithAnExistingLetter() {
        // GIVEN
        SourceDTO sourceWithExistingLetter = new SourceDTO("any", "any", "A", Technology.POSTMAN, "any", "any", false);

        // WHEN
        final ResponseEntity<SourceDTO> response = cut.create(PROJECT_CODE, sourceWithExistingLetter);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("source");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The letter is already used by another source.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void createOrUpdate_ShouldCreateANewSource_WhenAllBusinessRulesAreMet() {
        // GIVEN
        final SourceDTO sourceToCreate = new SourceDTO(null, "Rename X", "X", Technology.CUCUMBER, "newUrl", "master", true);

        // WHEN
        ResponseEntity<SourceDTO> response = cut.createOrUpdate(PROJECT_CODE, "code10", sourceToCreate);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().get("X-ara-alert")).containsExactly("ara.source.created");
        assertThat(response.getHeaders().get("X-ara-params")).containsExactly("code10");
        assertThat(response.getBody().getCode()).isEqualTo("code10");
        assertThat(response.getBody().getLetter()).isEqualTo("X");
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly( // Ordered by name ASC
                new SourceDTO("code10", "Rename X", "X", Technology.CUCUMBER, "newUrl", "master", true),
                new SourceDTO("code1", "Source A", "A", Technology.CUCUMBER, "vcsUrl1", "develop", true),
                new SourceDTO("code2", "Source B", "B", Technology.CUCUMBER, "vcsUrl2", "develop", true),
                new SourceDTO("code3", "Source C", "C", Technology.CUCUMBER, "vcsUrl3", "develop", true));
    }

    @Test
    public void createOrUpdate_ShouldUpdate_WhenSourceExists() {
        ResponseEntity<SourceDTO> response = cut.createOrUpdate(PROJECT_CODE, "code3", new SourceDTO(null, "Rename C", "D", Technology.CUCUMBER, "updatedUrl", "master", true));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.source.updated");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("code3");

        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly( // Ordered by name ASC
                new SourceDTO(response.getBody().getCode(), "Rename C", "D", Technology.CUCUMBER, "updatedUrl", "master", true),
                new SourceDTO("code1", "Source A", "A", Technology.CUCUMBER, "vcsUrl1", "develop", true),
                new SourceDTO("code2", "Source B", "B", Technology.CUCUMBER, "vcsUrl2", "develop", true));
    }

    @Test
    public void createOrUpdateWithSameLetter() {
        // WHEN
        ResponseEntity<SourceDTO> response = cut.createOrUpdate(PROJECT_CODE, "SEV-10", new SourceDTO("any", "any", "A", Technology.POSTMAN, "any", "master", false));

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("source");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The letter is already used by another source.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void createOrUpdateWithSameName() {
        // WHEN
        ResponseEntity<SourceDTO> response = cut.createOrUpdate(PROJECT_CODE, "SEV-10", new SourceDTO("any", "Source A", "Y", Technology.POSTMAN, "any", "any", false));

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("source");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The name is already used by another source.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void delete_ShouldRemoveSource_WhenSourceExists() {
        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, "code1");

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.source.deleted");
        assertThat(response.getHeaders().get("X-ara-params")).containsExactly("code1");
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly( // Ordered by name ASC
                new SourceDTO("code2", "Source B", "B", Technology.CUCUMBER, "vcsUrl2", "develop", true),
                new SourceDTO("code3", "Source C", "C", Technology.CUCUMBER, "vcsUrl3", "develop", true));
    }

    @Test
    public void delete_ShouldFailAsNotFound_WhenCalledWithNonexistentCode() {
        // GIVEN
        String nonexistentCode = "NONEXISTENT";

        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, nonexistentCode);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The source does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("source");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void delete_ShouldFailAsBadRequest_WhenSourceIsUsedByType() {
        // GIVEN
        String codeOfSourceUsedByType = "code3";

        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, codeOfSourceUsedByType);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.used_by_type");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The source is used by at least one type: please remove such types.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("source");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void delete_ShouldFailAsBadRequest_WhenSourceIsUsedByScenario() {
        // GIVEN
        String codeOfSourceUsedByScenario = "code2";

        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, codeOfSourceUsedByScenario);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.used_by_scenario");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The source is used by at least one scenario in Version Control System: you cannot remove such source.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("source");
        assertThatTableHasNotChangedInDataBase();
    }

    private void assertThatTableHasNotChangedInDataBase() {
        getAll_ShouldReturnAllSourcesOfTheProject_WhenCallingWithAnExistingProject();
    }

}
