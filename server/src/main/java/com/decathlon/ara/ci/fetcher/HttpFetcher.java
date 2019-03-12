package com.decathlon.ara.ci.fetcher;

import com.decathlon.ara.ci.bean.Artifact;
import com.decathlon.ara.ci.bean.ArtifactHolder;
import com.decathlon.ara.ci.bean.Build;
import com.decathlon.ara.ci.bean.CycleDef;
import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.ci.util.JsonParserConsumer;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.postman.support.StreamingHttpMessageConverter;
import com.decathlon.ara.report.bean.Feature;
import com.decathlon.ara.service.SettingProviderService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.dto.setting.SettingDTO;
import com.decathlon.ara.service.support.Settings;
import com.fasterxml.jackson.core.JsonFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public abstract class HttpFetcher implements PullFetcher {

    private static final ParameterizedTypeReference<List<Feature>> LIST_OF_FEATURES_TYPE =
            new ParameterizedTypeReference<List<Feature>>() {
            };

    private static final ParameterizedTypeReference<List<String>> LIST_OF_STRINGS_TYPE =
            new ParameterizedTypeReference<List<String>>() {
            };

    @NonNull
    protected final SettingService settingService;

    @NonNull
    private final SettingProviderService settingProviderService;

    @NonNull
    private final RestTemplateBuilder restTemplateBuilder;

    @NonNull
    private final JsonFactory jsonFactory;

    /**
     * @param <T> the type of the empty optional to return if exception is a 404 error
     * @param e   the exception thrown by RestTemplate
     * @param url the URL of the request that generated this exception
     * @return empty if the exception is a 404 error
     * @throws FetchException on any exception that is not a 404 error
     */
    private static <T> Optional<T> handle(NestedRuntimeException e, String url)
            throws FetchException {
        if (e instanceof HttpClientErrorException
                && ((HttpClientErrorException) e).getStatusCode() == HttpStatus.NOT_FOUND) {
            return Optional.empty(); // File not generated yet
        }
        throw new FetchException(e, url);
    }

    @Override
    public List<SettingDTO> getSettingDefinitions() {
        return settingProviderService.getJobIndexingHttpDefinitions();
    }

    /**
     * @param projectId the ID of the project in which to work
     * @param build     an execution job on continuous integration
     * @return the downloaded and parsed content of the artifact cycleDefinition.json of the job; empty if the server returned
     * 404 (not generated yet, or build crashed before generating it)
     * @throws FetchException on any network issue, wrong HTTP response status code other than 404 (500...) or
     *                        parsing issue
     */
    @Override
    public Optional<CycleDef> getCycleDefinition(long projectId, Build build) throws FetchException {
        String url = build.getUrl() +
                settingService.get(projectId, Settings.EXECUTION_INDEXER_HTTP_CYCLE_DEFINITION_PATH);
        try {
            return Optional.ofNullable(
                    getAuthenticatedHttpRestTemplate(projectId)
                            .getForObject(url, CycleDef.class));
        } catch (RestClientException | HttpMessageConversionException e) {
            return handle(e, url);
        }
    }

    /**
     * @param projectId the ID of the project in which to work
     * @param run       a given run job execution on continuous integration
     * @return the downloaded and parsed content of the Cucumber's artifact report.json of the job; empty if the server
     * returned 404 (not generated yet, or build crashed before generating it)
     * @throws FetchException on any network issue, wrong HTTP response status code other than 404 (500...) or
     *                        parsing issue
     */
    @Override
    public Optional<List<Feature>> getCucumberReport(long projectId, Run run) throws FetchException {
        String url = run.getJobUrl() +
                settingService.get(projectId, Settings.EXECUTION_INDEXER_HTTP_CUCUMBER_REPORT_PATH);
        try {
            return Optional.ofNullable(
                    getAuthenticatedHttpRestTemplate(projectId)
                            .exchange(url, HttpMethod.GET, null, LIST_OF_FEATURES_TYPE)
                            .getBody());
        } catch (RestClientException | HttpMessageConversionException e) {
            return handle(e, url);
        }
    }

    /**
     * @param projectId the ID of the project in which to work
     * @param run       a given run job execution on continuous integration
     * @return the downloaded and parsed content of the artifact stepDefinitions.json of the job; empty if the server
     * returned 404 (not generated yet, or build crashed before generating it)
     * @throws FetchException on any network issue, wrong HTTP response status code other than 404 (500...) or
     *                        parsing issue
     */
    @Override
    public Optional<List<String>> getCucumberStepDefinitions(long projectId, Run run) throws FetchException {
        String url = run.getJobUrl() +
                settingService.get(projectId, Settings.EXECUTION_INDEXER_HTTP_CUCUMBER_STEP_DEFINITIONS_PATH);
        try {
            return Optional.ofNullable(
                    getAuthenticatedHttpRestTemplate(projectId)
                            .exchange(url, HttpMethod.GET, null, LIST_OF_STRINGS_TYPE)
                            .getBody());
        } catch (RestClientException | HttpMessageConversionException e) {
            return handle(e, url);
        }
    }

    /**
     * @param projectId the ID of the project in which to work
     * @param run       a given run job execution on continuous integration
     * @return the downloaded list of artifact relative paths for the job; empty the job has no artifact (yet?)
     * @throws FetchException on any network issue, wrong HTTP response status code or parsing issue
     */
    @Override
    public List<String> getNewmanReportPaths(long projectId, Run run) throws FetchException {
        String url = run.getJobUrl() +
                settingService.get(projectId, Settings.EXECUTION_INDEXER_HTTP_NEWMAN_REPORTS_PATH);
        String startingFolderToRemove =
                settingService.get(projectId, Settings.EXECUTION_INDEXER_HTTP_NEWMAN_STARTING_FOLDER_TO_REMOVE);
        try {
            return Optional.ofNullable(
                    getAuthenticatedHttpRestTemplate(projectId)
                            .exchange(url, HttpMethod.GET, null, ArtifactHolder.class)
                            .getBody())
                    .map(ArtifactHolder::getArtifacts)
                    .orElseGet(ArrayList::new)
                    .stream()
                    .map(Artifact::getRelativePath)
                    .map(path -> {
                        if (path.startsWith(startingFolderToRemove)) {
                            return path.substring(startingFolderToRemove.length());
                        }
                        return path;
                    })
                    .collect(Collectors.toList());
        } catch (RestClientException | HttpMessageConversionException e) {
            // No handleException because the API always returns something:
            // No 404 error on the API URL: if there is a 404 error, something unexpected went wrong
            throw new FetchException(e, url);
        }
    }

    /**
     * Call StreamService to stream json data from different resources
     *
     * @param projectId        the ID of the project in which to work
     * @param run              a given run job execution on continuous integration
     * @param newmanReportPath relative path of the artifact to fetch
     * @param consumer         used to consume data during stream.
     * @throws FetchException on error while fetching the data (from network errors)
     */
    @Override
    public void streamNewmanResult(long projectId, Run run, String newmanReportPath, JsonParserConsumer consumer)
            throws FetchException {
        String startingFolderToPrepend =
                settingService.get(projectId, Settings.EXECUTION_INDEXER_HTTP_NEWMAN_STARTING_FOLDER_TO_PREPEND);
        final String url = run.getJobUrl() + startingFolderToPrepend + newmanReportPath;

        // * If detectRequestFactory true (default):
        //   HttpComponentsClientHttpRequestFactory will be used and it will consume the entire HTTP response,
        //   even if we close the stream early
        // * If detectRequestFactory false:
        //   SimpleClientHttpRequestFactory will be used and it will close the connection as soon as we ask it to
        try {
            getAuthenticatedHttpRestTemplateBuilder(projectId)
                    .detectRequestFactory(false)
                    .messageConverters(new StreamingHttpMessageConverter<>(jsonFactory, jsonParser -> {
                        consumer.accept(jsonParser);
                        return null; // Not used, but needed for template to work
                    }))
                    .build()
                    .getForObject(url, Object.class);
        } catch (RestClientException | HttpMessageConversionException e) {
            // No handleException because the API always returns something:
            // No 404 error on the API URL: if there is a 404 error, something unexpected went wrong
            throw new FetchException(e, url);
        }
    }

    protected RestTemplateBuilder getAuthenticatedHttpRestTemplateBuilder(long projectId) {
        String user = settingService.get(projectId, Settings.EXECUTION_INDEXER_HTTP_USER);
        String password = settingService.get(projectId, Settings.EXECUTION_INDEXER_HTTP_PASSWORD);

        if (StringUtils.isEmpty(user) || StringUtils.isEmpty(password)) {
            log.info("No authentication because no configured username and/or password for the project {} with the fetcher {}",
                    Long.valueOf(projectId), getClass().getName());
            return restTemplateBuilder;
        }

        return restTemplateBuilder.interceptors(new BasicAuthorizationInterceptor(user, password));
    }

    protected RestTemplate getAuthenticatedHttpRestTemplate(long projectId) {
        return getAuthenticatedHttpRestTemplateBuilder(projectId).build();
    }

}
