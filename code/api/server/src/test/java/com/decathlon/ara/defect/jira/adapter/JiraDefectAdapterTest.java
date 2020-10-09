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

package com.decathlon.ara.defect.jira.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.Entities;
import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.defect.bean.Defect;
import com.decathlon.ara.defect.jira.api.JiraRestClient;
import com.decathlon.ara.defect.jira.api.mapper.JiraMapper;
import com.decathlon.ara.defect.jira.api.model.JiraIssue;
import com.decathlon.ara.service.SettingProviderService;
import com.decathlon.ara.service.dto.setting.SettingDTO;
import com.decathlon.ara.service.exception.BadRequestException;

@ExtendWith(MockitoExtension.class)
public class JiraDefectAdapterTest {

    @Mock
    private SettingProviderService settingProviderService;

    @Mock
    private JiraRestClient jiraRestClient;

    @Mock
    private JiraMapper jiraMapper;

    @InjectMocks
    private JiraDefectAdapter jiraDefectAdapter;

    @Test
    public void getStatuses_throwFetchException_whenBadRequestExceptionRaised() throws BadRequestException, FetchException {
        // Given
        Long projectId = 1L;
        List<String> jiraIssueKeys = Arrays.asList("PRJ-1", "PRJ-2", "PRJ-3");

        // When
        when(jiraRestClient.getIssuesFromKeys(projectId, jiraIssueKeys)).thenThrow(new BadRequestException("Jira issues not fetched due to internal error!", Entities.SETTING, "some_error_code"));

        // Then
        assertThrows(FetchException.class, () -> jiraDefectAdapter.getStatuses(projectId, jiraIssueKeys));
    }

    @Test
    public void getStatuses_returnDefects_whenNoErrorFound() throws BadRequestException, FetchException {
        // Given
        Long projectId = 1L;
        List<String> jiraIssueKeys = Arrays.asList("PRJ-1", "PRJ-2", "PRJ-3");

        JiraIssue jiraIssue1 = mock(JiraIssue.class);
        JiraIssue jiraIssue2 = mock(JiraIssue.class);
        JiraIssue jiraIssue3 = mock(JiraIssue.class);
        List<JiraIssue> jiraIssues = Arrays.asList(jiraIssue1, jiraIssue2, jiraIssue3);

        Defect defect1 = mock(Defect.class);
        Defect defect2 = mock(Defect.class);
        Defect defect3 = mock(Defect.class);
        List<Defect> defects = Arrays.asList(defect1, defect2, defect3);

        // When
        when(jiraRestClient.getIssuesFromKeys(projectId, jiraIssueKeys)).thenReturn(jiraIssues);
        when(jiraMapper.toDefects(jiraIssues)).thenReturn(defects);

        // Then
        List<Defect> results = jiraDefectAdapter.getStatuses(projectId, jiraIssueKeys);
        assertThat(results)
                .hasSize(3)
                .contains(
                        defect1,
                        defect2,
                        defect3
                );
        verify(jiraRestClient).getIssuesFromKeys(projectId, Arrays.asList("PRJ-1", "PRJ-2", "PRJ-3"));
    }

    @Test
    public void getStatuses_returnFilteredDefects_whenSomeKeysAreInvalid() throws BadRequestException, FetchException {
        // Given
        Long projectId = 1L;
        List<String> jiraIssueKeys = Arrays.asList("INVALID_KEY", "PRJ-1", "PRJ-2", "42", "PRJ-3");
        List<String> filteredJiraIssueKeys = Arrays.asList("PRJ-1", "PRJ-2", "PRJ-3");

        JiraIssue jiraIssue1 = mock(JiraIssue.class);
        JiraIssue jiraIssue2 = mock(JiraIssue.class);
        JiraIssue jiraIssue3 = mock(JiraIssue.class);
        List<JiraIssue> jiraIssues = Arrays.asList(jiraIssue1, jiraIssue2, jiraIssue3);

        Defect defect1 = mock(Defect.class);
        Defect defect2 = mock(Defect.class);
        Defect defect3 = mock(Defect.class);
        List<Defect> defects = Arrays.asList(defect1, defect2, defect3);

        // When
        when(jiraRestClient.getIssuesFromKeys(projectId, filteredJiraIssueKeys)).thenReturn(jiraIssues);
        when(jiraMapper.toDefects(jiraIssues)).thenReturn(defects);

        // Then
        List<Defect> results = jiraDefectAdapter.getStatuses(projectId, jiraIssueKeys);
        assertThat(results)
                .hasSize(3)
                .contains(
                        defect1,
                        defect2,
                        defect3
                );
        verify(jiraRestClient).getIssuesFromKeys(projectId, filteredJiraIssueKeys);
    }

