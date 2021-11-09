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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.decathlon.ara.domain.Communication;
import com.decathlon.ara.domain.RootCause;
import com.decathlon.ara.repository.CommunicationRepository;
import com.decathlon.ara.repository.RootCauseRepository;
import com.decathlon.ara.service.dto.project.ProjectDTO;
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
@DatabaseSetup("/dbunit/project.xml")
class ProjectResourceIT {

    @Autowired
    private ProjectResource cut;

    @Autowired
    private CommunicationRepository communicationRepository;

    @Autowired
    private RootCauseRepository rootCauseRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void create_ShouldInsertCommunications_WhenCreatingAProject() {
        // GIVEN
        final ProjectDTO project = new ProjectDTO(null, "new-code", "any", false);

        // WHEN
        ResponseEntity<ProjectDTO> response = cut.create(project);
        entityManager.flush();

        // THEN
        final long createdProjectId = response.getBody().getId().longValue();
        final List<Communication> communications = communicationRepository.findAllByProjectIdOrderByCode(createdProjectId);
        assertThat(communications).hasSize(3);
        assertThat(communications.stream().map(Communication::getCode)).containsOnly(
                "executions",
                "scenario-writing-helps",
                "how" + "to-add-scenario");
    }

    @Test
    void create_ShouldInsertDefaultRootCauses_WhenCreatingAProject() {
        // GIVEN
        final ProjectDTO project = new ProjectDTO(null, "new-code", "any", false);

        // WHEN
        ResponseEntity<ProjectDTO> response = cut.create(project);
        entityManager.flush();

        // THEN
        final long createdProjectId = response.getBody().getId().longValue();
        final List<RootCause> rootCauses = rootCauseRepository.findAllByProjectIdOrderByName(createdProjectId);
        assertThat(rootCauses.stream().map(RootCause::getName)).containsExactly(
                "Fragile test",
                "Network issue",
                "Regression",
                "Test to update");
    }

    @Test
    void update_ShouldNotDeleteCommunications_WhenCalled() {
        // GIVEN
        final String code = "project-y";
        final ProjectDTO updatedProjectProperties = new ProjectDTO(null, "any", "any", false);

        // WHEN
        cut.update(code, updatedProjectProperties);

        // THEN
        assertThat(communicationRepository.findAllByProjectIdOrderByCode(1l)).hasSize(1);
    }


}
