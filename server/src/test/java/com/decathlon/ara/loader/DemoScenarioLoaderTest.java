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

package com.decathlon.ara.loader;

import com.decathlon.ara.common.NotGonnaHappenException;
import com.decathlon.ara.scenario.cucumber.upload.CucumberScenarioUploader;
import com.decathlon.ara.scenario.postman.upload.PostmanScenarioUploader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DemoScenarioLoaderTest {

    @Mock
    private PostmanScenarioUploader postmanScenarioUploader;

    @Mock
    private CucumberScenarioUploader cucumberScenarioUploader;

    @Mock
    private DemoLoaderService demoLoaderService;

    @InjectMocks
    private DemoScenarioLoader cut;

    @Test
    public void getResourceAsUtf8String_ShouldReturnUtf8Content_WhenResourceExists() {
        // GIVEN
        String resource = "demo/resource";

        // WHEN
        final String content = cut.getResourceAsUtf8String(resource);

        // THEN
        assertThat(content.replaceAll("[\r\n]", "")).isEqualTo("content-withÜTF8");
    }

    @Test(expected = NotGonnaHappenException.class)
    public void getResourceAsUtf8String_ShouldThrowNotGonnaHappenException_WhenDeveloperFucksUpCodeOrPackaging() {
        cut.getResourceAsUtf8String("nonexistent");
    }

}
