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

package com.decathlon.ara.scenario.karate.upload;

import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.domain.Source;
import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.scenario.common.upload.ScenarioUploader;
import com.decathlon.ara.scenario.karate.bean.KarateExecutedScenarioReport;
import com.decathlon.ara.scenario.karate.bean.description.KarateExecutedScenarioDescription;
import com.decathlon.ara.scenario.karate.bean.feature.KarateExecutedScenarioFeature;
import com.decathlon.ara.service.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class KarateScenarioUploader {

    private final ScenarioUploader uploader;

    public KarateScenarioUploader(ScenarioUploader uploader) {
        this.uploader = uploader;
    }

    public void upload(Long projectId, String sourceCode, List<KarateExecutedScenarioReport> karateReports) throws BadRequestException {
        uploader.processUploadedContent(projectId, sourceCode, Technology.KARATE, source -> {
            if (CollectionUtils.isEmpty(karateReports)) {
                return new ArrayList<>();
            }
            return karateReports.stream()
                    .map(report -> convertkarateReportToScenario(report, source))
                    .toList();
        });
    }

    public Scenario convertkarateReportToScenario(KarateExecutedScenarioReport karateReport, Source source) {
        Scenario scenario = new Scenario();
        scenario.setSource(source);
        if (karateReport != null) {
            scenario.setIgnored(karateReport.isIgnored());
            scenario.setSeverity(karateReport.getSeverity());
            scenario.setName(karateReport.getFunctionalitiesName());
            scenario.setTags(karateReport.getTagsAsString());
            scenario.setCountryCodes(karateReport.getCountryCodesAsString());

            KarateExecutedScenarioFeature feature = karateReport.getFeature();
            if (feature != null) {
                scenario.setFeatureFile(feature.getFileName());
                scenario.setFeatureName(feature.getName());
                scenario.setFeatureTags(feature.getTagsAsString());
            }

            KarateExecutedScenarioDescription description = karateReport.getDescription();
            if (description != null) {
                scenario.setContent(description.getStepsContent());
                scenario.setLine(description.getStartLineNumber());
            }
        }

        return scenario;
    }
}
