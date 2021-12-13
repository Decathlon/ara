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

import static com.decathlon.ara.util.TestUtil.NONEXISTENT;
import static com.decathlon.ara.util.TestUtil.header;
import static com.decathlon.ara.util.TestUtil.timestamp;
import static com.decathlon.ara.web.rest.FunctionalityResourceITSuite.TEAM_ID_NOT_ASSIGNABLE_TO_FUNCTIONALITIES;
import static com.decathlon.ara.web.rest.FunctionalityResourceITSuite.folder;
import static com.decathlon.ara.web.rest.FunctionalityResourceITSuite.functionality;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

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

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.enumeration.FunctionalitySeverity;
import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

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
@DatabaseSetup("/dbunit/functionality.xml")
class FunctionalityResourceUpdatePropertiesIT {

    private static final String PROJECT_CODE = "p";
    private static final Long A_FOLDER_ID = 1L;
    private static final Long A_FUNCTIONALITY_ID = 31L;

    @Autowired
    private FunctionalityRepository functionalityRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private FunctionalityResource cut;

    @Test
    void testUpdateFolder() {
        Long id = 12L;
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, id, folder("F 1.2 renamed"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(id);
        assertThat(response.getBody().getParentId()).isEqualTo(1);
        assertThat(response.getBody().getOrder()).isEqualTo(2000);
        assertThat(response.getBody().getType()).isEqualTo(FunctionalityType.FOLDER.name());
        assertThat(response.getBody().getName()).isEqualTo("F 1.2 renamed");

        entityManager.flush();

        Functionality reloadedFolder = functionalityRepository.getById(id);
        assertThat(reloadedFolder.getId()).isEqualTo(id);
        assertThat(reloadedFolder.getParentId()).isEqualTo(1);
        assertThat(reloadedFolder.getOrder()).isEqualTo(2000);
        assertThat(reloadedFolder.getType()).isEqualTo(FunctionalityType.FOLDER);
        assertThat(reloadedFolder.getName()).isEqualTo("F 1.2 renamed");
    }

    @Test
    void testUpdateFunctionality() {
        FunctionalityDTO functionality = new FunctionalityDTO();

        // These properties must be ignored, and not updated in database
        functionality.setId(NONEXISTENT);
        functionality.setParentId(NONEXISTENT);
        functionality.setOrder(-1);
        functionality.setType("FUNCTIONALITY");
        functionality.setCoveredScenarios(42);
        functionality.setCoveredCountryScenarios("to-be-ignored");
        functionality.setIgnoredScenarios(42);
        functionality.setIgnoredCountryScenarios("to-be-ignored");

        // These properties will be updated in database
        functionality.setName("F 2.2 renamed");
        functionality.setCountryCodes("be,nl");
        functionality.setTeamId(3L);
        functionality.setSeverity("HIGH");
        functionality.setCreated("Created");
        functionality.setStarted(Boolean.TRUE);
        functionality.setNotAutomatable(Boolean.FALSE); // Was null
        functionality.setComment("New comment");

        Long id = 22L;
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, id, functionality);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
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

        Functionality reloadedFunctionality = functionalityRepository.getById(id);
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
    void testUpdateNonexistentFolder() {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, NONEXISTENT, folder("N"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The folder does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    void testUpdateNonexistentFunctionality() {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, NONEXISTENT, functionality());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The functionality does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    void testUpdateFolderWithoutName() {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FOLDER_ID, folder("")); // Testing both ""...
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.mandatory_name");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A folder must have a name.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    void testUpdateFunctionalityWithoutName() {
        FunctionalityDTO functionality = functionality();
        functionality.setName(null);
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality); // ... and null
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.mandatory_name");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A functionality must have a name.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    void testUpdateFunctionalityWithoutTeamId() {
        FunctionalityDTO functionality = functionality();
        functionality.setTeamId(null);
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.mandatory_team_id");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A functionality must have a team.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    void testUpdateFunctionalityWithoutSeverity() {
        FunctionalityDTO functionality = functionality();
        functionality.setSeverity(null);
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.mandatory_severity");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A functionality must have a severity.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    void testUpdateFunctionalityWithoutCountryCode() {
        FunctionalityDTO functionality = functionality();
        functionality.setCountryCodes("");// Empty string...
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.mandatory_country_codes");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A functionality must have at least one country.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");

        functionality = functionality();
        functionality.setCountryCodes(null);
        response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality); // ... and null
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.mandatory_country_codes");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A functionality must have at least one country.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    void testUpdateFunctionalityBothStartedAndNotAutomatable() {
        FunctionalityDTO functionality = functionality();
        functionality.setStarted(Boolean.TRUE);
        functionality.setNotAutomatable(Boolean.TRUE);
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.exclusive_started_and_not_automatable");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A functionality cannot be both non-automatable and started.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    void testUpdateFunctionalityWithNonexistentCountry() {
        FunctionalityDTO functionality = functionality();
        functionality.setCountryCodes("nl,xx");
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The country does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("country");
    }

    @Test
    void testUpdateFunctionalityWithNonexistentTeam() {
        FunctionalityDTO functionality = functionality();
        functionality.setTeamId(NONEXISTENT);
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The team does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("team");
    }

    @Test
    void testUpdateFunctionalityWithNonexistentSeverity() {
        FunctionalityDTO functionality = functionality();
        functionality.setSeverity("NONEXISTENT");
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.wrong_severity");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("Wrong severity: it does not exist.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    void testUpdateFunctionalityWithNonAssignableTeam() {
        FunctionalityDTO functionality = functionality();
        functionality.setTeamId(TEAM_ID_NOT_ASSIGNABLE_TO_FUNCTIONALITIES);
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, A_FUNCTIONALITY_ID, functionality);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_assignable_team");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("team");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The team cannot be assigned to a functionality.");
    }

    @Test
    void testUpdateWithExistingNameOfFolder() {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, 1L, folder("F 2"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The name is already used by another folder in the same parent folder.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
        assertThat(header(response, HeaderUtil.DUPLICATE_PROPERTY_NAME)).isEqualTo("name");
        assertThat(header(response, HeaderUtil.OTHER_ENTITY_KEY)).isEqualTo("2");
    }

    @Test
    void testUpdateWithExistingNameOfFunctionality() {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, 113L, folder("F 1.1.1"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_unique");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The name is already used by another functionality in the same folder.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
        assertThat(header(response, HeaderUtil.DUPLICATE_PROPERTY_NAME)).isEqualTo("name");
        assertThat(header(response, HeaderUtil.OTHER_ENTITY_KEY)).isEqualTo("111");
    }

    @Test
    void testUpdateWithExistingNameInAnotherFolder() {
        final Long id = 2L;
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, id, folder("F 1.1.1"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(id);
    }

    @Test
    void testUpdateShouldSetUpdateDateTimeWhileNotUpdatingCreationDate() {
        // GIVEN
        final Long id = 2L;
        Date startDate = new Date();

        // WHEN
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, id, folder("F 1.1.1"));

        // THEN
        assertThat(response.getBody()).isNotNull();
        final Functionality createdFunctionality = functionalityRepository.findById(response.getBody().getId())
                .orElseThrow(() -> new AssertionError("Should have created a functionality"));
        assertThat(createdFunctionality.getCreationDateTime()).isEqualTo(timestamp(2018, Calendar.JANUARY, 1, 12, 0, 0));
        assertThat(createdFunctionality.getUpdateDateTime()).isBetween(startDate, new Date(), true, true);
    }

    @Test
    void testUpdateFolderWithContent() {
        FunctionalityDTO folder = folder("Renamed");
        folder.setId(A_FOLDER_ID);
        folder.setCountryCodes("nl");
        assertFolderWithContent(folder);
        folder.setTeamId(1L);
        assertFolderWithContent(folder);
        folder.setSeverity("HIGH");
        assertFolderWithContent(folder);
        folder.setCreated("18.02");
        assertFolderWithContent(folder);
        folder.setStarted(Boolean.TRUE);
        assertFolderWithContent(folder);
        folder.setNotAutomatable(Boolean.TRUE);
        assertFolderWithContent(folder);
        folder.setCoveredScenarios(42);
        assertFolderWithContent(folder);
        folder.setIgnoredScenarios(42);
        assertFolderWithContent(folder);
        folder.setCoveredCountryScenarios("some-content");
        assertFolderWithContent(folder);
        folder.setIgnoredCountryScenarios("some-content");
        assertFolderWithContent(folder);
        folder.setComment("Comment");
        assertFolderWithContent(folder);
    }

    @Test
    void testUpdateRemoveCommentShouldReturnEmptyString() {
        FunctionalityDTO functionality = new FunctionalityDTO();

        // These properties must be ignored, and not updated in database
        functionality.setId(NONEXISTENT);
        functionality.setParentId(NONEXISTENT);
        functionality.setOrder(-1);
        functionality.setType("FUNCTIONALITY");
        functionality.setCoveredScenarios(42);
        functionality.setCoveredCountryScenarios("to-be-ignored");
        functionality.setIgnoredScenarios(42);
        functionality.setIgnoredCountryScenarios("to-be-ignored");

        // These properties will be updated in database
        functionality.setName("F 2.2 renamed");
        functionality.setCountryCodes("be,nl");
        functionality.setTeamId(3L);
        functionality.setSeverity("HIGH");
        functionality.setCreated("Created");
        functionality.setStarted(Boolean.TRUE);
        functionality.setNotAutomatable(Boolean.FALSE); // Was null
        functionality.setComment("");

        Long id = 22L;
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, id, functionality);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(id);
        assertThat(response.getBody().getComment()).isEqualTo("");

        entityManager.flush();

        Functionality reloadedFunctionality = functionalityRepository.getById(id);
        assertThat(reloadedFunctionality.getId()).isEqualTo(id);
        assertThat(reloadedFunctionality.getComment()).isEqualTo("");
    }

    @Test
    void testUpdateRemoveCreatedShouldReturnEmptyString() {
        FunctionalityDTO functionality = new FunctionalityDTO();

        // These properties must be ignored, and not updated in database
        functionality.setId(NONEXISTENT);
        functionality.setParentId(NONEXISTENT);
        functionality.setOrder(-1);
        functionality.setType("FUNCTIONALITY");
        functionality.setCoveredScenarios(42);
        functionality.setCoveredCountryScenarios("to-be-ignored");
        functionality.setIgnoredScenarios(42);
        functionality.setIgnoredCountryScenarios("to-be-ignored");

        // These properties will be updated in database
        functionality.setName("F 2.2 renamed");
        functionality.setCountryCodes("be,nl");
        functionality.setTeamId(3L);
        functionality.setSeverity("HIGH");
        functionality.setCreated("");
        functionality.setStarted(Boolean.TRUE);
        functionality.setNotAutomatable(Boolean.FALSE); // Was null
        functionality.setComment("test");

        Long id = 22L;
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, id, functionality);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(id);
        assertThat(response.getBody().getCreated()).isEqualTo("");

        entityManager.flush();

        Functionality reloadedFunctionality = functionalityRepository.getById(id);
        assertThat(reloadedFunctionality.getId()).isEqualTo(id);
        assertThat(reloadedFunctionality.getCreated()).isEqualTo("");
    }

    private void assertFolderWithContent(FunctionalityDTO folder) {
        ResponseEntity<FunctionalityDTO> response = cut.updateProperties(PROJECT_CODE, folder.getId(), folder);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.folder_can_only_have_name");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A folder can only have a name.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }
}
