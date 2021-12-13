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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.domain.Source;
import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.scenario.common.upload.ScenarioUploader;
import com.decathlon.ara.scenario.generic.bean.GenericExecutedScenarioReport;
import com.decathlon.ara.scenario.generic.bean.description.GenericExecutedScenarioDescription;
import com.decathlon.ara.scenario.generic.bean.feature.GenericExecutedScenarioFeature;
import com.decathlon.ara.service.exception.BadRequestException;

@Component
public class GenericScenarioUploader {

    private final ScenarioUploader uploader;

    public GenericScenarioUploader(ScenarioUploader uploader) {
        this.uploader = uploader;
    }

    public void upload(Long projectId, String sourceCode, List<GenericExecutedScenarioReport> genericReports) throws BadRequestException {
        uploader.processUploadedContent(projectId, sourceCode, Technology.GENERIC, source -> {
            if (CollectionUtils.isEmpty(genericReports)) {
                return new ArrayList<>();
            }
            return genericReports.stream()
                    .map(report -> convertGenericReportToScenario(report, source))
                    .collect(Collectors.toList());
        });
    }

    public Scenario convertGenericReportToScenario(GenericExecutedScenarioReport genericReport, Source source) {
        Scenario scenario = new Scenario();
        scenario.setSource(source);
        if (genericReport != null) {
            scenario.setIgnored(genericReport.isIgnored());
            scenario.setSeverity(genericReport.getSeverity());
            scenario.setName(genericReport.getFunctionalitiesName());
            scenario.setTags(genericReport.getTagsAsString());
            scenario.setCountryCodes(genericReport.getCountryCodesAsString());

            GenericExecutedScenarioFeature feature = genericReport.getFeature();
            if (feature != null) {
                scenario.setFeatureFile(feature.getFileName());
                scenario.setFeatureName(feature.getName());
                scenario.setFeatureTags(feature.getTagsAsString());
            }

            GenericExecutedScenarioDescription description = genericReport.getDescription();
            if (description != null) {
                scenario.setContent(description.getStepsContent());
                scenario.setLine(description.getStartLineNumber());
            }
        }

        return scenario;
    }
}
