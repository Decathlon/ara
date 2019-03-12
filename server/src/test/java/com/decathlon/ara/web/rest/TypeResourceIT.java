package com.decathlon.ara.web.rest;

import com.decathlon.ara.service.dto.type.TypeWithSourceCodeDTO;
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
@DatabaseSetup("/dbunit/type.xml")
public class TypeResourceIT {

    private static final String PROJECT_CODE = "p";

    @Autowired
    private TypeResource cut;

    @Test
    public void getAll_ShouldReturnAllTypesOfTheProject_WhenCallingWithAnExistingProject() {
        // WHEN
        ResponseEntity<List<TypeWithSourceCodeDTO>> response = cut.getAll(PROJECT_CODE);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly( // Ordered by name ASC
                new TypeWithSourceCodeDTO("TYPE-01", "TEST", true, false, "code1"),
                new TypeWithSourceCodeDTO("TYPE-02", "TEST2", true, false, "code2"),
                new TypeWithSourceCodeDTO("TYPE-03", "TEST3", true, true, "code3"),
                new TypeWithSourceCodeDTO("TYPE-04", "TEST4", false, false, "code4"));
    }

    @Test
    public void create_ShouldInsertNewType_WhenAllBusinessRulesAreMet() {
        // GIVEN
        final TypeWithSourceCodeDTO type = new TypeWithSourceCodeDTO("TYPE-05", "TEST5", true, false, "code1");

        // WHEN
        ResponseEntity<TypeWithSourceCodeDTO> response = cut.create(PROJECT_CODE, type);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation().getPath()).isEqualTo("/api/projects/p/types/TYPE-05");
        assertThat(response.getBody().getCode()).isEqualTo("TYPE-05");
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly( // Ordered by name ASC
                new TypeWithSourceCodeDTO("TYPE-01", "TEST", true, false, "code1"),
                new TypeWithSourceCodeDTO("TYPE-02", "TEST2", true, false, "code2"),
                new TypeWithSourceCodeDTO("TYPE-03", "TEST3", true, true, "code3"),
                new TypeWithSourceCodeDTO("TYPE-04", "TEST4", false, false, "code4"),
                new TypeWithSourceCodeDTO(response.getBody().getCode(), "TEST5", true, false, "code1"));
    }

    @Test
    public void create_ShouldFailAsNotUnique_WhenCreatingWithAnExistingCode() {
        // GIVEN
        final TypeWithSourceCodeDTO type = new TypeWithSourceCodeDTO("TYPE-01", "TEST7", true, false, "code1");

        // WHEN
        ResponseEntity<TypeWithSourceCodeDTO> response = cut.create(PROJECT_CODE, type);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("type");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The code is already used by another type.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void create_ShouldFailAsNotUnique_WhenCreatingWithAnExistingName() {
        // GIVEN
        final TypeWithSourceCodeDTO type = new TypeWithSourceCodeDTO("TYPE-11", "TEST", true, false, "code1");

        // WHEN
        ResponseEntity<TypeWithSourceCodeDTO> response = cut.create(PROJECT_CODE, type);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("type");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The name is already used by another type.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void createOrUpdate_ShouldUpdateAType_WhenAllBusinessRulesAreMet() {
        // GIVEN
        final String code = "TYPE-03";
        final TypeWithSourceCodeDTO type = new TypeWithSourceCodeDTO(code, "updated", true, false, "code3");

        // WHEN
        ResponseEntity<TypeWithSourceCodeDTO> response = cut.createOrUpdate(PROJECT_CODE, code, type);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.type.updated");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("TYPE-03");
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly( // Ordered by name ASC
                new TypeWithSourceCodeDTO("TYPE-01", "TEST", true, false, "code1"),
                new TypeWithSourceCodeDTO("TYPE-02", "TEST2", true, false, "code2"),
                new TypeWithSourceCodeDTO("TYPE-03", "updated", true, false, "code3"),
                new TypeWithSourceCodeDTO("TYPE-04", "TEST4", false, false, "code4"));
    }

    @Test
    public void createOrUpdate_ShouldNotFailAsNotUnique_WhenChangingNothing() {
        // GIVEN
        String code = "TYPE-02";
        final TypeWithSourceCodeDTO type = new TypeWithSourceCodeDTO(code, "TEST2", true, false, "code2");

        // WHEN
        ResponseEntity<TypeWithSourceCodeDTO> response = cut.createOrUpdate(PROJECT_CODE, code, type);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.type.updated");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("TYPE-02");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void createOrUpdate_ShouldFailAsNotUnique_WhenCreatingWithAnExistingName() {
        // GIVEN
        String code = "TYPE-02";
        final TypeWithSourceCodeDTO type = new TypeWithSourceCodeDTO("TYPE-11", "TEST", true, false, "code1");

        // WHEN
        ResponseEntity<TypeWithSourceCodeDTO> response = cut.createOrUpdate(PROJECT_CODE, code, type);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("type");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The name is already used by another type.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void delete_ShouldRemoveType_WhenTypeExists() {
        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, "TYPE-04");

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.type.deleted");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("TYPE-04");
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly( // Ordered by name ASC
                new TypeWithSourceCodeDTO("TYPE-01", "TEST", true, false, "code1"),
                new TypeWithSourceCodeDTO("TYPE-02", "TEST2", true, false, "code2"),
                new TypeWithSourceCodeDTO("TYPE-03", "TEST3", true, true, "code3"));
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
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The type does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("type");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void delete_ShouldFailAsBadRequest_WhenTypeIsUsedByProblemPattern() {
        // GIVEN
        String codeOfTypeUsedByProblemPattern = "TYPE-01";

        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, codeOfTypeUsedByProblemPattern);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.used_by_problem_pattern");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("type");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The type is used by at least one rule of problem: please remove such rules and/or problems.");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void delete_ShouldFailAsBadRequest_WhenTypeIsUsedByRun() {
        // GIVEN
        String codeOfTypeUsedByRun = "TYPE-03";

        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, codeOfTypeUsedByRun);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.used_by_run");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("type");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The type is used by at least one run in an execution: please wait for executions with runs of such types to be purged.");
        assertThatTableHasNotChangedInDataBase();
    }

    private void assertThatTableHasNotChangedInDataBase() {
        getAll_ShouldReturnAllTypesOfTheProject_WhenCallingWithAnExistingProject();
    }

}
