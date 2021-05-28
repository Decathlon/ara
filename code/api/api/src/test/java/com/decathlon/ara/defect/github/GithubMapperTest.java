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

package com.decathlon.ara.defect.github;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class GithubMapperTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GithubMapper cut;

    @Test
    public void jsonToIssue_should_return_object() throws IOException {
        // Given
        String json = "{\"key\": 42 }";
        GithubIssue issue = new GithubIssue();
        issue.setNumber(42);
        issue.setTitle("Test");
        issue.setState("open");
        Mockito.doReturn(issue).when(this.objectMapper).readValue(json, GithubMapper.TYPE_REFERENCE_TO_GITHUB_ISSUE);
        // When
        Optional<GithubIssue> result = this.cut.jsonToIssue(json);
        // Then
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get()).isNotNull();
        Assertions.assertThat(result.get().getNumber()).isEqualTo(42L);
        Assertions.assertThat(result.get().getTitle()).isEqualTo("Test");
        Assertions.assertThat(result.get().getState()).isEqualTo("open");
    }

    @Test
    public void jsonToIssue_should_return_empty_on_error() throws IOException {
        // Given
        String json = "{\"key\": 42 }";
        Mockito.doThrow(JsonParseException.class).when(this.objectMapper).readValue(json, GithubMapper.TYPE_REFERENCE_TO_GITHUB_ISSUE);
        // When
        Optional<GithubIssue> result = this.cut.jsonToIssue(json);
        // Then
        Assertions.assertThat(result).isNotPresent();
    }

    @Test
    public void jsonToIssueList_should_return_a_filled_list() throws IOException {
        // Given
        String json = "[ {\"key\": 42 }, {\"key\": 24 } ]";
        GithubIssue issue1 = new GithubIssue();
        issue1.setNumber(42);
        issue1.setState("open");
        GithubIssue issue2 = new GithubIssue();
        issue2.setNumber(24);
        issue2.setState("closed");
        List<GithubIssue> issueList = Lists.list(issue1, issue2);
        Mockito.doReturn(issueList).when(this.objectMapper).readValue(json, GithubMapper.TYPE_REFERENCE_TO_LIST_GITHUB_ISSUE);
        // When
        List<GithubIssue> githubIssues = this.cut.jsonToIssueList(json);
        // Then
        Assertions.assertThat(githubIssues).isNotNull();
        Assertions.assertThat(githubIssues).hasSize(2);
        Assertions.assertThat(githubIssues.get(0).getNumber()).isEqualTo(42);
        Assertions.assertThat(githubIssues.get(1).getNumber()).isEqualTo(24);
    }

    @Test
    public void jsonToIssueList_should_return_an_empty_list_on_error() throws IOException {
        // Given
        String json = "[ {\"key\": 42 }, {\"key\": 24 } ]";
        Mockito.doThrow(JsonParseException.class).when(this.objectMapper).readValue(json, GithubMapper.TYPE_REFERENCE_TO_LIST_GITHUB_ISSUE);
        // When
        List<GithubIssue> githubIssues = this.cut.jsonToIssueList(json);
        // Then
        Assertions.assertThat(githubIssues).isNotNull();
        Assertions.assertThat(githubIssues).isEmpty();
    }
}