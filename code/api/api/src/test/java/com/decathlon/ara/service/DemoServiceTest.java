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

package com.decathlon.ara.service;

import com.decathlon.ara.loader.*;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.decathlon.ara.loader.DemoLoaderConstants.PROJECT_CODE_DEMO;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private SettingService settingService;

    @Mock
    private DemoExecutionLoader demoExecutionLoader;

    @Mock
    private DemoFunctionalityLoader demoFunctionalityLoader;

    @Mock
    private DemoProblemLoader demoProblemLoader;

    @Mock
    private DemoScenarioLoader demoScenarioLoader;

    @Mock
    private DemoSettingsLoader demoSettingsLoader;

    @InjectMocks
    private DemoService cut;

    @Test
    void create_ShouldFail_WhenDemoProjectAlreadyExists() {
        // GIVEN
        when(projectService.findOne(PROJECT_CODE_DEMO)).thenReturn(Optional.of(new ProjectDTO()));

        // WHEN
        assertThrows(BadRequestException.class, () -> cut.create());
    }

    @Test
    void delete_ShouldFail_WhenDemoProjectDoesNotExist() {
        // GIVEN
        when(projectRepository.findOneByCode(PROJECT_CODE_DEMO)).thenReturn(null);

        // WHEN
        assertThrows(NotFoundException.class, () -> cut.delete());
    }

}
