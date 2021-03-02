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

package com.decathlon.ara.scenario.generic.resource;

import com.decathlon.ara.scenario.generic.bean.GenericExecutedScenarioReport;
import com.decathlon.ara.scenario.generic.upload.GenericScenarioUploader;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenericResourceTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private GenericScenarioUploader genericScenarioUploader;

    @InjectMocks
    private GenericResource genericResource;

    @Test
    void uploadGenericScenarios_returnErrorResponse_whenExceptionThrown() throws BadRequestException {
        // Given
        String projectCode = "project-code";
        String sourceCode = "source-code";
        List<GenericExecutedScenarioReport> reports = mock(List.class);

        Long projectId = 1L;

        BadRequestException exception = mock(BadRequestException.class);

        // When
        when(projectService.toId(projectCode)).thenReturn(projectId);
        doThrow(exception).when(genericScenarioUploader).upload(projectId, sourceCode, reports);

        // Then
        ResponseEntity<Void> response = genericResource.uploadGenericScenarios(projectCode, sourceCode, reports);
        assertThat(response).isEqualTo(ResponseUtil.handle(exception));
    }

    @Test
    void uploadGenericScenarios_returnOkResponse_whenNoExceptionThrown() throws BadRequestException {
        // Given
        String projectCode = "project-code";
        String sourceCode = "source-code";
        List<GenericExecutedScenarioReport> reports = mock(List.class);

        Long projectId = 1L;

        // When
        when(projectService.toId(projectCode)).thenReturn(projectId);

        // Then
        ResponseEntity<Void> response = genericResource.uploadGenericScenarios(projectCode, sourceCode, reports);
        assertThat(response).isEqualTo(ResponseEntity.ok().build());
    }
}
