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

package com.decathlon.ara.defect.jira.api;

import com.decathlon.ara.defect.jira.api.model.JiraIssue;
import com.decathlon.ara.defect.jira.api.model.JiraIssueSearchResults;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.support.Settings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JiraRestClientTest {

    @Mock
    private SettingService settingService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private JiraRestClient jiraRestClient;

    @Test
    void getHeader_throwBadRequestException_whenTokenNotFound() {
        // Given
        Long projectId = 1L;

        // When
        when(settingService.get(projectId, Settings.DEFECT_JIRA_TOKEN)).thenReturn(null);

        // Then
        assertThrows(BadRequestException.class, () -> jiraRestClient.getHeader(projectId));
    }

    @Test
    void getHeader_throwBadRequestException_whenLoginNotFound() {
        // Given
        Long projectId = 1L;
        String token = "my_jira_token";

        // When
        when(settingService.get(projectId, Settings.DEFECT_JIRA_TOKEN)).thenReturn(token);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_LOGIN)).thenReturn(null);

        // Then
        assertThrows(BadRequestException.class, () -> jiraRestClient.getHeader(projectId));
    }

    @Test
    void getHeader_returnHeader_whenLoginAndTokenFound() throws BadRequestException {
        // Given
        Long projectId = 1L;
        String token = "my_jira_token";
        String login = "my_login";

        // When
        when(settingService.get(projectId, Settings.DEFECT_JIRA_TOKEN)).thenReturn(token);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_LOGIN)).thenReturn(login);

        // Then
        String authorization = "Basic bXlfbG9naW46bXlfamlyYV90b2tlbg==";

        HttpHeaders header = jiraRestClient.getHeader(projectId);
        assertThat(header).hasSize(2);
        assertThat(header.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(header.get("Authorization")).isEqualTo(Arrays.asList(authorization));
    }

    @Test
    void getIssue_throwBadRequestException_whenBaseUrlNotFound() {
        // Given
        Long projectId = 1L;
        String code = "PRJ-123";

        // When
        when(settingService.get(projectId, Settings.DEFECT_JIRA_BASE_URL)).thenReturn(null);

        // Then
        assertThrows(BadRequestException.class, () -> jiraRestClient.getIssue(projectId, code));
    }

    @Test
    void getIssue_returnEmptyOptional_whenCodeUnknown() throws BadRequestException {
        // Given
        Long projectId = 1L;
        String code = "PRJ-123";

        String baseUrl = "https://your_company.the_jira_base_url.org";
        String token = "my_jira_token";
        String login = "my_login";

        ResponseEntity<JiraIssue> responseEntity = mock(ResponseEntity.class);

        // When
        when(settingService.get(projectId, Settings.DEFECT_JIRA_BASE_URL)).thenReturn(baseUrl);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_TOKEN)).thenReturn(token);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_LOGIN)).thenReturn(login);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);

        // Then
        Optional<JiraIssue> issue = jiraRestClient.getIssue(projectId, code);

        ArgumentCaptor<String> urlArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpEntity> httpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(urlArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(), httpEntityArgumentCaptor.capture(), any(ParameterizedTypeReference.class));

        assertThat(urlArgumentCaptor.getValue()).isEqualTo("https://your_company.the_jira_base_url.org/rest/api/2/issue/PRJ-123");
        assertThat(httpMethodArgumentCaptor.getValue()).isEqualTo(HttpMethod.GET);
        HttpEntity<JiraIssue> httpEntity = httpEntityArgumentCaptor.getValue();
        assertThat(httpEntity).isNotNull();
        assertThat(httpEntity.getHeaders()).hasSize(2);
        assertThat(httpEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(httpEntity.getHeaders().get("Authorization")).isEqualTo(Arrays.asList("Basic bXlfbG9naW46bXlfamlyYV90b2tlbg=="));

        assertThat(issue).isEmpty();
    }

    @Test
    void getIssue_throwBadRequestException_whenHttpStatusCodeIsNeither404Nor200() {
        // Given
        Long projectId = 1L;
        String code = "PRJ-123";

        String baseUrl = "https://your_company.the_jira_base_url.org";
        String token = "my_jira_token";
        String login = "my_login";

        ResponseEntity<JiraIssue> responseEntity = mock(ResponseEntity.class);

        // When
        when(settingService.get(projectId, Settings.DEFECT_JIRA_BASE_URL)).thenReturn(baseUrl);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_TOKEN)).thenReturn(token);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_LOGIN)).thenReturn(login);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.I_AM_A_TEAPOT);

        // Then
        assertThrows(BadRequestException.class, () -> jiraRestClient.getIssue(projectId, code));
    }

    @Test
    void getIssue_returnJiraIssue_whenIssueFound() throws BadRequestException {
        // Given
        Long projectId = 1L;
        String code = "PRJ-123";

        String baseUrl = "https://your_company.the_jira_base_url.org";
        String token = "my_jira_token";
        String login = "my_login";

        ResponseEntity<JiraIssue> responseEntity = mock(ResponseEntity.class);
        JiraIssue jiraIssue = mock(JiraIssue.class);

        // When
        when(settingService.get(projectId, Settings.DEFECT_JIRA_BASE_URL)).thenReturn(baseUrl);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_TOKEN)).thenReturn(token);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_LOGIN)).thenReturn(login);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(jiraIssue);

        // Then
        Optional<JiraIssue> issue = jiraRestClient.getIssue(projectId, code);

        ArgumentCaptor<String> urlArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpEntity> httpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(urlArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(), httpEntityArgumentCaptor.capture(), any(ParameterizedTypeReference.class));

        assertThat(urlArgumentCaptor.getValue()).isEqualTo("https://your_company.the_jira_base_url.org/rest/api/2/issue/PRJ-123");
        assertThat(httpMethodArgumentCaptor.getValue()).isEqualTo(HttpMethod.GET);
        HttpEntity<JiraIssue> httpEntity = httpEntityArgumentCaptor.getValue();
        assertThat(httpEntity).isNotNull();
        assertThat(httpEntity.getHeaders()).hasSize(2);
        assertThat(httpEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(httpEntity.getHeaders().get("Authorization")).isEqualTo(Arrays.asList("Basic bXlfbG9naW46bXlfamlyYV90b2tlbg=="));

        assertThat(issue).isNotEmpty();
        assertThat(issue).hasValue(jiraIssue);
    }

    @Test
    void getIssuesFromKeys_returnEmptyList_whenNoCodesGiven() throws BadRequestException {
        // Given
        Long projectId = 1L;

        // When

        // Then
        List<JiraIssue> issues = jiraRestClient.getIssuesFromKeys(projectId, new ArrayList<>());
        assertThat(issues).isNotNull();
        assertThat(issues).isEmpty();
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    void getIssuesFromKeys_throwBadRequestException_whenBaseUrlNotFound() {
        // Given
        Long projectId = 1L;
        List<String> codes = Arrays.asList("PRJ-1", "PRJ-2", "PRJ-3");

        // When
        when(settingService.get(projectId, Settings.DEFECT_JIRA_BASE_URL)).thenReturn(null);

        // Then
        assertThrows(BadRequestException.class, () -> jiraRestClient.getIssuesFromKeys(projectId, codes));
    }

    @Test
    void getIssuesFromKeys_throwBadRequestException_whenResponseCodeStatusIsNot200() {
        // Given
        Long projectId = 1L;
        List<String> codes = Arrays.asList("PRJ-1", "PRJ-2", "PRJ-3");

        String baseUrl = "https://your_company.the_jira_base_url.org";
        String token = "my_jira_token";
        String login = "my_login";

        ResponseEntity<JiraIssue> responseEntity = mock(ResponseEntity.class);

        // When
        when(settingService.get(projectId, Settings.DEFECT_JIRA_BASE_URL)).thenReturn(baseUrl);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_TOKEN)).thenReturn(token);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_LOGIN)).thenReturn(login);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.I_AM_A_TEAPOT);

        // Then
        assertThrows(BadRequestException.class, () -> jiraRestClient.getIssuesFromKeys(projectId, codes));
    }

    @Test
    void getIssuesFromKeys_returnIssues_whenResponseCodeStatusIs200AndThereIsNoPagination() throws BadRequestException {
        // Given
        Long projectId = 1L;
        List<String> codes = Arrays.asList("PRJ-1", "PRJ-2", "PRJ-3");

        String baseUrl = "https://your_company.the_jira_base_url.org";
        String token = "my_jira_token";
        String login = "my_login";

        ResponseEntity<JiraIssueSearchResults> responseEntity = mock(ResponseEntity.class);
        JiraIssueSearchResults searchResults = mock(JiraIssueSearchResults.class);

        JiraIssue issue1 = mock(JiraIssue.class);
        JiraIssue issue2 = mock(JiraIssue.class);
        JiraIssue issue3 = mock(JiraIssue.class);

        Integer maxResults = 100;
        Integer total = 12;

        String expectedUrl = "https://your_company.the_jira_base_url.org/rest/api/2/search?jql=(issueKey in (PRJ-1, PRJ-2, PRJ-3))&maxResults=100";

        HttpMethod expectedHttpMethod = HttpMethod.GET;

        String expectedAuthorization = "Basic bXlfbG9naW46bXlfamlyYV90b2tlbg==";
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", expectedAuthorization);
        HttpEntity<JiraIssueSearchResults> expectedRequest = new HttpEntity<>(header);

        ParameterizedTypeReference<JiraIssueSearchResults> expectedParameterizedTypeReference = new ParameterizedTypeReference<JiraIssueSearchResults>() {};

        // When
        when(settingService.get(projectId, Settings.DEFECT_JIRA_BASE_URL)).thenReturn(baseUrl);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_TOKEN)).thenReturn(token);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_LOGIN)).thenReturn(login);

        when(restTemplate.exchange(expectedUrl, expectedHttpMethod, expectedRequest, expectedParameterizedTypeReference)).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(searchResults);
        when(searchResults.getIssues()).thenReturn(Arrays.asList(issue1, issue2, issue3));
        when(searchResults.getMaxResults()).thenReturn(maxResults);
        when(searchResults.getTotal()).thenReturn(total);

        // Then
        List<JiraIssue> issues = jiraRestClient.getIssuesFromKeys(projectId, codes);

        ArgumentCaptor<String> urlArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpEntity> httpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<ParameterizedTypeReference> parameterizedTypeReferenceArgumentCaptor = ArgumentCaptor.forClass(ParameterizedTypeReference.class);
        verify(restTemplate, times(1)).exchange(urlArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(), httpEntityArgumentCaptor.capture(), parameterizedTypeReferenceArgumentCaptor.capture());

        assertThat(urlArgumentCaptor.getValue()).isEqualTo(expectedUrl);
        assertThat(httpMethodArgumentCaptor.getValue()).isEqualTo(expectedHttpMethod);
        HttpEntity<JiraIssueSearchResults> actualRequest = httpEntityArgumentCaptor.getValue();
        assertThat(actualRequest).isNotNull();
        assertThat(actualRequest).isEqualTo(expectedRequest);
        assertThat(parameterizedTypeReferenceArgumentCaptor.getValue()).isEqualTo(expectedParameterizedTypeReference);

        assertThat(issues).isNotEmpty();
        assertThat(issues).contains(issue1, issue2, issue3);
    }

    @Test
    void getIssuesFromKeys_returnIssues_whenResponseCodeStatusIs200AndThereIsPaginationAndInitialMaxResultsEqualsToJiraMaxResultsThreshold() throws BadRequestException {
        // Given
        Long projectId = 1L;
        List<String> codes = Arrays.asList("PRJ-1", "PRJ-2", "PRJ-3");

        String baseUrl = "https://your_company.the_jira_base_url.org";
        String token = "my_jira_token";
        String login = "my_login";

        ResponseEntity<JiraIssueSearchResults> responseEntity = mock(ResponseEntity.class);
        JiraIssueSearchResults searchResults = mock(JiraIssueSearchResults.class);

        JiraIssue issue1 = mock(JiraIssue.class);
        JiraIssue issue2 = mock(JiraIssue.class);
        JiraIssue issue3 = mock(JiraIssue.class);

        Integer maxResults = 100;
        Integer total = 345;

        String expectedUrl = "https://your_company.the_jira_base_url.org/rest/api/2/search?jql=(issueKey in (PRJ-1, PRJ-2, PRJ-3))&maxResults=100";

        String expectedPaginationUrl1 = "https://your_company.the_jira_base_url.org/rest/api/2/search?jql=(issueKey in (PRJ-1, PRJ-2, PRJ-3))&startAt=100&maxResults=100";
        ResponseEntity<JiraIssueSearchResults> paginationResponseEntity1 = mock(ResponseEntity.class);
        JiraIssueSearchResults paginationSearchResult1 = mock(JiraIssueSearchResults.class);
        JiraIssue pagination1Issue1 = mock(JiraIssue.class);
        JiraIssue pagination1Issue2 = mock(JiraIssue.class);

        String expectedPaginationUrl2 = "https://your_company.the_jira_base_url.org/rest/api/2/search?jql=(issueKey in (PRJ-1, PRJ-2, PRJ-3))&startAt=200&maxResults=100";
        ResponseEntity<JiraIssueSearchResults> paginationResponseEntity2 = mock(ResponseEntity.class);
        JiraIssueSearchResults paginationSearchResult2 = mock(JiraIssueSearchResults.class);
        JiraIssue pagination2Issue1 = mock(JiraIssue.class);
        JiraIssue pagination2Issue2 = mock(JiraIssue.class);
        JiraIssue pagination2Issue3 = mock(JiraIssue.class);
        JiraIssue pagination2Issue4 = mock(JiraIssue.class);
        JiraIssue pagination2Issue5 = mock(JiraIssue.class);

        String expectedPaginationUrl3 = "https://your_company.the_jira_base_url.org/rest/api/2/search?jql=(issueKey in (PRJ-1, PRJ-2, PRJ-3))&startAt=300&maxResults=100";
        ResponseEntity<JiraIssueSearchResults> paginationResponseEntity3 = mock(ResponseEntity.class);
        JiraIssueSearchResults paginationSearchResult3 = mock(JiraIssueSearchResults.class);
        JiraIssue pagination3Issue1 = mock(JiraIssue.class);
        JiraIssue pagination3Issue2 = mock(JiraIssue.class);
        JiraIssue pagination3Issue3 = mock(JiraIssue.class);

        HttpMethod expectedHttpMethod = HttpMethod.GET;

        String expectedAuthorization = "Basic bXlfbG9naW46bXlfamlyYV90b2tlbg==";
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", expectedAuthorization);
        HttpEntity<JiraIssueSearchResults> expectedRequest = new HttpEntity<>(header);

        ParameterizedTypeReference<JiraIssueSearchResults> expectedParameterizedTypeReference = new ParameterizedTypeReference<JiraIssueSearchResults>() {};

        // When
        when(settingService.get(projectId, Settings.DEFECT_JIRA_BASE_URL)).thenReturn(baseUrl);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_TOKEN)).thenReturn(token);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_LOGIN)).thenReturn(login);

        when(restTemplate.exchange(expectedUrl, expectedHttpMethod, expectedRequest, expectedParameterizedTypeReference)).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(searchResults);
        when(searchResults.getIssues()).thenReturn(Arrays.asList(issue1, issue2, issue3));
        when(searchResults.getMaxResults()).thenReturn(maxResults);
        when(searchResults.getTotal()).thenReturn(total);

        when(restTemplate.exchange(expectedPaginationUrl1, expectedHttpMethod, expectedRequest, expectedParameterizedTypeReference)).thenReturn(paginationResponseEntity1);
        when(paginationResponseEntity1.getStatusCode()).thenReturn(HttpStatus.OK);
        when(paginationResponseEntity1.getBody()).thenReturn(paginationSearchResult1);
        when(paginationSearchResult1.getIssues()).thenReturn(Arrays.asList(pagination1Issue1, pagination1Issue2));

        when(restTemplate.exchange(expectedPaginationUrl2, expectedHttpMethod, expectedRequest, expectedParameterizedTypeReference)).thenReturn(paginationResponseEntity2);
        when(paginationResponseEntity2.getStatusCode()).thenReturn(HttpStatus.OK);
        when(paginationResponseEntity2.getBody()).thenReturn(paginationSearchResult2);
        when(paginationSearchResult2.getIssues()).thenReturn(Arrays.asList(pagination2Issue1, pagination2Issue2, pagination2Issue3, pagination2Issue4, pagination2Issue5));

        when(restTemplate.exchange(expectedPaginationUrl3, expectedHttpMethod, expectedRequest, expectedParameterizedTypeReference)).thenReturn(paginationResponseEntity3);
        when(paginationResponseEntity3.getStatusCode()).thenReturn(HttpStatus.OK);
        when(paginationResponseEntity3.getBody()).thenReturn(paginationSearchResult3);
        when(paginationSearchResult3.getIssues()).thenReturn(Arrays.asList(pagination3Issue1, pagination3Issue2, pagination3Issue3));

        // Then
        List<JiraIssue> issues = jiraRestClient.getIssuesFromKeys(projectId, codes);

        ArgumentCaptor<String> urlArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpEntity> httpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<ParameterizedTypeReference> parameterizedTypeReferenceArgumentCaptor = ArgumentCaptor.forClass(ParameterizedTypeReference.class);
        verify(restTemplate, times(4)).exchange(urlArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(), httpEntityArgumentCaptor.capture(), parameterizedTypeReferenceArgumentCaptor.capture());

        assertThat(urlArgumentCaptor.getAllValues())
                .hasSize(4)
                .containsOnly(
                        expectedUrl,
                        expectedPaginationUrl1,
                        expectedPaginationUrl2,
                        expectedPaginationUrl3
                );
        assertThat(httpMethodArgumentCaptor.getAllValues())
                .hasSize(4)
                .containsOnly(expectedHttpMethod);
        List<HttpEntity> actualRequests = httpEntityArgumentCaptor.getAllValues();
        assertThat(actualRequests)
                .hasSize(4)
                .containsOnly(expectedRequest);
        assertThat(parameterizedTypeReferenceArgumentCaptor.getAllValues())
                .hasSize(4)
                .containsOnly(expectedParameterizedTypeReference);

        assertThat(issues).isNotEmpty();
        assertThat(issues)
                .hasSize(13)
                .containsOnly(
                        issue1,
                        issue2,
                        issue3,
                        pagination1Issue1,
                        pagination1Issue2,
                        pagination2Issue1,
                        pagination2Issue2,
                        pagination2Issue3,
                        pagination2Issue4,
                        pagination2Issue5,
                        pagination3Issue1,
                        pagination3Issue2,
                        pagination3Issue3
                );
    }

    @Test
    void getIssuesFromKeys_returnIssues_whenResponseCodeStatusIs200AndThereIsPaginationAndInitialMaxResultsLesserThanJiraMaxResultsThreshold() throws BadRequestException {
        // Given
        Long projectId = 1L;
        List<String> codes = Arrays.asList("PRJ-1", "PRJ-2", "PRJ-3");

        String baseUrl = "https://your_company.the_jira_base_url.org";
        String token = "my_jira_token";
        String login = "my_login";

        ResponseEntity<JiraIssueSearchResults> responseEntity = mock(ResponseEntity.class);
        JiraIssueSearchResults searchResults = mock(JiraIssueSearchResults.class);

        JiraIssue issue1 = mock(JiraIssue.class);
        JiraIssue issue2 = mock(JiraIssue.class);
        JiraIssue issue3 = mock(JiraIssue.class);

        Integer maxResults = 150;
        Integer total = 345;

        String expectedUrl = "https://your_company.the_jira_base_url.org/rest/api/2/search?jql=(issueKey in (PRJ-1, PRJ-2, PRJ-3))&maxResults=100";

        String expectedPaginationUrl1 = "https://your_company.the_jira_base_url.org/rest/api/2/search?jql=(issueKey in (PRJ-1, PRJ-2, PRJ-3))&startAt=100&maxResults=150";
        ResponseEntity<JiraIssueSearchResults> paginationResponseEntity1 = mock(ResponseEntity.class);
        JiraIssueSearchResults paginationSearchResult1 = mock(JiraIssueSearchResults.class);
        JiraIssue pagination1Issue1 = mock(JiraIssue.class);
        JiraIssue pagination1Issue2 = mock(JiraIssue.class);

        String expectedPaginationUrl2 = "https://your_company.the_jira_base_url.org/rest/api/2/search?jql=(issueKey in (PRJ-1, PRJ-2, PRJ-3))&startAt=250&maxResults=150";
        ResponseEntity<JiraIssueSearchResults> paginationResponseEntity2 = mock(ResponseEntity.class);
        JiraIssueSearchResults paginationSearchResult2 = mock(JiraIssueSearchResults.class);
        JiraIssue pagination2Issue1 = mock(JiraIssue.class);
        JiraIssue pagination2Issue2 = mock(JiraIssue.class);
        JiraIssue pagination2Issue3 = mock(JiraIssue.class);
        JiraIssue pagination2Issue4 = mock(JiraIssue.class);
        JiraIssue pagination2Issue5 = mock(JiraIssue.class);

        HttpMethod expectedHttpMethod = HttpMethod.GET;

        String expectedAuthorization = "Basic bXlfbG9naW46bXlfamlyYV90b2tlbg==";
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", expectedAuthorization);
        HttpEntity<JiraIssueSearchResults> expectedRequest = new HttpEntity<>(header);

        ParameterizedTypeReference<JiraIssueSearchResults> expectedParameterizedTypeReference = new ParameterizedTypeReference<JiraIssueSearchResults>() {};

        // When
        when(settingService.get(projectId, Settings.DEFECT_JIRA_BASE_URL)).thenReturn(baseUrl);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_TOKEN)).thenReturn(token);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_LOGIN)).thenReturn(login);

        when(restTemplate.exchange(expectedUrl, expectedHttpMethod, expectedRequest, expectedParameterizedTypeReference)).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(searchResults);
        when(searchResults.getIssues()).thenReturn(Arrays.asList(issue1, issue2, issue3));
        when(searchResults.getMaxResults()).thenReturn(maxResults);
        when(searchResults.getTotal()).thenReturn(total);

        when(restTemplate.exchange(expectedPaginationUrl1, expectedHttpMethod, expectedRequest, expectedParameterizedTypeReference)).thenReturn(paginationResponseEntity1);
        when(paginationResponseEntity1.getStatusCode()).thenReturn(HttpStatus.OK);
        when(paginationResponseEntity1.getBody()).thenReturn(paginationSearchResult1);
        when(paginationSearchResult1.getIssues()).thenReturn(Arrays.asList(pagination1Issue1, pagination1Issue2));

        when(restTemplate.exchange(expectedPaginationUrl2, expectedHttpMethod, expectedRequest, expectedParameterizedTypeReference)).thenReturn(paginationResponseEntity2);
        when(paginationResponseEntity2.getStatusCode()).thenReturn(HttpStatus.OK);
        when(paginationResponseEntity2.getBody()).thenReturn(paginationSearchResult2);
        when(paginationSearchResult2.getIssues()).thenReturn(Arrays.asList(pagination2Issue1, pagination2Issue2, pagination2Issue3, pagination2Issue4, pagination2Issue5));

        // Then
        List<JiraIssue> issues = jiraRestClient.getIssuesFromKeys(projectId, codes);

        ArgumentCaptor<String> urlArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpEntity> httpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<ParameterizedTypeReference> parameterizedTypeReferenceArgumentCaptor = ArgumentCaptor.forClass(ParameterizedTypeReference.class);
        verify(restTemplate, times(3)).exchange(urlArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(), httpEntityArgumentCaptor.capture(), parameterizedTypeReferenceArgumentCaptor.capture());

        assertThat(urlArgumentCaptor.getAllValues())
                .hasSize(3)
                .containsOnly(
                        expectedUrl,
                        expectedPaginationUrl1,
                        expectedPaginationUrl2
                );
        assertThat(httpMethodArgumentCaptor.getAllValues())
                .hasSize(3)
                .containsOnly(expectedHttpMethod);
        List<HttpEntity> actualRequests = httpEntityArgumentCaptor.getAllValues();
        assertThat(actualRequests)
                .hasSize(3)
                .containsOnly(expectedRequest);
        assertThat(parameterizedTypeReferenceArgumentCaptor.getAllValues())
                .hasSize(3)
                .containsOnly(expectedParameterizedTypeReference);

        assertThat(issues).isNotEmpty();
        assertThat(issues)
                .hasSize(10)
                .containsOnly(
                        issue1,
                        issue2,
                        issue3,
                        pagination1Issue1,
                        pagination1Issue2,
                        pagination2Issue1,
                        pagination2Issue2,
                        pagination2Issue3,
                        pagination2Issue4,
                        pagination2Issue5
                );
    }

    @Test
    void getIssuesFromKeys_returnIssues_whenResponseCodeStatusIs200AndThereIsPaginationAndInitialMaxResultsGreaterThanJiraMaxResultsThreshold() throws BadRequestException {
        // Given
        Long projectId = 1L;
        List<String> codes = Arrays.asList("PRJ-1", "PRJ-2", "PRJ-3");

        String baseUrl = "https://your_company.the_jira_base_url.org";
        String token = "my_jira_token";
        String login = "my_login";

        ResponseEntity<JiraIssueSearchResults> responseEntity = mock(ResponseEntity.class);
        JiraIssueSearchResults searchResults = mock(JiraIssueSearchResults.class);

        JiraIssue issue1 = mock(JiraIssue.class);
        JiraIssue issue2 = mock(JiraIssue.class);
        JiraIssue issue3 = mock(JiraIssue.class);

        Integer maxResults = 50;
        Integer total = 345;

        String expectedUrl = "https://your_company.the_jira_base_url.org/rest/api/2/search?jql=(issueKey in (PRJ-1, PRJ-2, PRJ-3))&maxResults=100";

        String expectedPaginationUrl1 = "https://your_company.the_jira_base_url.org/rest/api/2/search?jql=(issueKey in (PRJ-1, PRJ-2, PRJ-3))&startAt=50&maxResults=50";
        ResponseEntity<JiraIssueSearchResults> paginationResponseEntity1 = mock(ResponseEntity.class);
        JiraIssueSearchResults paginationSearchResult1 = mock(JiraIssueSearchResults.class);
        JiraIssue pagination1Issue1 = mock(JiraIssue.class);
        JiraIssue pagination1Issue2 = mock(JiraIssue.class);

        String expectedPaginationUrl2 = "https://your_company.the_jira_base_url.org/rest/api/2/search?jql=(issueKey in (PRJ-1, PRJ-2, PRJ-3))&startAt=100&maxResults=50";
        ResponseEntity<JiraIssueSearchResults> paginationResponseEntity2 = mock(ResponseEntity.class);
        JiraIssueSearchResults paginationSearchResult2 = mock(JiraIssueSearchResults.class);
        JiraIssue pagination2Issue1 = mock(JiraIssue.class);
        JiraIssue pagination2Issue2 = mock(JiraIssue.class);
        JiraIssue pagination2Issue3 = mock(JiraIssue.class);
        JiraIssue pagination2Issue4 = mock(JiraIssue.class);
        JiraIssue pagination2Issue5 = mock(JiraIssue.class);

        String expectedPaginationUrl3 = "https://your_company.the_jira_base_url.org/rest/api/2/search?jql=(issueKey in (PRJ-1, PRJ-2, PRJ-3))&startAt=150&maxResults=50";
        ResponseEntity<JiraIssueSearchResults> paginationResponseEntity3 = mock(ResponseEntity.class);
        JiraIssueSearchResults paginationSearchResult3 = mock(JiraIssueSearchResults.class);
        JiraIssue pagination3Issue1 = mock(JiraIssue.class);
        JiraIssue pagination3Issue2 = mock(JiraIssue.class);
        JiraIssue pagination3Issue3 = mock(JiraIssue.class);

        String expectedPaginationUrl4 = "https://your_company.the_jira_base_url.org/rest/api/2/search?jql=(issueKey in (PRJ-1, PRJ-2, PRJ-3))&startAt=200&maxResults=50";
        ResponseEntity<JiraIssueSearchResults> paginationResponseEntity4 = mock(ResponseEntity.class);
        JiraIssueSearchResults paginationSearchResult4 = mock(JiraIssueSearchResults.class);
        JiraIssue pagination4Issue1 = mock(JiraIssue.class);

        String expectedPaginationUrl5 = "https://your_company.the_jira_base_url.org/rest/api/2/search?jql=(issueKey in (PRJ-1, PRJ-2, PRJ-3))&startAt=250&maxResults=50";
        ResponseEntity<JiraIssueSearchResults> paginationResponseEntity5 = mock(ResponseEntity.class);
        JiraIssueSearchResults paginationSearchResult5 = mock(JiraIssueSearchResults.class);
        JiraIssue pagination5Issue1 = mock(JiraIssue.class);
        JiraIssue pagination5Issue2 = mock(JiraIssue.class);

        String expectedPaginationUrl6 = "https://your_company.the_jira_base_url.org/rest/api/2/search?jql=(issueKey in (PRJ-1, PRJ-2, PRJ-3))&startAt=300&maxResults=50";
        ResponseEntity<JiraIssueSearchResults> paginationResponseEntity6 = mock(ResponseEntity.class);
        JiraIssueSearchResults paginationSearchResult6 = mock(JiraIssueSearchResults.class);
        JiraIssue pagination6Issue1 = mock(JiraIssue.class);
        JiraIssue pagination6Issue2 = mock(JiraIssue.class);
        JiraIssue pagination6Issue3 = mock(JiraIssue.class);
        JiraIssue pagination6Issue4 = mock(JiraIssue.class);

        HttpMethod expectedHttpMethod = HttpMethod.GET;

        String expectedAuthorization = "Basic bXlfbG9naW46bXlfamlyYV90b2tlbg==";
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", expectedAuthorization);
        HttpEntity<JiraIssueSearchResults> expectedRequest = new HttpEntity<>(header);

        ParameterizedTypeReference<JiraIssueSearchResults> expectedParameterizedTypeReference = new ParameterizedTypeReference<JiraIssueSearchResults>() {};

        // When
        when(settingService.get(projectId, Settings.DEFECT_JIRA_BASE_URL)).thenReturn(baseUrl);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_TOKEN)).thenReturn(token);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_LOGIN)).thenReturn(login);

        when(restTemplate.exchange(expectedUrl, expectedHttpMethod, expectedRequest, expectedParameterizedTypeReference)).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(searchResults);
        when(searchResults.getIssues()).thenReturn(Arrays.asList(issue1, issue2, issue3));
        when(searchResults.getMaxResults()).thenReturn(maxResults);
        when(searchResults.getTotal()).thenReturn(total);

        when(restTemplate.exchange(expectedPaginationUrl1, expectedHttpMethod, expectedRequest, expectedParameterizedTypeReference)).thenReturn(paginationResponseEntity1);
        when(paginationResponseEntity1.getStatusCode()).thenReturn(HttpStatus.OK);
        when(paginationResponseEntity1.getBody()).thenReturn(paginationSearchResult1);
        when(paginationSearchResult1.getIssues()).thenReturn(Arrays.asList(pagination1Issue1, pagination1Issue2));

        when(restTemplate.exchange(expectedPaginationUrl2, expectedHttpMethod, expectedRequest, expectedParameterizedTypeReference)).thenReturn(paginationResponseEntity2);
        when(paginationResponseEntity2.getStatusCode()).thenReturn(HttpStatus.OK);
        when(paginationResponseEntity2.getBody()).thenReturn(paginationSearchResult2);
        when(paginationSearchResult2.getIssues()).thenReturn(Arrays.asList(pagination2Issue1, pagination2Issue2, pagination2Issue3, pagination2Issue4, pagination2Issue5));

        when(restTemplate.exchange(expectedPaginationUrl3, expectedHttpMethod, expectedRequest, expectedParameterizedTypeReference)).thenReturn(paginationResponseEntity3);
        when(paginationResponseEntity3.getStatusCode()).thenReturn(HttpStatus.OK);
        when(paginationResponseEntity3.getBody()).thenReturn(paginationSearchResult3);
        when(paginationSearchResult3.getIssues()).thenReturn(Arrays.asList(pagination3Issue1, pagination3Issue2, pagination3Issue3));

        when(restTemplate.exchange(expectedPaginationUrl4, expectedHttpMethod, expectedRequest, expectedParameterizedTypeReference)).thenReturn(paginationResponseEntity4);
        when(paginationResponseEntity4.getStatusCode()).thenReturn(HttpStatus.OK);
        when(paginationResponseEntity4.getBody()).thenReturn(paginationSearchResult4);
        when(paginationSearchResult4.getIssues()).thenReturn(Arrays.asList(pagination4Issue1));

        when(restTemplate.exchange(expectedPaginationUrl5, expectedHttpMethod, expectedRequest, expectedParameterizedTypeReference)).thenReturn(paginationResponseEntity5);
        when(paginationResponseEntity5.getStatusCode()).thenReturn(HttpStatus.OK);
        when(paginationResponseEntity5.getBody()).thenReturn(paginationSearchResult5);
        when(paginationSearchResult5.getIssues()).thenReturn(Arrays.asList(pagination5Issue1, pagination5Issue2));

        when(restTemplate.exchange(expectedPaginationUrl6, expectedHttpMethod, expectedRequest, expectedParameterizedTypeReference)).thenReturn(paginationResponseEntity6);
        when(paginationResponseEntity6.getStatusCode()).thenReturn(HttpStatus.OK);
        when(paginationResponseEntity6.getBody()).thenReturn(paginationSearchResult6);
        when(paginationSearchResult6.getIssues()).thenReturn(Arrays.asList(pagination6Issue1, pagination6Issue2, pagination6Issue3, pagination6Issue4));

        // Then
        List<JiraIssue> issues = jiraRestClient.getIssuesFromKeys(projectId, codes);

        ArgumentCaptor<String> urlArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpEntity> httpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<ParameterizedTypeReference> parameterizedTypeReferenceArgumentCaptor = ArgumentCaptor.forClass(ParameterizedTypeReference.class);
        verify(restTemplate, times(7)).exchange(urlArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(), httpEntityArgumentCaptor.capture(), parameterizedTypeReferenceArgumentCaptor.capture());

        assertThat(urlArgumentCaptor.getAllValues())
                .hasSize(7)
                .containsOnly(
                        expectedUrl,
                        expectedPaginationUrl1,
                        expectedPaginationUrl2,
                        expectedPaginationUrl3,
                        expectedPaginationUrl4,
                        expectedPaginationUrl5,
                        expectedPaginationUrl6
                );
        assertThat(httpMethodArgumentCaptor.getAllValues())
                .hasSize(7)
                .containsOnly(expectedHttpMethod);
        List<HttpEntity> actualRequests = httpEntityArgumentCaptor.getAllValues();
        assertThat(actualRequests)
                .hasSize(7)
                .containsOnly(expectedRequest);
        assertThat(parameterizedTypeReferenceArgumentCaptor.getAllValues())
                .hasSize(7)
                .containsOnly(expectedParameterizedTypeReference);

        assertThat(issues).isNotEmpty();
        assertThat(issues)
                .hasSize(20)
                .containsOnly(
                        issue1,
                        issue2,
                        issue3,
                        pagination1Issue1,
                        pagination1Issue2,
                        pagination2Issue1,
                        pagination2Issue2,
                        pagination2Issue3,
                        pagination2Issue4,
                        pagination2Issue5,
                        pagination3Issue1,
                        pagination3Issue2,
                        pagination3Issue3,
                        pagination4Issue1,
                        pagination5Issue1,
                        pagination5Issue2,
                        pagination6Issue1,
                        pagination6Issue2,
                        pagination6Issue3,
                        pagination6Issue4
                );
    }

    @Test
    void getUpdatedIssues_throwBadRequestException_whenBaseUrlNotFoundAndUpdateDateNotNull() {
        // Given
        Long projectId = 1L;
        Date updateDate = new Date();

        // When
        when(settingService.get(projectId, Settings.DEFECT_JIRA_BASE_URL)).thenReturn(null);

        // Then
        assertThrows(BadRequestException.class, () -> jiraRestClient.getUpdatedIssues(projectId, updateDate));
    }

    @Test
    void getUpdatedIssues_throwBadRequestException_whenResponseCodeStatusIsNot200AndUpdateDateNotNull() {
        // Given
        Long projectId = 1L;
        Date updateDate = new Date();

        String baseUrl = "https://your_company.the_jira_base_url.org";
        String token = "my_jira_token";
        String login = "my_login";

        ResponseEntity<JiraIssue> responseEntity = mock(ResponseEntity.class);

        // When
        when(settingService.get(projectId, Settings.DEFECT_JIRA_BASE_URL)).thenReturn(baseUrl);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_TOKEN)).thenReturn(token);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_LOGIN)).thenReturn(login);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.I_AM_A_TEAPOT);

        // Then
        assertThrows(BadRequestException.class, () -> jiraRestClient.getUpdatedIssues(projectId, updateDate));
    }

    @Test
    void getUpdatedIssues_returnIssues_whenResponseCodeStatusIs200AndUpdateDateNotNull() throws BadRequestException, ParseException {
        // Given
        Long projectId = 1L;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date updateDate = sdf.parse("2020/09/25 18:00:15");

        String baseUrl = "https://your_company.the_jira_base_url.org";
        String token = "my_jira_token";
        String login = "my_login";

        ResponseEntity<JiraIssueSearchResults> responseEntity = mock(ResponseEntity.class);
        JiraIssueSearchResults searchResults = mock(JiraIssueSearchResults.class);

        JiraIssue issue1 = mock(JiraIssue.class);
        JiraIssue issue2 = mock(JiraIssue.class);
        JiraIssue issue3 = mock(JiraIssue.class);

        Integer maxResults = 100;
        Integer total = 12;

        String expectedUrl = "https://your_company.the_jira_base_url.org/rest/api/2/search?jql=(updated > \"2020-09-25 18:00\")&maxResults=100";

        HttpMethod expectedHttpMethod = HttpMethod.GET;

        String expectedAuthorization = "Basic bXlfbG9naW46bXlfamlyYV90b2tlbg==";
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", expectedAuthorization);
        HttpEntity<JiraIssueSearchResults> expectedRequest = new HttpEntity<>(header);

        ParameterizedTypeReference<JiraIssueSearchResults> expectedParameterizedTypeReference = new ParameterizedTypeReference<JiraIssueSearchResults>() {};

        // When
        when(settingService.get(projectId, Settings.DEFECT_JIRA_BASE_URL)).thenReturn(baseUrl);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_TOKEN)).thenReturn(token);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_LOGIN)).thenReturn(login);

        when(restTemplate.exchange(expectedUrl, expectedHttpMethod, expectedRequest, expectedParameterizedTypeReference)).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(searchResults);
        when(searchResults.getIssues()).thenReturn(Arrays.asList(issue1, issue2, issue3));
        when(searchResults.getMaxResults()).thenReturn(maxResults);
        when(searchResults.getTotal()).thenReturn(total);

        // Then
        List<JiraIssue> issues = jiraRestClient.getUpdatedIssues(projectId, updateDate);

        ArgumentCaptor<String> urlArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpEntity> httpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<ParameterizedTypeReference> parameterizedTypeReferenceArgumentCaptor = ArgumentCaptor.forClass(ParameterizedTypeReference.class);
        verify(restTemplate, times(1)).exchange(urlArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(), httpEntityArgumentCaptor.capture(), parameterizedTypeReferenceArgumentCaptor.capture());

        assertThat(urlArgumentCaptor.getValue()).isEqualTo(expectedUrl);
        assertThat(httpMethodArgumentCaptor.getValue()).isEqualTo(expectedHttpMethod);
        HttpEntity<JiraIssueSearchResults> actualRequest = httpEntityArgumentCaptor.getValue();
        assertThat(actualRequest).isNotNull();
        assertThat(actualRequest).isEqualTo(expectedRequest);
        assertThat(parameterizedTypeReferenceArgumentCaptor.getValue()).isEqualTo(expectedParameterizedTypeReference);

        assertThat(issues).isNotEmpty();
        assertThat(issues).contains(issue1, issue2, issue3);
    }

    @Test
    void getUpdatedIssues_returnIssues_whenResponseCodeStatusIs200AndThereAreProjectsFilterAndUpdateDateNotNull() throws BadRequestException, ParseException {
        // Given
        Long projectId = 1L;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date updateDate = sdf.parse("2020/09/25 18:00:15");

        String baseUrl = "https://your_company.the_jira_base_url.org";
        String token = "my_jira_token";
        String login = "my_login";

        ResponseEntity<JiraIssueSearchResults> responseEntity = mock(ResponseEntity.class);
        JiraIssueSearchResults searchResults = mock(JiraIssueSearchResults.class);

        JiraIssue issue1 = mock(JiraIssue.class);
        JiraIssue issue2 = mock(JiraIssue.class);
        JiraIssue issue3 = mock(JiraIssue.class);

        Integer maxResults = 100;
        Integer total = 12;

        String expectedUrl = "https://your_company.the_jira_base_url.org/rest/api/2/search?jql=(project in (TEST, QA, PRJ, POC) AND updated > \"2020-09-25 18:00\")&maxResults=100";

        HttpMethod expectedHttpMethod = HttpMethod.GET;

        String expectedAuthorization = "Basic bXlfbG9naW46bXlfamlyYV90b2tlbg==";
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.set("Authorization", expectedAuthorization);
        HttpEntity<JiraIssueSearchResults> expectedRequest = new HttpEntity<>(header);

        ParameterizedTypeReference<JiraIssueSearchResults> expectedParameterizedTypeReference = new ParameterizedTypeReference<JiraIssueSearchResults>() {};

        // When
        when(settingService.get(projectId, Settings.DEFECT_JIRA_BASE_URL)).thenReturn(baseUrl);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_TOKEN)).thenReturn(token);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_LOGIN)).thenReturn(login);
        when(settingService.get(projectId, Settings.DEFECT_JIRA_FILTER_PROJECTS)).thenReturn("TEST,QA, PRJ,  POC");

        when(restTemplate.exchange(expectedUrl, expectedHttpMethod, expectedRequest, expectedParameterizedTypeReference)).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseEntity.getBody()).thenReturn(searchResults);
        when(searchResults.getIssues()).thenReturn(Arrays.asList(issue1, issue2, issue3));
        when(searchResults.getMaxResults()).thenReturn(maxResults);
        when(searchResults.getTotal()).thenReturn(total);

        // Then
        List<JiraIssue> issues = jiraRestClient.getUpdatedIssues(projectId, updateDate);

        ArgumentCaptor<String> urlArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpMethod> httpMethodArgumentCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpEntity> httpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<ParameterizedTypeReference> parameterizedTypeReferenceArgumentCaptor = ArgumentCaptor.forClass(ParameterizedTypeReference.class);
        verify(restTemplate, times(1)).exchange(urlArgumentCaptor.capture(), httpMethodArgumentCaptor.capture(), httpEntityArgumentCaptor.capture(), parameterizedTypeReferenceArgumentCaptor.capture());

        assertThat(urlArgumentCaptor.getValue()).isEqualTo(expectedUrl);
        assertThat(httpMethodArgumentCaptor.getValue()).isEqualTo(expectedHttpMethod);
        HttpEntity<JiraIssueSearchResults> actualRequest = httpEntityArgumentCaptor.getValue();
        assertThat(actualRequest).isNotNull();
        assertThat(actualRequest).isEqualTo(expectedRequest);
        assertThat(parameterizedTypeReferenceArgumentCaptor.getValue()).isEqualTo(expectedParameterizedTypeReference);

        assertThat(issues).isNotEmpty();
        assertThat(issues).contains(issue1, issue2, issue3);
    }
}
