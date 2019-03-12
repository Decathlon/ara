package com.decathlon.ara.web.rest;

import com.decathlon.ara.service.dto.country.CountryDTO;
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
@DatabaseSetup("/dbunit/country.xml")
public class CountryResourceIT {

    private static final String PROJECT_CODE = "p";

    @Autowired
    private CountryResource cut;

    @Test
    public void getAll_ShouldReturnAllCountriesOfTheProject_WhenCallingWithAnExistingProject() {
        // WHEN
        ResponseEntity<List<CountryDTO>> response = cut.getAll(PROJECT_CODE);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly( // Ordered by code ASC
                new CountryDTO("be", "Belgium"),
                new CountryDTO("cn", "China"),
                new CountryDTO("f1", "Used by functionality at begin"),
                new CountryDTO("f2", "Used by functionality in middle"),
                new CountryDTO("f3", "Used by functionality at end"),
                new CountryDTO("nl", "Netherlands"),
                new CountryDTO("s1", "Used by scenario at begin"),
                new CountryDTO("s2", "Used by scenario in middle"),
                new CountryDTO("s3", "Used by scenario at end"),
                new CountryDTO("ud", "Used by country-deployment"),
                new CountryDTO("up", "Used by problem-pattern"),
                new CountryDTO("ur", "Used by run"));
    }

