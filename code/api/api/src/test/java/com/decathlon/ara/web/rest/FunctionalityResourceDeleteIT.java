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

import javax.persistence.EntityNotFoundException;
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
import com.decathlon.ara.repository.FunctionalityRepository;
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
class FunctionalityResourceDeleteIT {

    private static final String PROJECT_CODE = "p";

    @Autowired
    private FunctionalityRepository functionalityRepository;

    @Autowired
    private FunctionalityResource cut;

    @Test
    void testDeleteNonexistent() {
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, NONEXISTENT.longValue());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The functionality or folder does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");

        // Nothing should have changed
        assertCountIsNow(11);
    }

    @Test
    void testDeleteOneFunctionality() {
        deleteSingle(22);
    }

    @Test
    void testDeleteOneSingleFolder() {
        deleteSingle(12);
    }

    @Test
    void testDeleteFolderWithChildren() {
        long id = 11;
        deleteAndAssertSuccess(id);
        assertCountIsNow(7);
        assertNotFoundAnymore(id);
        assertNotFoundAnymore(111);
        assertNotFoundAnymore(112);
        assertNotFoundAnymore(113);
    }

    @Test
    void testDeleteRootFolderWithChildren() {
        long id = 1;
        deleteAndAssertSuccess(id);
        assertCountIsNow(5);
        assertNotFoundAnymore(id);
        assertNotFoundAnymore(11);
        assertNotFoundAnymore(12);
        assertNotFoundAnymore(111);
        assertNotFoundAnymore(112);
        assertNotFoundAnymore(113);
    }

    private void assertCountIsNow(int expected) {
        assertThat(functionalityRepository.count()).isEqualTo(expected);
    }

    private void assertNotFoundAnymore(long id) {
        final Functionality functionality = functionalityRepository.getById(Long.valueOf(id));
        try {
            assertThat(functionality).isNull();
        } catch (EntityNotFoundException e) {
            // Expected
        }
    }

    private void deleteSingle(long id) {
        deleteAndAssertSuccess(id);
        assertCountIsNow(10);
        assertNotFoundAnymore(id);
    }

    private void deleteAndAssertSuccess(long id) {
        ResponseEntity<Void> response = cut.delete(PROJECT_CODE, id);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(header(response, HeaderUtil.ALERT)).isEqualTo("ara.functionality.deleted");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo(String.valueOf(id));
    }

}
