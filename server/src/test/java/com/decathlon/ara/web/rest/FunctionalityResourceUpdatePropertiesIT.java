package com.decathlon.ara.web.rest;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.enumeration.FunctionalitySeverity;
import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import java.util.Calendar;
import java.util.Date;
import javax.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static com.decathlon.ara.util.TestUtil.NONEXISTENT;
import static com.decathlon.ara.util.TestUtil.header;
import static com.decathlon.ara.util.TestUtil.timestamp;
import static com.decathlon.ara.web.rest.FunctionalityResourceITSuite.TEAM_ID_NOT_ASSIGNABLE_TO_FUNCTIONALITIES;
import static com.decathlon.ara.web.rest.FunctionalityResourceITSuite.folder;
import static com.decathlon.ara.web.rest.FunctionalityResourceITSuite.functionality;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
@DatabaseSetup("/dbunit/functionality.xml")
public class FunctionalityResourceUpdatePropertiesIT {

    private static final String PROJECT_CODE = "p";
    private static final Long A_FOLDER_ID = Long.valueOf(1);
    private static final Long A_FUNCTIONALITY_ID = Long.valueOf(31);

    @Autowired
    private FunctionalityRepository functionalityRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private FunctionalityResource cut;

    @Test
    public void testUpdateFolder() {
        Long id = Long.valueOf(12);
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, id, folder("F 1.2 renamed"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(id);
        assertThat(response.getBody().getParentId()).isEqualTo(1);
        assertThat(response.getBody().getOrder()).isEqualTo(2000);
        assertThat(response.getBody().getType()).isEqualTo(FunctionalityType.FOLDER.name());
        assertThat(response.getBody().getName()).isEqualTo("F 1.2 renamed");

        entityManager.flush();

        Functionality reloadedFolder = functionalityRepository.getOne(id);
        assertThat(reloadedFolder.getId()).isEqualTo(id);
        assertThat(reloadedFolder.getParentId()).isEqualTo(1);
        assertThat(reloadedFolder.getOrder()).isEqualTo(2000);
        assertThat(reloadedFolder.getType()).isEqualTo(FunctionalityType.FOLDER);
        assertThat(reloadedFolder.getName()).isEqualTo("F 1.2 renamed");
    }

    @Test
    public void testUpdateFunctionality() {
        FunctionalityDTO functionality = new FunctionalityDTO();

        // These properties must be ignored, and not updated in database
        functionality.setId(NONEXISTENT);
        functionality.setParentId(NONEXISTENT);
        functionality.setOrder(-1);
        functionality.setType("FUNCTIONALITY");
        functionality.setCoveredScenarios(Integer.valueOf(42));
        functionality.setCoveredCountryScenarios("to-be-ignored");
        functionality.setIgnoredScenarios(Integer.valueOf(42));
        functionality.setIgnoredCountryScenarios("to-be-ignored");

        // These properties will be updated in database
        functionality.setName("F 2.2 renamed");
        functionality.setCountryCodes("be,nl");
        functionality.setTeamId(Long.valueOf(3));
        functionality.setSeverity("HIGH");
        functionality.setCreated("Created");
        functionality.setStarted(Boolean.TRUE);
        functionality.setNotAutomatable(Boolean.FALSE); // Was null
        functionality.setComment("New comment");

        Long id = Long.valueOf(22);
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, id, functionality);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(id);
        assertThat(response.getBody().getParentId()).isEqualTo(2);
        assertThat(response.getBody().getOrder()).isEqualTo(2000);
        assertThat(response.getBody().getType()).isEqualTo(FunctionalityType.FUNCTIONALITY.name());
        assertThat(response.getBody().getName()).isEqualTo("F 2.2 renamed");
        assertThat(response.getBody().getCountryCodes()).isEqualTo("be,nl");
        assertThat(response.getBody().getTeamId()).isEqualTo(3L);
        assertThat(response.getBody().getSeverity()).isEqualTo("HIGH");
        assertThat(response.getBody().getCreated()).isEqualTo("Created");
        assertThat(response.getBody().getStarted()).isEqualTo(Boolean.TRUE);
        assertThat(response.getBody().getNotAutomatable()).isEqualTo(Boolean.FALSE);
        assertThat(response.getBody().getCoveredScenarios()).isNull();
        assertThat(response.getBody().getCoveredCountryScenarios()).isNull();
        assertThat(response.getBody().getIgnoredScenarios()).isNull();
        assertThat(response.getBody().getIgnoredCountryScenarios()).isNull();
        assertThat(response.getBody().getComment()).isEqualTo("New comment");

        entityManager.flush();

        Functionality reloadedFunctionality = functionalityRepository.getOne(id);
        assertThat(reloadedFunctionality.getId()).isEqualTo(id);
        assertThat(reloadedFunctionality.getParentId()).isEqualTo(2);
        assertThat(reloadedFunctionality.getOrder()).isEqualTo(2000);
        assertThat(reloadedFunctionality.getType()).isEqualTo(FunctionalityType.FUNCTIONALITY);
        assertThat(reloadedFunctionality.getName()).isEqualTo("F 2.2 renamed");
        assertThat(reloadedFunctionality.getCountryCodes()).isEqualTo("be,nl");
        assertThat(reloadedFunctionality.getTeamId()).isEqualTo(3L);
        assertThat(reloadedFunctionality.getSeverity()).isEqualTo(FunctionalitySeverity.HIGH);
        assertThat(reloadedFunctionality.getCreated()).isEqualTo("Created");
        assertThat(reloadedFunctionality.getStarted()).isEqualTo(Boolean.TRUE);
        assertThat(reloadedFunctionality.getNotAutomatable()).isEqualTo(Boolean.FALSE);
        assertThat(reloadedFunctionality.getCoveredScenarios()).isNull();
        assertThat(reloadedFunctionality.getCoveredCountryScenarios()).isNull();
        assertThat(reloadedFunctionality.getIgnoredScenarios()).isNull();
        assertThat(reloadedFunctionality.getIgnoredCountryScenarios()).isNull();
        assertThat(reloadedFunctionality.getComment()).isEqualTo("New comment");
    }