    @Test
    public void create_ShouldInsertEntity_WhenAllRulesAreRespected() {
        // GIVEN
        CountryDTO country = new CountryDTO()
                .withCode("  cd \t ") // Should be trimmed
                .withName(" \t some-name  "); // Should be trimmed

        // WHEN
        ResponseEntity<CountryDTO> response = cut.create(PROJECT_CODE, country);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation().getPath()).isEqualTo("/api/projects/p/countries/cd");
        assertThat(response.getHeaders().get("X-ara-alert")).containsExactly("ara.country.created");
        assertThat(response.getHeaders().get("X-ara-params")).containsExactly("cd");
        assertThat(response.getBody().getCode()).isEqualTo("cd");
        assertThat(response.getBody().getName()).isEqualTo("some-name");
        final CountryDTO reReadDto = getByCode("cd");
        assertThat(reReadDto.getName()).isEqualTo("some-name");
    }

    @Test
    public void create_ShouldFailAsNotUnique_WhenCreatingWithAnExistingCode() {
        // GIVEN
        CountryDTO country = new CountryDTO()
                .withCode("be")
                .withName("Already exists...");

        // WHEN
        ResponseEntity<CountryDTO> response = cut.create(PROJECT_CODE, country);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("country");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The code is already used by another country.");
        assertThat(response.getHeaders().get("X-ara-duplicatePropertyName")).containsExactly("code");
        assertThat(response.getHeaders().get("X-ara-otherEntityKey")).containsExactly("be");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void create_ShouldFailAsNotUnique_WhenCreatingWithAnExistingName() {
        // GIVEN
        CountryDTO country = new CountryDTO()
                .withCode("xx")
                .withName("China");

        // WHEN
        ResponseEntity<CountryDTO> response = cut.create(PROJECT_CODE, country);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("country");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The name is already used by another country.");
        assertThat(response.getHeaders().get("X-ara-duplicatePropertyName")).containsExactly("name");
        assertThat(response.getHeaders().get("X-ara-otherEntityKey")).containsExactly("cn");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void createOrUpdate_ShouldCreateANewCountry_WhenProvidingANewCode() {
        // GIVEN
        final String rawCode = "  cd \t "; // Should be trimmed
        CountryDTO country = new CountryDTO()
                .withCode("any")
                .withName(" \t some-name  "); // Should be trimmed

        // WHEN
        ResponseEntity<CountryDTO> response = cut.createOrUpdate(PROJECT_CODE, rawCode, country);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().get("X-ara-alert")).containsExactly("ara.country.created");
        assertThat(response.getHeaders().get("X-ara-params")).containsExactly("cd");
        assertThat(response.getBody().getCode()).isEqualTo("cd");
        assertThat(response.getBody().getName()).isEqualTo("some-name");
        final CountryDTO reReadDto = getByCode("cd");
        assertThat(reReadDto.getName()).isEqualTo("some-name");
    }

    @Test
    public void createOrUpdate_ShouldUpdateACountry_WhenAllBusinessRulesAreMet() {
        // GIVEN
        final String rawCode = "  be \t "; // Should be trimmed
        CountryDTO country = new CountryDTO()
                .withCode("any")
                .withName(" \t some-name  "); // Should be trimmed

        // WHEN
        ResponseEntity<CountryDTO> response = cut.createOrUpdate(PROJECT_CODE, rawCode, country);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get("X-ara-alert")).containsExactly("ara.country.updated");
        assertThat(response.getHeaders().get("X-ara-params")).containsExactly("be");
        assertThat(response.getBody().getCode()).isEqualTo("be");
        assertThat(response.getBody().getName()).isEqualTo("some-name");
        final CountryDTO reReadDto = getByCode("be");
        assertThat(reReadDto.getName()).isEqualTo("some-name");
    }

    @Test
    public void createOrUpdate_ShouldFailAsNotUnique_WhenCreatingWithAnExistingName() {
        // GIVEN
        final String rawCode = "be";
        CountryDTO country = new CountryDTO()
                .withCode("any")
                .withName("China");

        // WHEN
        ResponseEntity<CountryDTO> response = cut.createOrUpdate(PROJECT_CODE, rawCode, country);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("country");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The name is already used by another country.");
        assertThat(response.getHeaders().get("X-ara-duplicatePropertyName")).containsExactly("name");
        assertThat(response.getHeaders().get("X-ara-otherEntityKey")).containsExactly("cn");
    }

    @Test
    public void delete_ShouldRemoveCountry_WhenCountryExists() {
        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, "cn");

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get("X-ara-alert")).containsExactly("ara.country.deleted");
        assertThat(response.getHeaders().get("X-ara-params")).containsExactly("cn");
        final CountryDTO reReadDto = getByCode("cn");
        assertThat(reReadDto.getCode()).isEqualTo("not-found");
    }

    @Test
    public void delete_ShouldFailAsNotFound_WhenCalledWithNonexistentCode() {
        // GIVEN
        String nonexistentCode = "xx";

        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, nonexistentCode);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The country does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("country");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void delete_ShouldFailAsBadRequest_WhenCountryIsUsedByCountryDeployment() {
        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, "ud");

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.used_by_country_deployment");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The country is used by at least one deployment in an execution: please wait for executions with such deployments to be purged.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("country");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void delete_ShouldFailAsBadRequest_WhenCountryIsUsedByRun() {
        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, "ur");

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.used_by_run");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The country is used by at least one run in an execution: please wait for executions with such runs to be purged.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("country");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void delete_ShouldFailAsBadRequest_WhenCountryIsUsedByProblemPattern() {
        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, "up");

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.used_by_problem_pattern");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The country is used by at least one rule of problem: please remove such rules and/or problems.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("country");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void delete_ShouldFailAsBadRequest_WhenCountryIsUsedByFunctionalityAtBegin() {
        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, "f1");

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.used_by_functionality");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The country is used by at least one functionality: please remove the country from such functionalities.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("country");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void delete_ShouldFailAsBadRequest_WhenCountryIsUsedByFunctionalityInMiddle() {
        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, "f2");

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.used_by_functionality");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The country is used by at least one functionality: please remove the country from such functionalities.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("country");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void delete_ShouldFailAsBadRequest_WhenCountryIsUsedByFunctionalityAtEnd() {
        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, "f3");

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.used_by_functionality");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The country is used by at least one functionality: please remove the country from such functionalities.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("country");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void delete_ShouldFailAsBadRequest_WhenCountryIsUsedByScenarioAtBegin() {
        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, "s1");

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.used_by_scenario");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The country is used by at least one scenario in Version Control System: please remove the country from such scenarios.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("country");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void delete_ShouldFailAsBadRequest_WhenCountryIsUsedByScenarioInMiddle() {
        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, "s2");

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.used_by_scenario");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The country is used by at least one scenario in Version Control System: please remove the country from such scenarios.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("country");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    public void delete_ShouldFailAsBadRequest_WhenCountryIsUsedByScenarioAtEnd() {
        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, "s3");

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.used_by_scenario");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The country is used by at least one scenario in Version Control System: please remove the country from such scenarios.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("country");
        assertThatTableHasNotChangedInDataBase();
    }

    private CountryDTO getByCode(String countryCode) {
        return cut.getAll(PROJECT_CODE).getBody().stream()
                .filter(c -> countryCode.equals(c.getCode()))
                .findFirst()
                .orElse(new CountryDTO("not-found", "not-found"));
    }

    private void assertThatTableHasNotChangedInDataBase() {
        getAll_ShouldReturnAllCountriesOfTheProject_WhenCallingWithAnExistingProject();
    }

}
