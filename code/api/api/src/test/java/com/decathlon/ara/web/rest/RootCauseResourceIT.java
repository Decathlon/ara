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
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

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

import com.decathlon.ara.service.dto.rootcause.RootCauseDTO;
import com.decathlon.ara.util.factory.RootCauseDTOFactory;
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
@DatabaseSetup("/dbunit/root-cause.xml")
class RootCauseResourceIT {

    private static final String PROJECT_CODE = "p";

    @Autowired
    private RootCauseResource cut;

    @Test
    void create_ShouldInsertEntity_WhenAllRulesAreRespected() {
        // GIVEN
        final RootCauseDTO rootCause = RootCauseDTOFactory.get(null, " A Trimmed Root Cause \t ");

        // WHEN
        ResponseEntity<RootCauseDTO> response = cut.create(PROJECT_CODE, rootCause);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation().getPath()).isEqualTo("/api/projects/p/root-causes/" + response.getBody().getId());
        assertThat(response.getBody().getId()).isGreaterThan(3);
        assertThat(response.getBody().getName()).isEqualTo("A Trimmed Root Cause");
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly( // Ordered by name ASC
                RootCauseDTOFactory.get(response.getBody().getId(), "A Trimmed Root Cause"),
                RootCauseDTOFactory.get(Long.valueOf(1), "Root Cause A"),
                RootCauseDTOFactory.get(Long.valueOf(3), "Root Cause B"),
                RootCauseDTOFactory.get(Long.valueOf(2), "Root Cause C"));
    }

    @Test
    void create_ShouldFailAsBadRequest_WhenIdProvided() {
        // GIVEN
        final RootCauseDTO rootCauseWithId = RootCauseDTOFactory.get(NONEXISTENT, "Id should not be provided");

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
    void create_ShouldFailAsNotUnique_WhenNameAlreadyExists() {
        // GIVEN
        final RootCauseDTO rootCauseWithExistingName = RootCauseDTOFactory.get(null, "Root Cause A");

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
    void getAll_ShouldReturnAllEntitiesOrderedByName() {
        // WHEN
        ResponseEntity<List<RootCauseDTO>> response = cut.getAll(PROJECT_CODE);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly( // Ordered by name ASC
                RootCauseDTOFactory.get(Long.valueOf(1), "Root Cause A"),
                RootCauseDTOFactory.get(Long.valueOf(3), "Root Cause B"),
                RootCauseDTOFactory.get(Long.valueOf(2), "Root Cause C"));
    }

    @Test
    void update_ShouldUpdateEntity_WhenAllRulesAreRespected() {
        // GIVEN
        final Long existingId = Long.valueOf(1);
        final RootCauseDTO rootCause = RootCauseDTOFactory.get(null, "Renamed");

        // WHEN
        ResponseEntity<RootCauseDTO> response = cut.update(PROJECT_CODE, existingId, rootCause);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.root-cause.updated");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("1");
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly( // Ordered by name ASC
                RootCauseDTOFactory.get(Long.valueOf(1), "Renamed"),
                RootCauseDTOFactory.get(Long.valueOf(3), "Root Cause B"),
                RootCauseDTOFactory.get(Long.valueOf(2), "Root Cause C"));
    }

    @Test
    void update_ShouldNotFailAsNameNotUnique_WhenUpdatingWithoutAnyChange() {
        // GIVEN
        Long existingId = Long.valueOf(1);
        final RootCauseDTO rootCause = RootCauseDTOFactory.get(existingId, "Root Cause A");

        // WHEN
        ResponseEntity<RootCauseDTO> response = cut.update(PROJECT_CODE, existingId, rootCause);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.root-cause.updated");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("1");
        assertThatTableHasNotChangedInDataBase();
    }

    @Test
    void update_ShouldFailAsNotFound_WhenUpdatingNonexistentEntity() {
        // GIVEN
        final RootCauseDTO anyRootCause = RootCauseDTOFactory.get(null, "Trying to update nonexistent");

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
    void update_ShouldFailAsNotUnique_WhenNameAlreadyExists() {
        // GIVEN
        final Long id = Long.valueOf(2);
        final RootCauseDTO rootCauseWithExistingName = RootCauseDTOFactory.get(null, "Root Cause A");

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
    void delete_ShouldDeleteEntity_WhenRulesAreRespected() {
        // GIVEN
        final long existingId = 1;

        // WHEN
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, existingId);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.root-cause.deleted");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("1");
        assertThat(cut.getAll(PROJECT_CODE).getBody()).containsExactly( // Ordered by name ASC
                RootCauseDTOFactory.get(Long.valueOf(3), "Root Cause B"),
                RootCauseDTOFactory.get(Long.valueOf(2), "Root Cause C"));
    }

    @Test
    void delete_ShouldFailAsNotFound_WhenDeletingNonexistentEntity() {
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
