package com.decathlon.ara.ci.service;

import com.decathlon.ara.ci.fetcher.Fetcher;
import com.decathlon.ara.ci.fetcher.FileSystemFetcher;
import com.decathlon.ara.ci.fetcher.HttpFetcher;
import com.decathlon.ara.ci.fetcher.PullFetcher;
import com.decathlon.ara.common.NotGonnaHappenException;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.SettingProviderService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.support.Settings;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class FetcherServiceTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private SettingService settingService;

    @Mock
    private ApplicationContext applicationContext;

    @Spy
    @InjectMocks
    private FetcherService cut;

    @Test
    public void get_should_return_the_fetcher_of_the_given_project() {
        // Given
        String fetcherCode = FileSystemFetcher.FILESYSTEM;
        long projectId = 18L;
        this.given_fetchers_for_application_context();
        Mockito.doReturn(fetcherCode).when(settingService).get(projectId, Settings.EXECUTION_INDEXER);
        // When
        Fetcher fetcher = this.cut.get(projectId);
        // Then
        Assertions.assertThat(fetcher.getCode()).isEqualTo(fetcherCode);
        Assertions.assertThat(fetcher.getClass()).isAssignableFrom(FileSystemFetcher.class);
    }

    @Test(expected = NotGonnaHappenException.class)
    public void get_should_throws_exception_if_custom_fetcher_on_the_given_project() {
        // Given
        String fetcherCode = "Totally-A-Not-Existing-Fetcher-Code";
        long projectId = 32L;
        this.given_fetchers_for_application_context();
        Mockito.doReturn(fetcherCode).when(settingService).get(projectId, Settings.EXECUTION_INDEXER);
        // When
        this.cut.get(projectId);
    }

    @Test
    public void usePullFetcher_should_return_true_on_pull_fetcher() {
        // Given
        long projectId = 55L;
        PullFetcher httpFetcher = Mockito.mock(HttpFetcher.class);
        Mockito.doReturn(httpFetcher).when(this.cut).get(projectId);
        // When
        boolean result = this.cut.usePullFetcher(projectId);
        // Then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void usePullFetcher_should_return_false_on_push_fetcher() {
        // Given
        long projectId = 59L;
        Fetcher pullFetcher = Mockito.mock(FileSystemFetcher.class);
        Mockito.doReturn(pullFetcher).when(this.cut).get(projectId);
        // When
        boolean result = this.cut.usePullFetcher(projectId);
        // Then
        Assertions.assertThat(result).isFalse();
    }

    private void given_fetchers_for_application_context() {
        Map<String, Fetcher> fetchersInContext = new HashMap<>();
        Fetcher fetcher1 = Mockito.mock(HttpFetcher.class);
        Fetcher fetcher2 = new FileSystemFetcher(
                Mockito.mock(ProjectRepository.class),
                settingService,
                Mockito.mock(SettingProviderService.class),
                Mockito.mock(JsonFactory.class),
                Mockito.mock(ObjectMapper.class));
        fetchersInContext.put("Fetcher1", fetcher1);
        fetchersInContext.put("Fetcher2", fetcher2);
        Mockito.doReturn("Some name for sorting of fetchers").when(fetcher1).getName();
        Mockito.doReturn(fetchersInContext).when(this.applicationContext).getBeansOfType(Fetcher.class);
    }

}
