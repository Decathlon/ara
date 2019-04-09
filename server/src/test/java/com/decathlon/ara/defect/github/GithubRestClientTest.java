package com.decathlon.ara.defect.github;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class GithubRestClientTest {
    @Mock
    private GithubMapper mapper;

    @Mock
    private HttpClient httpClient;

    @Spy
    @InjectMocks
    private GithubRestClient cut;


    @Test
    public void requestIssue_should_return_the_issue() throws IOException, URISyntaxException {
        // Given
        String owner = "owner";
        String repo = "test";
        String token = "token";
        long issue = 42L;
        String jsonResponse = "{\"key\": \"value\"}";
        GithubIssue expectedIssue = new GithubIssue();
        expectedIssue.setNumber(issue);
        HttpResponse mockedResponse = this.given_an_issue_response(200, jsonResponse);
        Mockito.doReturn(Optional.of(expectedIssue)).when(this.mapper).jsonToIssue(jsonResponse);
        Mockito.doReturn(mockedResponse).when(this.httpClient).execute(Mockito.any());
        this.cut.forOwnerAndRepository(owner, repo).withToken(token);
        // When
        Optional<GithubIssue> result = this.cut.requestIssue(issue);
        // Then
        this.assert_that_request_is_well_formed(owner, repo, token, issue);
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get()).isNotNull();
        Assertions.assertThat(result.get().getNumber()).isEqualTo(issue);
    }

    @Test
    public void requestIssue_should_empty_on_404() throws IOException, URISyntaxException {
        // Given
        String owner = "owner";
        String repo = "test";
        String token = "token";
        long issue = 42L;
        HttpResponse mockedResponse = this.given_an_issue_response(404, "Not found.");
        Mockito.doReturn(mockedResponse).when(this.httpClient).execute(Mockito.any());
        this.cut.forOwnerAndRepository(owner, repo).withToken(token);
        // When
        Optional<GithubIssue> result = this.cut.requestIssue(issue);
        // Then
        this.assert_that_request_is_well_formed(owner, repo, token, issue);
        Mockito.verify(this.mapper, Mockito.never()).jsonToIssue(Mockito.anyString());
        Assertions.assertThat(result).isNotPresent();
    }


    @Test
    public void requestIssue_should_empty_on_410() throws IOException, URISyntaxException {
        // Given
        String owner = "owner";
        String repo = "test";
        String token = "token";
        long issue = 42L;
        HttpResponse mockedResponse = this.given_an_issue_response(410, "Gone.");
        Mockito.doReturn(mockedResponse).when(this.httpClient).execute(Mockito.any());
        this.cut.forOwnerAndRepository(owner, repo).withToken(token);
        // When
        Optional<GithubIssue> result = this.cut.requestIssue(issue);
        // Then
        this.assert_that_request_is_well_formed(owner, repo, token, issue);
        Mockito.verify(this.mapper, Mockito.never()).jsonToIssue(Mockito.anyString());
        Assertions.assertThat(result).isNotPresent();
    }

    @Test
    public void requestIssue_should_throw_exception_on_500() throws IOException {
        // Given
        String owner = "owner";
        String repo = "test";
        String token = "token";
        long issue = 42L;
        HttpResponse mockedResponse = this.given_an_issue_response(500, "Internal Server Error.");
        Mockito.doReturn(mockedResponse).when(this.httpClient).execute(Mockito.any());
        this.cut.forOwnerAndRepository(owner, repo).withToken(token);
        // When
        try {
            this.cut.requestIssue(issue);
            Assertions.fail("IOException is expected on error 500.");
        } catch (IOException | URISyntaxException ex) {
            String expectedMessage = "Error while requesting issue " + issue + " on repo "
                    + owner + "/" + repo + " : 500";
            Assertions.assertThat(ex.getMessage()).isEqualTo(expectedMessage);
        }
    }

    @Test
    public void requestIssues_should_request_all_the_issues() throws IOException, URISyntaxException {
        // Given
        String owner = "owner";
        String repo = "test";
        String token = "token";
        List<Long> issueIds = Lists.list(1L, 2L, 3L, 4L, 5L, 6L, 7L);
        this.cut.forOwnerAndRepository(owner, repo).withToken(token);
        Mockito.doReturn(Optional.of(new GithubIssue())).when(this.cut).requestIssue(Mockito.anyLong());
        // When
        this.cut.requestIssues(issueIds);
        // Then
        Mockito.verify(this.cut, Mockito.times(7)).requestIssue(Mockito.anyLong());
    }

    @Test
    public void getIssuesUpdatedSince_should_return_the_list_of_issues_after_date() throws IOException, URISyntaxException, ParseException {
        // Given
        String owner = "owner";
        String repo = "test";
        String token = "token";
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String expectedDate = "2019-04-04T04:21:00";
        Date date = format.parse(expectedDate);
        String jsonResponse = "[ {\"key\": \"value\"}, {\"key\": \"value2\" } ]";
        GithubIssue issue1 = new GithubIssue();
        issue1.setNumber(42L);
        GithubIssue issue2 = new GithubIssue();
        issue2.setNumber(24L);
        HttpResponse mockedResponse = this.given_an_issue_response(200, jsonResponse);
        Mockito.doReturn(Lists.list(issue1, issue2)).when(this.mapper).jsonToIssueList(jsonResponse);
        Mockito.doReturn(mockedResponse).when(this.httpClient).execute(Mockito.any());
        this.cut.forOwnerAndRepository(owner, repo).withToken(token);
        // When
        List<GithubIssue> issuesUpdatedSince = this.cut.getIssuesUpdatedSince(date);
        // Then
        this.assert_that_issue_since_request_is_well_formed(owner, repo, expectedDate);
        Assertions.assertThat(issuesUpdatedSince).isNotNull();
        Assertions.assertThat(issuesUpdatedSince).hasSize(2);
        Assertions.assertThat(issuesUpdatedSince).containsExactly(issue1, issue2);
    }

    @Test
    public void getIssuesUpdatedSince_should_return_empty_list_on_404() throws IOException, URISyntaxException, ParseException {
        // Given
        String owner = "owner";
        String repo = "test";
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String expectedDate = "2019-04-04T04:21:00";
        Date date = format.parse(expectedDate);
        String contentReponse = "Not Found.";
        HttpResponse mockedResponse = this.given_an_issue_response(404, contentReponse);
        Mockito.doReturn(mockedResponse).when(this.httpClient).execute(Mockito.any());
        this.cut.forOwnerAndRepository(owner, repo);
        // When
        List<GithubIssue> issuesUpdatedSince = this.cut.getIssuesUpdatedSince(date);
        // Then
        this.assert_that_issue_since_request_is_well_formed(owner, repo, expectedDate);
        Assertions.assertThat(issuesUpdatedSince).isNotNull();
        Assertions.assertThat(issuesUpdatedSince).isEmpty();
    }

    @Test
    public void getIssuesUpdatedSince_should_throw_error_on_500() throws IOException, URISyntaxException, ParseException {
        // Given
        String owner = "owner";
        String repo = "test";
        String token = "token";
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String expectedDate = "2019-04-04T04:21:00";
        Date date = format.parse(expectedDate);
        String contentReponse = "Not Found.";
        HttpResponse mockedResponse = this.given_an_issue_response(500, contentReponse);
        Mockito.doReturn(mockedResponse).when(this.httpClient).execute(Mockito.any());
        this.cut.forOwnerAndRepository(owner, repo).withToken(token);
        // When
        try {
            this.cut.getIssuesUpdatedSince(date);
            Assertions.fail("An IOException was expected here.");
        } catch (IOException ex) {
            Assertions.assertThat(ex.getMessage()).isEqualTo("Error while retrieving issues updated since "
                    + expectedDate + " on repo " + owner + "/" + repo + " : 500");
        }
    }

    private HttpResponse given_an_issue_response(int code, String body) throws IOException {
        HttpResponse response = Mockito.mock(HttpResponse.class);
        // Status Line
        StatusLine statusLine = Mockito.mock(StatusLine.class);
        Mockito.doReturn(code).when(statusLine).getStatusCode();
        Mockito.doReturn(statusLine).when(response).getStatusLine();
        // Entity
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        Mockito.doReturn(new ByteArrayInputStream(body.getBytes())).when(entity).getContent();
        Mockito.doReturn(entity).when(response).getEntity();
        return response;
    }

    private void assert_that_request_is_well_formed(String owner, String repo, String token, long issue) throws IOException, URISyntaxException {
        ArgumentCaptor<HttpGet> request = ArgumentCaptor.forClass(HttpGet.class);
        Mockito.verify(this.httpClient).execute(request.capture());
        String expectedPath = GithubRestClient.PROTOCOL + "://" + GithubRestClient.BASEPATH
                + "/repos/" + owner + "/" + repo + "/issues/" + issue;
        expectedPath += "?filter=all&state=all";
        Assertions.assertThat(request.getValue().getURI()).isEqualTo(new URI(expectedPath));
        Assertions.assertThat(request.getValue().containsHeader("Authorization")).isTrue();
    }

    private void assert_that_issue_since_request_is_well_formed(String owner, String repo, String time) throws IOException, URISyntaxException {
        ArgumentCaptor<HttpGet> request = ArgumentCaptor.forClass(HttpGet.class);
        Mockito.verify(this.httpClient).execute(request.capture());
        String expectedPath = GithubRestClient.PROTOCOL + "://" + GithubRestClient.BASEPATH
                + "/repos/" + owner + "/" + repo + "/issues";
        expectedPath += "?filter=all&state=all&since=" + time.replace(":", "%3A");
        Assertions.assertThat(request.getValue().getURI()).isEqualTo(new URI(expectedPath));
        Assertions.assertThat(request.getValue().containsHeader("Authorization")).isTrue();
    }
}