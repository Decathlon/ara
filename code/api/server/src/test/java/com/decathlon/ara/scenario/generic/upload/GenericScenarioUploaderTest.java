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

package com.decathlon.ara.scenario.generic.upload;

import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.domain.Source;
import com.decathlon.ara.scenario.common.upload.ScenarioUploader;
import com.decathlon.ara.scenario.generic.bean.GenericExecutedScenarioReport;
import com.decathlon.ara.scenario.generic.bean.description.GenericExecutedScenarioDescription;
import com.decathlon.ara.scenario.generic.bean.feature.GenericExecutedScenarioFeature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenericScenarioUploaderTest {

    @Mock
    private ScenarioUploader scenarioUploader;

    @InjectMocks
    private GenericScenarioUploader genericScenarioUploader;

    @Test
    void convertGenericReportToScenario_returnScenario() {
        // Given
        Source source = mock(Source.class);
        GenericExecutedScenarioReport report = mock(GenericExecutedScenarioReport.class);
        GenericExecutedScenarioFeature feature = mock(GenericExecutedScenarioFeature.class);
        GenericExecutedScenarioDescription description = mock(GenericExecutedScenarioDescription.class);

        // When
        when(report.isIgnored()).thenReturn(true);
        when(report.getSeverity()).thenReturn("high");
        when(report.getFunctionalitiesName()).thenReturn("functionality names");
        when(report.getTagsAsString()).thenReturn("tag1 tag2 tag3");
        when(report.getCountryCodesAsString()).thenReturn("fr,be,nl");
        when(report.getFeature()).thenReturn(feature);
        when(feature.getFileName()).thenReturn("file.test");
        when(feature.getName()).thenReturn("feature_name");
        when(feature.getTagsAsString()).thenReturn("f_tag1 f_tag2");
        when(report.getDescription()).thenReturn(description);
        when(description.getStepsContent()).thenReturn("step_content");
        when(description.getStartLineNumber()).thenReturn(12);

        // Then
        Scenario scenario = genericScenarioUploader.convertGenericReportToScenario(report, source);
        assertThat(scenario)
                .extracting(
                        "ignored",
                        "severity",
                        "name",
                        "tags",
                        "countryCodes",
                        "featureFile",
                        "featureName",
                        "featureTags",
                        "content",
                        "line"
                )
                .contains(
                        true,
                        "high",
                        "functionality names",
                        "tag1 tag2 tag3",
                        "fr,be,nl",
                        "file.test",
                        "feature_name",
                        "f_tag1 f_tag2",
                        "step_content",
                        12
                );
    }
}
