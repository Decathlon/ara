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

import com.decathlon.ara.Entities;
import com.decathlon.ara.defect.jira.api.model.JiraIssue;
import com.decathlon.ara.defect.jira.api.model.JiraIssueSearchResults;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.support.Settings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JiraRestClient {

    @Autowired
    private SettingService settingService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Get a Jira issue from a key, if it exists.
     * @param projectId the project id
     * @param issueKey the Jira issue key
     * @return the Jira issue matching the key given, if found. Return an empty optional otherwise
     * @throws BadRequestException thrown if the Jira API call returned an error code
     */
    public Optional<JiraIssue> getIssue(Long projectId, String issueKey) throws BadRequestException {
        String baseUrl = getJiraBaseUrl(projectId);
        String url = String.format("%s/rest/api/2/issue/%s", baseUrl, issueKey);

        HttpHeaders header = getHeader(projectId);
        HttpEntity<JiraIssue> request = new HttpEntity<>(header);

        final ParameterizedTypeReference<JiraIssue> responseType = new ParameterizedTypeReference<JiraIssue>() {};
        ResponseEntity<JiraIssue> response = restTemplate.exchange(url, HttpMethod.GET, request, responseType);
        HttpStatus httpStatus = response.getStatusCode();
        if (HttpStatus.NOT_FOUND.equals(httpStatus)) {
            String infoMessage = String.format("DEFECT|jira|The issue %s was not found", issueKey);
            log.warn(infoMessage);
            return Optional.empty();
        }
        Boolean responseIsNotOK = !HttpStatus.OK.equals(httpStatus);
        if (responseIsNotOK) {
            String errorMessage = String.format("DEFECT|jira|The Jira request [%s] %s returned an error status code -> %s", HttpMethod.GET, url, httpStatus);
            log.warn(errorMessage);
            throw new BadRequestException(errorMessage, Entities.SETTING, "jira_request_error");
        }

        JiraIssue jiraIssue = response.getBody();
        return Optional.of(jiraIssue);
    }

    /**
     * Get the Jira API base url
     * @param projectId the project id
     * @return the Jira base url
     * @throws BadRequestException thrown if not found
     */
    private String getJiraBaseUrl(Long projectId) throws BadRequestException {
        String baseUrl = settingService.get(projectId, Settings.DEFECT_JIRA_BASE_URL);
        if (StringUtils.isBlank(baseUrl)) {
            log.error("DEFECT|jira|Jira base url not found for this project ({})", projectId);
            throw new BadRequestException("Jira base url not found", Entities.SETTING, "jira_base_url_not_found");
        }
        return baseUrl;
    }

    /**
     * Get the header required to call Jira APIs
     * @param projectId the project id
     * @return the Jira API header
     * @throws BadRequestException thrown if a setting is missing
     */
    public HttpHeaders getHeader(Long projectId) throws BadRequestException {
        String token = settingService.get(projectId, Settings.DEFECT_JIRA_TOKEN);
        if (StringUtils.isBlank(token)) {
            log.error("DEFECT|jira|Jira token not found for this project ({})", projectId);
            throw new BadRequestException("Jira token not found", Entities.SETTING, "jira_token_not_found");
        }

        String login = settingService.get(projectId, Settings.DEFECT_JIRA_LOGIN);
        if (StringUtils.isBlank(login)) {
            log.error("DEFECT|jira|Jira login not found for this project ({})", projectId);
            throw new BadRequestException("Jira login not found", Entities.SETTING, "jira_login_not_found");
        }

        String authorization = String.format("%s:%s", login, token);
        authorization = Base64.getEncoder().encodeToString(authorization.getBytes());
        authorization = String.format("Basic %s", authorization);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authorization);
        return headers;
    }

    /**
     * Get Jira issues matching a list of keys
     * @param projectId the project id
     * @param issueKeys the Jira issue keys
     * @return the matching Jira issues
     * @throws BadRequestException thrown if (one of) the API call(s) returned an error code
     */
    public List<JiraIssue> getIssuesFromKeys(Long projectId, List<String> issueKeys) throws BadRequestException {
        if (CollectionUtils.isEmpty(issueKeys)) {
            return new ArrayList<>();
        }

        String codes = String.join(", ", issueKeys);
        String jql = String.format("issueKey in (%s)", codes);

        return searchJiraIssues(projectId, jql);
    }

    /**
     * Search Jira issues matching a jql query
     * @param projectId the project id
     * @param jql the jql query
     * @return a list of issues matching the jql query given
     * @throws BadRequestException thrown if (one of) the API call(s) returned an error code
     */
    private List<JiraIssue> searchJiraIssues(Long projectId, String jql) throws BadRequestException {
        final Integer initialMaxResults = 100;

        final String baseUrl = getJiraBaseUrl(projectId);
        final String urlWithJQL = String.format("%s/rest/api/2/search?jql=(%s)", baseUrl, jql);
        final String finalUrl = String.format("%s&maxResults=%d", urlWithJQL, initialMaxResults);

        HttpHeaders header = getHeader(projectId);
        JiraIssueSearchResults searchResult = getSearchResultsFromHeaderAndUrl(header, finalUrl);

        // It is not possible to get all the results at once if their number is greater than a threshold Jira sets.
        // The maxResults field can't be greater than this threshold:
        // if the query contains maxResults=200 BUT the threshold is 100, Jira won't return the 200 requested but 100 instead
        // This forces to call Jira API as much as it is necessary to reach the total result number
        // Also, keep in mind that this threshold can vary without notice
        // Please check the Jira documentation for more details about this subject:
        // https://confluence.atlassian.com/jirakb/changing-maxresults-parameter-for-jira-rest-api-779160706.html,
        final Integer actualMaxResults = searchResult.getMaxResults();
        final Integer total = searchResult.getTotal();
        final Integer firstResultsNumber = initialMaxResults <= actualMaxResults ? initialMaxResults : actualMaxResults;
        final Integer remainingResultsNumber = total - firstResultsNumber;

        List<JiraIssue> allIssues = searchResult.getIssues();
        log.debug("DEFECT|jira|[Jira] Getting issues from [{}]", finalUrl);
        log.debug("DEFECT|jira|[Jira] Planning to load {} issues...", total);
        if (remainingResultsNumber > 0) {
            log.debug("DEFECT|jira|[Jira] Loading the remaining ({}) issues ...", remainingResultsNumber);
            final Integer pageNumbers = remainingResultsNumber / actualMaxResults;
            List<String> paginatedUrls = IntStream
                    .range(0, pageNumbers + 1)
                    .map(page -> firstResultsNumber + (page * actualMaxResults))
                    .mapToObj(String::valueOf)
                    .map(startIndex -> String.format("%s&startAt=%s&maxResults=%d", urlWithJQL, startIndex, actualMaxResults))
                    .collect(Collectors.toList());
            log.debug("DEFECT|jira|[Jira] {} API calls required", paginatedUrls.size());
            for (String paginatedUrl: paginatedUrls) {
                JiraIssueSearchResults paginatedSearchResult = getSearchResultsFromHeaderAndUrl(header, paginatedUrl);
                List<JiraIssue> paginatedIssues = paginatedSearchResult.getIssues();
                log.debug("DEFECT|jira|[Jira] Pagination: loaded {} issues from url [{}]", paginatedIssues.size(), paginatedUrl);
                allIssues = Stream.of(allIssues, paginatedIssues)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
                log.debug("DEFECT|jira|[Jira] Pagination: Now reaching {} issues", allIssues.size());
            }
        }

        log.debug("DEFECT|jira|[Jira] {} issues effectively loaded", allIssues.size());
        return allIssues;
    }

    /**
     * Get Jira search results from a header and an url
     * @param header the Jira header
     * @param url the Jira REST API url
     * @return the search results containing the issues
     * @throws BadRequestException thrown if (one of) the API call(s) returned an error code
     */
    private JiraIssueSearchResults getSearchResultsFromHeaderAndUrl(HttpHeaders header, String url) throws BadRequestException {
        log.debug("DEFECT|jira|[Jira] Searching issues... ({})", url);

        HttpEntity<JiraIssueSearchResults> request = new HttpEntity<>(header);
        final ParameterizedTypeReference<JiraIssueSearchResults> responseType = new ParameterizedTypeReference<>() {};
        ResponseEntity<JiraIssueSearchResults> response = restTemplate.exchange(url, HttpMethod.GET, request, responseType);

        HttpStatus httpStatus = response.getStatusCode();
        Boolean responseIsNotOK = !HttpStatus.OK.equals(httpStatus);
        if (responseIsNotOK) {
            String errorMessage = String.format("DEFECT|jira|[Jira] The Jira request [%s] %s returned an error status code -> %s", HttpMethod.GET, url, httpStatus);
            log.warn(errorMessage);
            throw new BadRequestException(errorMessage, Entities.SETTING, "jira_request_error");
        }
        return response.getBody();
    }

    /**
     * Get Jira issues having update dates greater than a given date
     * @param projectId the project id
     * @param updateDate the issues update date threshold
     * @return the Jira issues
     * @throws BadRequestException thrown if (one of) the API call(s) returned an error code
     */
    public List<JiraIssue> getUpdatedIssues(Long projectId, Date updateDate) throws BadRequestException {
        String projectCodes = settingService.get(projectId, Settings.DEFECT_JIRA_FILTER_PROJECTS);
        String projectCodesJQL = "";
        if (StringUtils.isNotBlank(projectCodes)) {
            String[] splitProjectCodes = projectCodes
                    .replaceAll("\\s+","")
                    .split(",");
            projectCodesJQL = String.format("project in (%s) AND ", String.join(", ", splitProjectCodes));
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String formattedUpdateDate = dateFormat.format(updateDate);
        String updatedDateJQL = String.format("updated > \"%s\"", formattedUpdateDate);
        String finalJql = String.format("%s%s", projectCodesJQL, updatedDateJQL);

        return searchJiraIssues(projectId, finalJql);
    }

}
