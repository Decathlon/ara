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

package com.decathlon.ara.scenario.cypress.upload;

import java.util.List;

import org.springframework.stereotype.Component;

import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.scenario.common.upload.ScenarioUploader;
import com.decathlon.ara.scenario.cucumber.bean.Feature;
import com.decathlon.ara.scenario.cucumber.util.ScenarioExtractorUtil;
import com.decathlon.ara.service.exception.BadRequestException;

@Component
public class CypressScenarioUploader {

    private final ScenarioUploader uploader;

    public CypressScenarioUploader(ScenarioUploader uploader) {
        this.uploader = uploader;
    }

    public void uploadScenarios(long projectId, String sourceCode, List<Feature> features) throws BadRequestException {
        uploader.processUploadedContent(
                projectId,
                sourceCode,
                Technology.CYPRESS,
                source -> ScenarioExtractorUtil.extractScenarios(source, features));
    }
}
