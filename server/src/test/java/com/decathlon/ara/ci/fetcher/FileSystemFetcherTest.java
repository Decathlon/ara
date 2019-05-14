package com.decathlon.ara.ci.fetcher;

import com.decathlon.ara.ci.bean.Build;
import com.decathlon.ara.ci.bean.CountryDeploymentExecution;
import com.decathlon.ara.ci.bean.CycleDef;
import com.decathlon.ara.ci.bean.ExecutionTree;
import com.decathlon.ara.domain.enumeration.Result;
import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.ci.util.JsonParserConsumer;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.report.bean.Feature;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.service.SettingProviderService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.dto.setting.SettingDTO;
import com.decathlon.ara.service.support.Settings;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileSystemFetcherTest {

    private static final String EXECUTIONS_FOLDER = FileSystemFetcherTest.class
            .getResource("/executions").getPath();

    private static final String AN_EXECUTION_FOLDER = EXECUTIONS_FOLDER + "/intestption/master/day/1546257599999";

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SettingService settingService;

    @Mock
    private SettingProviderService settingProviderService;

    @Mock
    private JsonFactory jsonFactory;

    @Spy
    private ObjectMapper objectMapper;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private JsonParserConsumer jsonParserConsumer;

    @InjectMocks
    private FileSystemFetcher cut;

    @Test
    public void getCode_ShouldReturnConstantCodeBecauseItIsStoredInDatabase() {
        assertThat(cut.getCode()).isEqualTo("filesystem");
    }

    @Test
    public void getName_ShouldReturnUserFriendlyNameToShowInDropDownListInProjectSettings() {
        assertThat(cut.getName()).isEqualTo("File-system indexer (from ARA server disk or mount point)");
    }

    @Test
    public void getSettingDefinitions_ShouldReturnDefinitionsFromSettingProviderService() {
        // GIVEN
        List<SettingDTO> expectedSettingDefinitions = Collections.emptyList();
        when(settingProviderService.getJobIndexingFileSystemDefinitions()).thenReturn(expectedSettingDefinitions);

        // WHEN
        final List<SettingDTO> actualSettingDefinitions = cut.getSettingDefinitions();

        // THEN
        assertThat(actualSettingDefinitions).isSameAs(expectedSettingDefinitions);
    }

    @Test
    public void getTree_ShouldReturnDeploymentJobAndRunJobs() throws FetchException {
        // GIVEN
        long projectId = 42;
        when(settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_BUILD_INFORMATION_PATH))
                .thenReturn("/buildInformation.json");
        Build build = new Build().withLink(AN_EXECUTION_FOLDER);

        // WHEN
        ExecutionTree executionTree = cut.getTree(projectId, build);

        // THEN
        assertThat(executionTree.getDeployedCountries().stream().map(CountryDeploymentExecution::getCountry))
                .containsOnly("nl");
        assertThat(executionTree.getNonRegressionTests().stream().map(t -> t.getCountry() + "/" + t.getType()))
                .containsOnly("nl/cucumber", "nl/postman");
    }

    @Test
    public void getCycleDefinition_ShouldReturnCycleDefinition_WhenFileIsPresent() throws FetchException {
        // GIVEN
        long projectId = 42;
        when(settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_CYCLE_DEFINITION_PATH))
                .thenReturn("/cycleDefinition.json");
        Build build = new Build().withLink(AN_EXECUTION_FOLDER);

        // WHEN
        Optional<CycleDef> maybeCycleDefinition = cut.getCycleDefinition(projectId, build);

        // THEN
        assertThat(maybeCycleDefinition).isPresent();
        assertThat(maybeCycleDefinition.get().getPlatformsRules()).containsKey("euin2");
        assertThat(maybeCycleDefinition.get().getPlatformsRules().get("euin2")).isNotEmpty();

        assertThat(maybeCycleDefinition.get().getPlatformsRules().get("euin2").stream().anyMatch(platformRule ->
                "NL".equals(platformRule.getCountry())
                        && "api,api-postman,firefox,performance".equals(platformRule.getTestTypes())
                        && "all".equals(platformRule.getSeverityTags())
                        && !platformRule.isEnabled()
        )).isTrue();

        assertThat(maybeCycleDefinition.get().getQualityThresholds()).isNotEmpty();
        assertThat(maybeCycleDefinition.get().getQualityThresholds()).containsKey("high");
        assertThat(maybeCycleDefinition.get().getQualityThresholds().get("high").getFailure()).isEqualTo(85);
        assertThat(maybeCycleDefinition.get().getQualityThresholds().get("high").getWarning()).isEqualTo(90);
        assertThat(maybeCycleDefinition.get().getQualityThresholds().get("sanity-check").getFailure()).isEqualTo(90);
        assertThat(maybeCycleDefinition.get().getQualityThresholds().get("sanity-check").getWarning()).isEqualTo(95);
        assertThat(maybeCycleDefinition.get().getQualityThresholds().get("medium").getFailure()).isEqualTo(80);
        assertThat(maybeCycleDefinition.get().getQualityThresholds().get("medium").getWarning()).isEqualTo(85);
    }

    @Test
    public void getCycleDefinition_ShouldReturnEmpty_WhenFileIsAbsent() throws FetchException {
        // GIVEN
        long projectId = 42;
        when(settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_CYCLE_DEFINITION_PATH))
                .thenReturn("/nonexistent.json");
        Build build = new Build().withLink(AN_EXECUTION_FOLDER);

        // WHEN
        Optional<CycleDef> maybeCycleDefinition = cut.getCycleDefinition(projectId, build);

        // THEN
        assertThat(maybeCycleDefinition).isNotPresent();
    }

    @Test
    public void getCucumberReport_ShouldReturnReport_WhenFileIsPresent() throws FetchException {
        // GIVEN
        long projectId = 42;
        final Run run = new Run().withJobLink(AN_EXECUTION_FOLDER + "/nl/cucumber");
        when(settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_CUCUMBER_REPORT_PATH))
                .thenReturn("/report.json");

        // WHEN
        Optional<List<Feature>> maybeFeatures = cut.getCucumberReport(projectId, run);

        // THEN
        assertThat(maybeFeatures).isPresent();
        assertThat(maybeFeatures.orElse(Collections.emptyList()).stream().allMatch(feature ->
                "Some feature name".equals(feature.getName())
        )).isTrue();
    }

    @Test
    public void getCucumberReport_ShouldReturnEmpty_WhenFileIsAbsent() throws FetchException {
        // GIVEN
        long projectId = 42;
        final Run run = new Run().withJobUrl(AN_EXECUTION_FOLDER + "/nl/cucumber");
        when(settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_CUCUMBER_REPORT_PATH))
                .thenReturn("/nonexistent.json");

        // WHEN
        Optional<List<Feature>> maybeFeatures = cut.getCucumberReport(projectId, run);

        // THEN
        assertThat(maybeFeatures).isNotPresent();
    }

    @Test
    public void getCucumberStepDefinitions_ShouldReturnStepDefinitionList_WhenFileIsPresent() throws FetchException {
        // GIVEN
        long projectId = 42;
        final Run run = new Run().withJobLink(AN_EXECUTION_FOLDER + "/nl/cucumber");
        when(settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_CUCUMBER_STEP_DEFINITIONS_PATH))
                .thenReturn("/stepDefinitions.json");

        // WHEN
        Optional<List<String>> maybeStepDefinitions = cut.getCucumberStepDefinitions(projectId, run);

        // THEN
        assertThat(maybeStepDefinitions).isPresent();
        assertThat(maybeStepDefinitions.orElse(Collections.emptyList())).containsOnly("^Step \"([^\"]*)\"$");
    }

    @Test
    public void getCucumberStepDefinitions_ShouldReturnEmpty_WhenFileIsAbsent() throws FetchException {
        // GIVEN
        long projectId = 42;
        final Run run = new Run().withJobLink(AN_EXECUTION_FOLDER + "/nl/cucumber");
        when(settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_CUCUMBER_STEP_DEFINITIONS_PATH))
                .thenReturn("/nonexistent.json");

        // WHEN
        Optional<List<String>> maybeStepDefinitions = cut.getCucumberStepDefinitions(projectId, run);

        // THEN
        assertThat(maybeStepDefinitions).isNotPresent();
    }

    @Test
    public void completeBuildInformation_ShouldCompleteThePassedBuildFromBuildInformationJson_WhenPassedAnExecution() throws FetchException {
        // GIVEN
        long projectId = 42;
        Build build = new Build().withLink(AN_EXECUTION_FOLDER);
        when(settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_BUILD_INFORMATION_PATH))
                .thenReturn("/buildInformation.json");

        // WHEN
        cut.completeBuildInformation(projectId, build);

        // THEN
        assertThat(build.isBuilding()).isTrue();
        assertThat(build.getDisplayName()).isEqualTo("b1807.1805041456");
        assertThat(build.getTimestamp()).isEqualTo(1525442442556L);
        assertThat(build.getDuration()).isEqualTo(0);
        assertThat(build.getEstimatedDuration()).isEqualTo(5914168);
        assertThat(build.getResult()).isEqualTo(Result.SUCCESS);
        assertThat(build.getRelease()).isEqualTo("the-release");
        assertThat(build.getVersion()).isEqualTo("the-version");
        assertThat(build.getVersionTimestamp()).isEqualTo(1525442442000L);
        assertThat(build.getComment()).isEqualTo("Some comment");
    }

    @Test
    public void completeBuildInformation_ShouldCompleteThePassedBuildFromBuildInformationJson_WhenPassedACountryDeployment() throws FetchException {
        // GIVEN
        long projectId = 42;
        String countryDeploymentJobUrl = new File(AN_EXECUTION_FOLDER + "/nl").getPath();
        Build build = new Build().withLink(countryDeploymentJobUrl);
        when(settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_BUILD_INFORMATION_PATH))
                .thenReturn("/buildInformation.json");

        // WHEN
        cut.completeBuildInformation(projectId, build);

        // THEN
        assertThat(build.isBuilding()).isFalse();
        assertThat(build.getDisplayName()).isEqualTo("Deployment Job #1");
        assertThat(build.getDuration()).isEqualTo(0);
        assertThat(build.getTimestamp()).isEqualTo(1525442442556L);
        assertThat(build.getEstimatedDuration()).isEqualTo(5914168);
        assertThat(build.getResult()).isEqualTo(Result.UNSTABLE);
    }

    @Test
    public void completeBuildInformation_ShouldCompleteThePassedBuildFromBuildInformationJson_WhenPassedARun() throws FetchException {
        // GIVEN
        long projectId = 42;
        String runJobUrl = new File(AN_EXECUTION_FOLDER + "/nl/postman").getPath();
        Build build = new Build().withLink(runJobUrl);
        when(settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_BUILD_INFORMATION_PATH))
                .thenReturn("/buildInformation.json");

        // WHEN
        cut.completeBuildInformation(projectId, build);

        // THEN
        assertThat(build.isBuilding()).isFalse();
        assertThat(build.getDisplayName()).isEqualTo("Postman Job #3");
        assertThat(build.getDuration()).isEqualTo(396963);
        assertThat(build.getTimestamp()).isEqualTo(1526290193507L);
        assertThat(build.getEstimatedDuration()).isEqualTo(328122);
        assertThat(build.getResult()).isEqualTo(Result.FAILURE);
    }

    @Test
    public void getNewmanReportPaths_ShouldReturnReportFilesInTheReportsFolderOrSubFolders_WhenReportsExist()
            throws FetchException {
        // GIVEN
        long projectId = 42;
        final Run run = new Run().withJobLink(AN_EXECUTION_FOLDER + "/nl/postman");
        when(settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_NEWMAN_REPORTS_PATH))
                .thenReturn("/reports");

        // WHEN
        List<String> artifactsRelativePath = cut.getNewmanReportPaths(projectId, run);

        // THEN
        assertThat(artifactsRelativePath).containsOnly(
                "sub-folder/collection1.json",
                "sub-folder/collection2.json",
                "collection3.json",
                "result.txt");
    }

    @Test(expected = FetchException.class)
    public void getNewmanReportPaths_ShouldThrowFetchException_WhenReportFolderDoesNotExist() throws FetchException {
        // GIVEN
        long projectId = 42;
        final Run run = new Run().withJobLink(AN_EXECUTION_FOLDER + "/nl/postman");
        when(settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_NEWMAN_REPORTS_PATH))
                .thenReturn("/nonexistent");

        // WHEN
        cut.getNewmanReportPaths(projectId, run);
    }

    @Test
    public void streamNewmanResult_ShouldLoadTheCorrectJsonFileByAddingBackTheReportsFolder()
            throws IOException, FetchException, ReflectiveOperationException {
        // WHEN
        final long projectId = 42;
        final Run run = new Run()
                .withJobLink(AN_EXECUTION_FOLDER + "/nl/postman/");
        final String newmanReportPath = "sub-folder/collection1.json";
        when(settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_NEWMAN_REPORTS_PATH))
                .thenReturn("/reports");
        ArgumentCaptor<InputStream> argument = ArgumentCaptor.forClass(InputStream.class);
        when(jsonFactory.createParser(argument.capture())).thenReturn(null);

        // WHEN
        cut.streamNewmanResult(projectId, run, newmanReportPath, jsonParserConsumer);

        // THEN
        assertThat(pathOf(argument.getValue()))
                .endsWith("master/day/1546257599999/nl/postman/reports/sub-folder/collection1.json");
    }

    @Test
    public void streamNewmanResult_ShouldCallConsumerWithTheLoadedFile() throws IOException, FetchException {
        // WHEN
        final long projectId = 42;
        final Run run = new Run()
                .withJobLink(AN_EXECUTION_FOLDER + "/nl/postman/");
        final String newmanReportPath = "sub-folder/collection1.json";
        when(settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_NEWMAN_REPORTS_PATH))
                .thenReturn("/reports");
        when(jsonFactory.createParser(any(InputStream.class))).thenReturn(jsonParser);

        // WHEN
        cut.streamNewmanResult(projectId, run, newmanReportPath, jsonParserConsumer);

        // THEN
        verify(jsonParserConsumer).accept(same(jsonParser));
    }

    @Test(expected = FetchException.class)
    public void streamNewmanResult_ShouldThrowFetchException_WhenFileIsAbsent() throws FetchException {
        // WHEN
        final long projectId = 42;
        final Run run = new Run()
                .withJobLink(AN_EXECUTION_FOLDER + "/nl/postman/");
        final String newmanReportPath = "nonexistent.json";
        when(settingService.get(projectId, Settings.EXECUTION_INDEXER_FILE_NEWMAN_REPORTS_PATH))
                .thenReturn("/reports");

        // WHEN
        cut.streamNewmanResult(projectId, run, newmanReportPath, jsonParserConsumer);
    }

    private String pathOf(InputStream fileInputStream) throws ReflectiveOperationException {
        // Don't pollute the calling test with cast
        FileInputStream realFileInputStream = (FileInputStream) fileInputStream;

        // Get the underlying opened file
        Field field = realFileInputStream.getClass().getDeclaredField("path");
        field.setAccessible(true);
        String path = (String) field.get(realFileInputStream);

        // Make the test behaviour consistent on Windows and Unix
        return path.replace('\\', '/');
    }

}
