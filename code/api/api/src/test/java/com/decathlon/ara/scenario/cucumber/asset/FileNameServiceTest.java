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

package com.decathlon.ara.scenario.cucumber.asset;

import com.decathlon.ara.service.util.DateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.decathlon.ara.util.TestUtil.timestamp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileNameServiceTest {

    @Mock
    private DateService dateService;

    @InjectMocks
    private FileNameService cut;

    @Test
    void generateReportFileName_ShouldGenerateADatedFileName_WhenCalledWithAnExtension() {
        // GIVEN
        when(dateService.now()).thenReturn(timestamp(2018, 1, 1, 1, 1, 1));

        // WHEN
        final String fileName = cut.generateReportFileName("Name", "ext");

        // THEN
        assertThat(fileName).isEqualTo("2018.02.01-01h01m01.000-Name.ext");
    }

    @Test
    void generateReportFileName_ShouldGenerateADatedDirectoryName_WhenCalledWithoutExtension() {
        // GIVEN
        when(dateService.now()).thenReturn(timestamp(2018, 1, 1, 1, 1, 1));

        // WHEN
        final String fileName = cut.generateReportFileName("Name", null);

        // THEN
        assertThat(fileName).isEqualTo("2018.02.01-01h01m01.000-Name");
    }

    @Test
    void generateReportFileName_ShouldTruncateName_WhenCalledWithALargeScenarioName() {
        // GIVEN
        when(dateService.now()).thenReturn(timestamp(2018, 1, 1, 1, 1, 1));

        // WHEN
        final String fileName = cut.generateReportFileName("" +
                "Veeeeeeeeeeeeeeeeeeeeerrrrrrrrrrrrrrrrrrrrrryyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy" +
                "Loooooooooooooooooooooooooooooooonnnnnnnnnnnnnnnng" +
                "Name", null);

        // THEN
        assertThat(fileName).isEqualTo("2018.02.01-01h01m01.000-VeeeeeeeeeeeeeeeeeeeeerrrrrrrrrrrrrrrrrrrrrryyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyLoooooooooooooooooooooooooooooooonnnnnnnnnnnnnnnngN");
    }

    @Test
    void generateReportFileName_ShouldReplaceSpacesWithDashes_WhenScenarioNameHasSpaces() {
        // GIVEN
        when(dateService.now()).thenReturn(timestamp(2018, 1, 1, 1, 1, 1));

        // WHEN
        final String fileName = cut.generateReportFileName("Scenario Name", null);

        // THEN
        assertThat(fileName).isEqualTo("2018.02.01-01h01m01.000-Scenario-Name");
    }

    @Test
    void generateReportFileName_ShouldRemoveNonAlphaNumericCharacters_WhenScenarioNameHasSpecialCharacters() {
        // GIVEN
        when(dateService.now()).thenReturn(timestamp(2018, 1, 1, 1, 1, 1));

        // WHEN
        final String fileName = cut.generateReportFileName("Scenario+Name!042", null);

        // THEN
        assertThat(fileName).isEqualTo("2018.02.01-01h01m01.000-ScenarioName042");
    }

}
