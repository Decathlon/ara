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

package com.decathlon.ara.web.rest.advice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResponseDtoValidatorTest {

    @Mock
    private WebRequest request;

    @InjectMocks
    private ResponseDtoValidator cut;

    @Test
    void getResourceName_ShouldReturnSubResourceOfProject_WhenUrlHasASubResourceOfProject() {
        // GIVEN
        when(request.getDescription(false))
                .thenReturn("uri=/api/projects/some-project/some-resources/api");

        // WHEN
        final String resourceName = cut.getResourceName(request);

        // THEN
        assertThat(resourceName).isEqualTo("some-resources");
    }

    @Test
    void getResourceName_ShouldReturnProjects_WhenUrlIsAProjectUrl() {
        // GIVEN
        when(request.getDescription(false))
                .thenReturn("uri=/api/projects/some-project");

        // WHEN
        final String resourceName = cut.getResourceName(request);

        // THEN
        assertThat(resourceName).isEqualTo("projects");
    }

}
