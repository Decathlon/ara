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

import com.decathlon.ara.domain.enumeration.FunctionalitySeverity;
import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.service.dto.functionality.FunctionalityWithChildrenDTO;
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
public class FunctionalityResourceGetAllIT {

    private static final String PROJECT_CODE = "p";

    @Autowired
    private FunctionalityResource cut;

    @Test
    public void testGetAll() {
        ResponseEntity<List<FunctionalityWithChildrenDTO>> response = cut.getAll(PROJECT_CODE);
        final List<FunctionalityWithChildrenDTO> tree = response.getBody();

        // Root-level nodes (check first node completely, and other nodes superficially to just check correct order)
        assertThat(tree).hasSize(3);
        assertThat(tree.get(0).getId()).isEqualTo(1);
        assertThat(tree.get(0).getParentId()).isNull();
        assertThat(tree.get(0).getOrder()).isEqualTo(1000);
        assertThat(tree.get(0).getType()).isEqualTo(FunctionalityType.FOLDER.name());
        assertThat(tree.get(0).getName()).isEqualTo("F 1");
        assertThat(tree.get(1).getId()).isEqualTo(2);
        assertThat(tree.get(1).getOrder()).isEqualTo(2000);
        assertThat(tree.get(1).getName()).isEqualTo("F 2");
        assertThat(tree.get(2).getId()).isEqualTo(3);

        // Direct-children-level nodes
        assertThat(tree.get(0).getChildren()).hasSize(2);
        assertThat(tree.get(0).getChildren().get(0).getId()).isEqualTo(11);
        assertThat(tree.get(0).getChildren().get(0).getParentId()).isEqualTo(1);
        assertThat(tree.get(0).getChildren().get(0).getOrder()).isEqualTo(1000);
        assertThat(tree.get(0).getChildren().get(0).getType()).isEqualTo(FunctionalityType.FOLDER.name());
        assertThat(tree.get(0).getChildren().get(0).getName()).isEqualTo("F 1.1");
        assertThat(tree.get(1).getChildren()).hasSize(2);
        assertThat(tree.get(1).getChildren().get(0).getId()).isEqualTo(21);
        assertThat(tree.get(1).getChildren().get(1).getId()).isEqualTo(22);
        assertThat(tree.get(2).getChildren()).hasSize(1);
        assertThat(tree.get(2).getChildren().get(0).getId()).isEqualTo(31);

        // Grand-children-level nodes (check first functionality-node has all attributes)
        List<FunctionalityWithChildrenDTO> grandChildren = tree.get(0).getChildren().get(0).getChildren();
        assertThat(grandChildren).hasSize(3);
        assertThat(grandChildren.get(0).getId()).isEqualTo(111);
        assertThat(grandChildren.get(0).getParentId()).isEqualTo(11);
        assertThat(grandChildren.get(0).getOrder()).isEqualTo(1000);
        assertThat(grandChildren.get(0).getType()).isEqualTo(FunctionalityType.FUNCTIONALITY.name());
        assertThat(grandChildren.get(0).getName()).isEqualTo("F 1.1.1");
        assertThat(grandChildren.get(0).getCountryCodes()).isEqualTo("be,nl");
        assertThat(grandChildren.get(0).getTeamId()).isEqualTo(1);
        assertThat(grandChildren.get(0).getSeverity()).isEqualTo(FunctionalitySeverity.LOW.name());
        assertThat(grandChildren.get(0).getCreated()).isEqualTo("18.02");
        assertThat(grandChildren.get(0).getStarted()).isFalse();
        assertThat(grandChildren.get(0).getNotAutomatable()).isFalse();
        assertThat(grandChildren.get(0).getCoveredScenarios()).isEqualTo(5);
        assertThat(grandChildren.get(0).getCoveredCountryScenarios()).isEqualTo("API:cn=3,nl=1|WEB:be=2");
        assertThat(grandChildren.get(0).getIgnoredScenarios()).isEqualTo(2);
        assertThat(grandChildren.get(0).getIgnoredCountryScenarios()).isEqualTo("WEB:be=2");
        assertThat(grandChildren.get(0).getComment()).isEqualTo("Comment");
        assertThat(grandChildren.get(1).getId()).isEqualTo(112);
        assertThat(grandChildren.get(2).getId()).isEqualTo(113);
        assertThat(grandChildren.get(2).getCountryCodes()).isNull();
        assertThat(grandChildren.get(2).getTeamId()).isEqualTo(3);
        assertThat(grandChildren.get(2).getSeverity()).isNull();
        assertThat(grandChildren.get(2).getCreated()).isNull();
        assertThat(grandChildren.get(2).getStarted()).isNull();
        assertThat(grandChildren.get(2).getNotAutomatable()).isNull();
        assertThat(grandChildren.get(2).getCoveredScenarios()).isNull();
        assertThat(grandChildren.get(2).getCoveredCountryScenarios()).isNull();
        assertThat(grandChildren.get(2).getIgnoredScenarios()).isNull();
        assertThat(grandChildren.get(2).getIgnoredCountryScenarios()).isNull();
        assertThat(grandChildren.get(2).getComment()).isNull();
    }

}