    @Test
    public void testUpdateNonexistentFolder() {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, NONEXISTENT, folder("N"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The folder does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    public void testUpdateNonexistentFunctionality() {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, NONEXISTENT, functionality());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The functionality does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    public void testUpdateFolderWithoutName() {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FOLDER_ID, folder("")); // Testing both ""...
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.mandatory_name");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A folder must have a name.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    public void testUpdateFunctionalityWithoutName() {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality().withName(null)); // ... and null
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.mandatory_name");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A functionality must have a name.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    public void testUpdateFunctionalityWithoutTeamId() {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality().withTeamId(null));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.mandatory_team_id");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A functionality must have a team.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    public void testUpdateFunctionalityWithoutSeverity() {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality().withSeverity(null));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.mandatory_severity");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A functionality must have a severity.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    public void testUpdateFunctionalityWithoutCountryCode() {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality().withCountryCodes("")); // Empty string...
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.mandatory_country_codes");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A functionality must have at least one country.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");

        response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality().withCountryCodes(null)); // ... and null
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.mandatory_country_codes");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A functionality must have at least one country.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    public void testUpdateFunctionalityBothStartedAndNotAutomatable() {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality().withStarted(Boolean.TRUE).withNotAutomatable(Boolean.TRUE));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.exclusive_started_and_not_automatable");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A functionality cannot be both non-automatable and started.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    public void testUpdateFunctionalityWithNonexistentCountry() {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality().withCountryCodes("nl,xx"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The country does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("country");
    }

    @Test
    public void testUpdateFunctionalityWithNonexistentTeam() {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality().withTeamId(NONEXISTENT));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The team does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("team");
    }

    @Test
    public void testUpdateFunctionalityWithNonexistentSeverity() {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality().withSeverity("NONEXISTENT"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.wrong_severity");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("Wrong severity: it does not exist.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    public void testUpdateFunctionalityWithNonAssignableTeam() {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality().withTeamId(TEAM_ID_NOT_ASSIGNABLE_TO_FUNCTIONALITIES));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_assignable_team");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("team");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The team cannot be assigned to a functionality.");
    }

    @Test
    public void testUpdateWithExistingNameOfFolder() {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, Long.valueOf(1), folder("F 2"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The name is already used by another folder in the same parent folder.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
        assertThat(header(response, HeaderUtil.DUPLICATE_PROPERTY_NAME)).isEqualTo("name");
        assertThat(header(response, HeaderUtil.OTHER_ENTITY_KEY)).isEqualTo("2");
    }

    @Test
    public void testUpdateWithExistingNameOfFunctionality() {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, Long.valueOf(113), folder("F 1.1.1"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The name is already used by another functionality in the same folder.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
        assertThat(header(response, HeaderUtil.DUPLICATE_PROPERTY_NAME)).isEqualTo("name");
        assertThat(header(response, HeaderUtil.OTHER_ENTITY_KEY)).isEqualTo("111");
    }

    @Test
    public void testUpdateWithExistingNameInAnotherFolder() {
        final Long id = Long.valueOf(2);
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, id, folder("F 1.1.1"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(id);
    }

    @Test
    public void testUpdateShouldSetUpdateDateTimeWhileNotUpdatingCreationDate() {
        // GIVEN
        final Long id = Long.valueOf(2);
        Date startDate = new Date();

        // WHEN
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, id, folder("F 1.1.1"));

        // THEN
        final Functionality createdFunctionality = functionalityRepository.findById(response.getBody().getId())
                .orElseThrow(() -> new AssertionError("Should have created a functionality"));
        assertThat(createdFunctionality.getCreationDateTime()).isEqualTo(timestamp(2018, Calendar.JANUARY, 1, 12, 0, 0));
        assertThat(createdFunctionality.getUpdateDateTime()).isBetween(startDate, new Date(), true, true);
    }

    @Test
    public void testUpdateFolderWithContent() {
        FunctionalityDTO folder = folder("Renamed").withId(A_FOLDER_ID);
        assertFolderWithContent(folder.withCountryCodes("nl"));
        assertFolderWithContent(folder.withTeamId(Long.valueOf(1)));
        assertFolderWithContent(folder.withSeverity("HIGH"));
        assertFolderWithContent(folder.withCreated("18.02"));
        assertFolderWithContent(folder.withStarted(Boolean.TRUE));
        assertFolderWithContent(folder.withNotAutomatable(Boolean.TRUE));
        assertFolderWithContent(folder.withCoveredScenarios(Integer.valueOf(42)));
        assertFolderWithContent(folder.withIgnoredScenarios(Integer.valueOf(42)));
        assertFolderWithContent(folder.withCoveredCountryScenarios("some-content"));
        assertFolderWithContent(folder.withIgnoredCountryScenarios("some-content"));
        assertFolderWithContent(folder.withComment("Comment"));
    }

    private void assertFolderWithContent(FunctionalityDTO folder) {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, folder.getId(), folder);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.folder_can_only_have_name");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A folder can only have a name.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

}
