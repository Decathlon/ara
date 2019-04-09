package com.decathlon.ara.defect.github;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Provide Java implementation of the GitHub REST API.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class GithubRestClient {
    static final String PROTOCOL = "https";
    static final String BASEPATH = "api.github.com";

    @Autowired
    private GithubMapper githubMapper;

    private HttpClient httpClient;
    private String currentOwner;
    private String currentRepo;
    private String currentAuthToken;

    /**
     * Define the owner and repository to use for the next requests.
     *
     * @param owner the owner (user or organization) of the Repository
     * @param repo  the name of the Repository
     * @return the calling instance
     */
    GithubRestClient forOwnerAndRepository(String owner, String repo) {
        if (this.isNotEmpty(owner)) {
            this.currentOwner = owner;
        }
        if (this.isNotEmpty(repo)) {
            this.currentRepo = repo;
        }
        return this;
    }

    /**
     * Sets the Authorization token to use for the next requests (usually a Personal Access Token).
     *
     * @param token the Authorization token
     * @return the calling instance
     */
    GithubRestClient withToken(String token) {
        if (this.isNotEmpty(token)) {
            this.currentAuthToken = token;
        }
        return this;
    }

    /**
     * Request the informations about the given issue, based on the owner and repository given before
     * ({@link GithubRestClient#forOwnerAndRepository(String, String)})
     *
     * @param issueId the id of the wanted issue
     * @return the informations about the issue in a POJO.
     * @throws IOException if Github can't be accessed
     * @throws URISyntaxException if the informations provided into the owner and repository name are invalid in the URI.
     */
    Optional<GithubIssue> requestIssue(long issueId) throws IOException, URISyntaxException {
        this.prepareClient();
        String repoPath = this.currentOwner + "/" + this.currentRepo;
        URI uri = new URIBuilder()
                .setScheme(PROTOCOL)
                .setHost(BASEPATH)
                .setPath("/repos/" + repoPath + "/issues/" + issueId)
                .setParameter("filter", "all")
                .setParameter("state", "all")
                .build();
        HttpGet request = new HttpGet(uri);
        request.addHeader("Authorization", "token " + this.currentAuthToken);
        HttpResponse response = this.httpClient.execute(request);
        int responseCode = response.getStatusLine().getStatusCode();
        if (404 == responseCode || 410 == responseCode) {
            return Optional.empty();
        } else if (200 == responseCode) {
            return this.githubMapper.jsonToIssue(this.getContentOf(response));
        } else {
            String msg = "Error while requesting issue " + issueId + " on repo " + repoPath + " : " + responseCode;
            log.warn(msg);
            throw new IOException(msg);
        }
    }

    /**
     * Request the informations about several issues, based on the owner and repository given before
     * ({@link GithubRestClient#forOwnerAndRepository(String, String)})
     *
     * @param issueIds the list of id of the wanted issues
     * @return the informations about the issues in a POJO. The list will contains only the issue with existing ids.
     * @throws IOException if Github can't be accessed
     * @throws URISyntaxException if the informations provided into the owner and repository name are invalid in the URI.
     */
    List<GithubIssue> requestIssues(List<Long> issueIds) throws IOException, URISyntaxException {
        List<GithubIssue> result = new ArrayList<>();
        for (Long issueId : issueIds) {
            this.requestIssue(issueId).ifPresent(result::add);
        }
        return result;
    }

    /**
     * Request the informations about all the issues which has been updated since the given date, based on the owner
     * and repository given before ({@link GithubRestClient#forOwnerAndRepository(String, String)})
     *
     * @param time the start timestamp to search issues.
     * @return the informations about the issues in a POJO.
     * @throws IOException if Github can't be accessed
     * @throws URISyntaxException if the informations provided into the owner and repository name are invalid in the URI.
     */
    List<GithubIssue> getIssuesUpdatedSince(Date time) throws IOException, URISyntaxException {
        this.prepareClient();
        List<GithubIssue> result = new ArrayList<>();
        String repoPath = this.currentOwner + "/" + this.currentRepo;
        String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(time);
        URI uri = new URIBuilder()
                .setScheme(PROTOCOL)
                .setHost(BASEPATH)
                .setPath("/repos/" + repoPath + "/issues")
                .setParameter("filter", "all")
                .setParameter("state", "all")
                .setParameter("since", date)
                .build();
        HttpGet request = new HttpGet(uri);
        request.addHeader("Authorization", "token " + this.currentAuthToken);
        HttpResponse response = this.httpClient.execute(request);
        int responseCode = response.getStatusLine().getStatusCode();
        if (200 == responseCode) {
            result.addAll(this.githubMapper.jsonToIssueList(this.getContentOf(response)));
        } else if (404 != responseCode) {
            String msg = "Error while retrieving issues updated since " + date + " on repo " + repoPath + " : " + responseCode;
            log.warn(msg);
            throw new IOException(msg);
        }
        return result;
    }

    private boolean isNotEmpty(String str) {
        return null != str && !str.trim().isEmpty();
    }

    private String getContentOf(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        StringBuilder content = new StringBuilder();
        try (BufferedReader contentReader = new BufferedReader(new InputStreamReader(entity.getContent()))) {
            String line = contentReader.readLine();
            while (null != line) {
                content.append(line);
                line = contentReader.readLine();
            }
        }
        return content.toString();
    }

    private void prepareClient() {
        if (null == this.httpClient) {
            this.httpClient = HttpClients.createDefault();
        }
    }
}
