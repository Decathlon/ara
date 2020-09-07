/******************************************************************************
 * Copyright (C) 2020 by the ARA Contributors                                 *
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

package com.decathlon.ara.scenario.common.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.scenario.common.indexer.ScenariosIndexer;
import com.decathlon.ara.scenario.cucumber.indexer.CucumberScenariosIndexer;
import com.decathlon.ara.scenario.cypress.indexer.CypressScenariosIndexer;
import com.decathlon.ara.scenario.postman.indexer.PostmanScenariosIndexer;

@ExtendWith(MockitoExtension.class)
public class ScenariosIndexerStrategyTest {

    @Mock
    private PostmanScenariosIndexer postmanScenariosIndexer;

    @Mock
    private CucumberScenariosIndexer cucumberScenariosIndexer;

    @Mock
    private CypressScenariosIndexer cypressScenariosIndexer;

    @InjectMocks
    private ScenariosIndexerStrategy scenariosIndexerStrategy;

    @Test
    public void getScenariosIndexer_returnEmpty_whenTechnologyIsNull(){
        // Given

        // When

        // Then
        Optional<ScenariosIndexer> scenariosIndexer = scenariosIndexerStrategy.getScenariosIndexer(null);
        assertThat(scenariosIndexer).isEmpty();
    }

    @Test
    public void getScenariosIndexer_returnCucumberIndexer_whenTechnologyIsCucumber(){
        // Given

        // When

        // Then
        Optional<ScenariosIndexer> scenariosIndexer = scenariosIndexerStrategy.getScenariosIndexer(Technology.CUCUMBER);
        assertThat(scenariosIndexer).isEqualTo(Optional.of(cucumberScenariosIndexer));
    }

    @Test
    public void getScenariosIndexer_returnPostmanIndexer_whenTechnologyIsPostman(){
        // Given

        // When

        // Then
        Optional<ScenariosIndexer> scenariosIndexer = scenariosIndexerStrategy.getScenariosIndexer(Technology.POSTMAN);
        assertThat(scenariosIndexer).isEqualTo(Optional.of(postmanScenariosIndexer));
    }

    @Test
    public void getScenariosIndexer_returnCypressIndexer_whenTechnologyIsCypress(){
        // Given

        // When

        // Then
        Optional<ScenariosIndexer> scenariosIndexer = scenariosIndexerStrategy.getScenariosIndexer(Technology.CYPRESS);
        assertThat(scenariosIndexer).isEqualTo(Optional.of(cypressScenariosIndexer));
    }
}
