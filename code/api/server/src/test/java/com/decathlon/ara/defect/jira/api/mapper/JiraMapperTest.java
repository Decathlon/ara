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

package com.decathlon.ara.defect.jira.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.defect.bean.Defect;
import com.decathlon.ara.defect.jira.api.model.JiraFields;
import com.decathlon.ara.defect.jira.api.model.JiraIssue;
import com.decathlon.ara.defect.jira.api.model.status.JiraStatus;
import com.decathlon.ara.defect.jira.api.model.status.JiraStatusCategory;
import com.decathlon.ara.domain.enumeration.ProblemStatus;

@ExtendWith(MockitoExtension.class)
public class JiraMapperTest {

    @InjectMocks
    private JiraMapper jiraMapper;

    @Test
    public void toDefect_returnDefectWithId_whenJiraIssueHasKey() {
        // Given
        JiraIssue jiraIssue = mock(JiraIssue.class);
        String key = "PRJ-XX";

        // When
        when(jiraIssue.getKey()).thenReturn(key);

        // Then
        Defect defect = jiraMapper.toDefect(jiraIssue);
        assertThat(defect).isNotNull();
        assertThat(defect.getId()).isEqualTo(key);
    }

    @Test
    public void toDefect_returnDefectWithoutCloseDateTime_whenJiraIssueHasNoResolutionDate() {
        // Given
        JiraIssue jiraIssue = mock(JiraIssue.class);
        JiraFields jiraFields = mock(JiraFields.class);

        // When
        when(jiraIssue.getFields()).thenReturn(jiraFields);
        when(jiraFields.getResolutionDate()).thenReturn(null);

        // Then
        Defect defect = jiraMapper.toDefect(jiraIssue);
        assertThat(defect).isNotNull();
        assertThat(defect.getCloseDateTime()).isNull();
    }

    @Test
    public void toDefect_returnDefectWithCloseDateTime_whenJiraIssueHasAResolutionDate() throws ParseException {
        // Given
        JiraIssue jiraIssue = mock(JiraIssue.class);
        JiraFields jiraFields = mock(JiraFields.class);

        ZonedDateTime resolutionDate = ZonedDateTime.of(2020, 12, 9, 6, 3, 2, 500, ZoneId.systemDefault());

        // When
        when(jiraIssue.getFields()).thenReturn(jiraFields);
        when(jiraFields.getResolutionDate()).thenReturn(resolutionDate);

        // Then
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date expectedDate = sdf.parse("2020/12/09 06:03:02");

        Defect defect = jiraMapper.toDefect(jiraIssue);
        assertThat(defect).isNotNull();
        assertThat(defect.getCloseDateTime()).isNotNull();
        assertThat(defect.getCloseDateTime()).isEqualTo(expectedDate);
    }

    @Test
    public void toDefect_returnOpenDefect_whenJiraIssueHasUndefinedStatus() {
        // Given
        JiraIssue jiraIssue = mock(JiraIssue.class);
        JiraFields jiraFields = mock(JiraFields.class);
        JiraStatus jiraStatus = mock(JiraStatus.class);
        JiraStatusCategory jiraStatusCategory = mock(JiraStatusCategory.class);

        // When
        when(jiraIssue.getFields()).thenReturn(jiraFields);
        when(jiraFields.getStatus()).thenReturn(jiraStatus);
        when(jiraStatus.getCategory()).thenReturn(jiraStatusCategory);
        when(jiraStatusCategory.getKey()).thenReturn(JiraStatusCategory.UNDEFINED);

        // Then
        Defect defect = jiraMapper.toDefect(jiraIssue);
        assertThat(defect).isNotNull();
        assertThat(defect.getStatus()).isNotNull();
        assertThat(defect.getStatus()).isEqualTo(ProblemStatus.OPEN);
    }

