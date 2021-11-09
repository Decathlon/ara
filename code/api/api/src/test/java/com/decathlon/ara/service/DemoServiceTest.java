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

import static com.decathlon.ara.loader.DemoLoaderConstants.PROJECT_CODE_DEMO;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import com.decathlon.ara.configuration.security.TestAuthentication;
import com.decathlon.ara.domain.enumeration.MemberRole;
import com.decathlon.ara.loader.DemoExecutionLoader;
import com.decathlon.ara.loader.DemoFunctionalityLoader;
import com.decathlon.ara.loader.DemoProblemLoader;
import com.decathlon.ara.loader.DemoScenarioLoader;
import com.decathlon.ara.loader.DemoSettingsLoader;
import com.decathlon.ara.service.dto.member.MemberDTO;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class DemoServiceTest {

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
    
    @Mock
    private ProjectUserMemberService projectUserMemberService;

    @InjectMocks
    private DemoService cut;

    @Test
    void create_ShouldAddAdminRoleToCurrentUser_WhenDemoProjectAlreadyExists() throws BadRequestException {
        // GIVEN
        TestAuthentication authentication = new TestAuthentication();
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(projectService.findOne(PROJECT_CODE_DEMO)).thenReturn(Optional.of(new ProjectDTO()));

        // WHEN
        cut.create();
        
        //THEN
        ArgumentCaptor<MemberDTO> captor = ArgumentCaptor.forClass(MemberDTO.class);
        verify(projectUserMemberService).addMember(eq(PROJECT_CODE_DEMO), captor.capture());
        MemberDTO member = captor.getValue();
        assertEquals(authentication.getName(), member.getName());
        assertEquals(MemberRole.ADMIN, member.getRole());
    }

    @Test
    void delete_ShouldFail_WhenDemoProjectDoesNotExist() throws NotFoundException {
        // GIVEN
        when(projectService.toId(PROJECT_CODE_DEMO)).thenThrow(NotFoundException.class);

        // WHEN
        assertThrows(NotFoundException.class, () -> cut.delete());
    }

}