    @Test
    public void getChangedDefects_throwFetchException_whenBadRequestExceptionRaised() throws BadRequestException, FetchException, ParseException {
        // Given
        Long projectId = 1L;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date startDate = sdf.parse("2020/06/04 02:01:03");

        // When
        when(jiraRestClient.getUpdatedIssues(projectId, startDate)).thenThrow(new BadRequestException("Jira issues not fetched due to internal error!", Entities.SETTING, "some_error_code"));

        // Then
        assertThrows(FetchException.class, () -> jiraDefectAdapter.getChangedDefects(projectId, startDate));
    }

    @Test
    public void getChangedDefects_returnDefects_whenNoErrorFound() throws BadRequestException, FetchException, ParseException {
        // Given
        Long projectId = 1L;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date startDate = sdf.parse("2020/06/04 02:01:03");

        JiraIssue jiraIssue1 = mock(JiraIssue.class);
        JiraIssue jiraIssue2 = mock(JiraIssue.class);
        JiraIssue jiraIssue3 = mock(JiraIssue.class);
        List<JiraIssue> jiraIssues = Arrays.asList(jiraIssue1, jiraIssue2, jiraIssue3);

        Defect defect1 = mock(Defect.class);
        Defect defect2 = mock(Defect.class);
        Defect defect3 = mock(Defect.class);
        List<Defect> defects = Arrays.asList(defect1, defect2, defect3);

        // When
        when(jiraRestClient.getUpdatedIssues(projectId, startDate)).thenReturn(jiraIssues);
        when(jiraMapper.toDefects(jiraIssues)).thenReturn(defects);

        // Then
        List<Defect> results = jiraDefectAdapter.getChangedDefects(projectId, startDate);
        assertThat(results)
                .hasSize(3)
                .contains(
                        defect1,
                        defect2,
                        defect3
                );
    }

    @Test
    public void getCode_returnCode() {
        // Given
        String expectedCode = "jira";

        // When

        // Then
        String actualCode = jiraDefectAdapter.getCode();
        assertThat(actualCode).isEqualTo(expectedCode);
    }

    @Test
    public void getName_returnName() {
        // Given
        String expectedName = "Jira";

        // When

        // Then
        String actualName = jiraDefectAdapter.getName();
        assertThat(actualName).isEqualTo(expectedName);
    }

    @Test
    public void getSettingDefinitions_returnSettings() {
        // Given
        SettingDTO setting1 = mock(SettingDTO.class);
        SettingDTO setting2 = mock(SettingDTO.class);
        SettingDTO setting3 = mock(SettingDTO.class);
        SettingDTO setting4 = mock(SettingDTO.class);
        SettingDTO setting5 = mock(SettingDTO.class);

        // When
        when(settingProviderService.getDefectJiraDefinitions()).thenReturn(
                Arrays.asList(
                        setting1,
                        setting2,
                        setting3,
                        setting4,
                        setting5
                )
        );

        // Then
        List<SettingDTO> settings = jiraDefectAdapter.getSettingDefinitions();
        assertThat(settings)
                .hasSize(5)
                .contains(
                        setting1,
                        setting2,
                        setting3,
                        setting4,
                        setting5
                );
    }

}