    @Test
    public void toDefect_returnOpenDefect_whenJiraIssueHasIndeterminateStatus() {
        // Given
        JiraIssue jiraIssue = mock(JiraIssue.class);
        JiraFields jiraFields = mock(JiraFields.class);
        JiraStatus jiraStatus = mock(JiraStatus.class);
        JiraStatusCategory jiraStatusCategory = mock(JiraStatusCategory.class);

        // When
        when(jiraIssue.getFields()).thenReturn(jiraFields);
        when(jiraFields.getStatus()).thenReturn(jiraStatus);
        when(jiraStatus.getCategory()).thenReturn(jiraStatusCategory);
        when(jiraStatusCategory.getKey()).thenReturn(JiraStatusCategory.INDETERMINATE);

        // Then
        Defect defect = jiraMapper.toDefect(jiraIssue);
        assertThat(defect).isNotNull();
        assertThat(defect.getStatus()).isNotNull();
        assertThat(defect.getStatus()).isEqualTo(ProblemStatus.OPEN);
    }

    @Test
    public void toDefect_returnOpenDefect_whenJiraIssueHasNewStatus() {
        // Given
        JiraIssue jiraIssue = mock(JiraIssue.class);
        JiraFields jiraFields = mock(JiraFields.class);
        JiraStatus jiraStatus = mock(JiraStatus.class);
        JiraStatusCategory jiraStatusCategory = mock(JiraStatusCategory.class);

        // When
        when(jiraIssue.getFields()).thenReturn(jiraFields);
        when(jiraFields.getStatus()).thenReturn(jiraStatus);
        when(jiraStatus.getCategory()).thenReturn(jiraStatusCategory);
        when(jiraStatusCategory.getKey()).thenReturn(JiraStatusCategory.NEW);

        // Then
        Defect defect = jiraMapper.toDefect(jiraIssue);
        assertThat(defect).isNotNull();
        assertThat(defect.getStatus()).isNotNull();
        assertThat(defect.getStatus()).isEqualTo(ProblemStatus.OPEN);
    }

    @Test
    public void toDefect_returnClosedDefect_whenJiraIssueHasDoneStatus() {
        // Given
        JiraIssue jiraIssue = mock(JiraIssue.class);
        JiraFields jiraFields = mock(JiraFields.class);
        JiraStatus jiraStatus = mock(JiraStatus.class);
        JiraStatusCategory jiraStatusCategory = mock(JiraStatusCategory.class);

        // When
        when(jiraIssue.getFields()).thenReturn(jiraFields);
        when(jiraFields.getStatus()).thenReturn(jiraStatus);
        when(jiraStatus.getCategory()).thenReturn(jiraStatusCategory);
        when(jiraStatusCategory.getKey()).thenReturn(JiraStatusCategory.DONE);

        // Then
        Defect defect = jiraMapper.toDefect(jiraIssue);
        assertThat(defect).isNotNull();
        assertThat(defect.getStatus()).isNotNull();
        assertThat(defect.getStatus()).isEqualTo(ProblemStatus.CLOSED);
    }

    @Test
    public void toDefects_returnEmptyList_whenJiraIssuesEmpty() {
        // Given

        // When

        // Then
        List<Defect> defects = jiraMapper.toDefects(null);
        assertThat(defects)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void toDefects_returnDefects_whenJiraIssuesNotEmpty() {
        // Given
        JiraIssue jiraIssue1 = mock(JiraIssue.class);
        String key1 = "PRJ-1";

        JiraIssue jiraIssue2 = mock(JiraIssue.class);
        String key2 = "PRJ-2";

        JiraIssue jiraIssue3 = mock(JiraIssue.class);
        String key3 = "PRJ-3";

        // When
        when(jiraIssue1.getKey()).thenReturn(key1);
        when(jiraIssue2.getKey()).thenReturn(key2);
        when(jiraIssue3.getKey()).thenReturn(key3);

        // Then
        List<Defect> defects = jiraMapper.toDefects(Arrays.asList(jiraIssue1, jiraIssue2, jiraIssue3));
        assertThat(defects)
                .isNotEmpty()
                .hasSize(3)
                .extracting("id")
                .contains(
                        key1,
                        key2,
                        key3
                );
    }
}
