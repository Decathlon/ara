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

import com.decathlon.ara.service.dto.scenario.ScenarioDTO;
import com.decathlon.ara.web.rest.util.HeaderUtil;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
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

import javax.transaction.Transactional;
import java.util.List;

import static com.decathlon.ara.util.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

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
class FunctionalityResourceGetScenariosIT {

    private static final String PROJECT_CODE = "p";

    @Autowired
    private FunctionalityResource cut;

    @Test
    void testGetScenarios() {
        // Ordered by feature file and then scenario name
        assertFoundScenarios(111, longs(111011201, 11101, 11102, 111011202, 111011203, 11103, 11104));
        assertFoundScenarios(112, longs(111011201, 111011202, 111011203, 11201));
        assertFoundScenarios(22, longs());
    }

    @Test
    void testGetScenariosOfNonExistentFunctionality() {
        final ResponseEntity<List<ScenarioDTO>> response = cut.getScenarios(PROJECT_CODE, NONEXISTENT.longValue());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.not_found");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("The functionality does not exist: it has perhaps been removed.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    @Test
    void testGetScenariosOfFolder() {
        final ResponseEntity<List<ScenarioDTO>> response = cut.getScenarios(PROJECT_CODE, 1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(header(response, HeaderUtil.ERROR)).isEqualTo("error.folders_have_no_coverage");
        assertThat(header(response, HeaderUtil.MESSAGE)).isEqualTo("A folder cannot have coverage.");
        assertThat(header(response, HeaderUtil.PARAMS)).isEqualTo("functionality");
    }

    private void assertFoundScenarios(int functionalityId, Long[] scenarioIds) {
        final ResponseEntity<List<ScenarioDTO>> response = cut.getScenarios(PROJECT_CODE, functionalityId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().stream().map(ScenarioDTO::getId)).containsExactly(scenarioIds);
    }

}
