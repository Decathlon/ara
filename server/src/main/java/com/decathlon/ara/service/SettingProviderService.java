package com.decathlon.ara.service;

import com.decathlon.ara.ci.fetcher.Fetcher;
import com.decathlon.ara.ci.fetcher.FileSystemFetcher;
import com.decathlon.ara.ci.service.FetcherService;
import com.decathlon.ara.defect.DefectAdapter;
import com.decathlon.ara.service.dto.setting.SettingDTO;
import com.decathlon.ara.service.dto.setting.SettingGroupDTO;
import com.decathlon.ara.service.dto.setting.SettingOptionDTO;
import com.decathlon.ara.service.dto.setting.SettingType;
import com.decathlon.ara.service.support.Settings;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for providing setting definitions. It is ignored from code coverage, as it's merely a configuration file.
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SettingProviderService {

    private static final String EG_QUOTE = "Eg. \"";
    private static final String THE_LIST_IS_CASE_INSENSITIVE = "The list is case insensitive. ";

    private static final Object DEFAULT_EXECUTIONS_FOLDER_LOCK = new Object();

    @NonNull
    private final FetcherService fetcherService;

    @NonNull
    private final DefectService defectService;

    private String defaultExecutionsFolderCache;

    /**
     * Get the project settings, organized as a tree of groups, and populate this tree with the provided values.
     * Note that the presence of some settings depend on the value of other ones (eg. when no defect tracking system is
     * defined, there is no point in configuring defect URLs).
     *
     * @param projectId     the ID of the project in which to work
     * @param projectValues the value of settings configured for this project (to enable some settings and populate
     *                      values on all available settings)
     * @return the settings, tailored to the GUI: definitions, editing helps and values (passwords are hidden)
     */
    List<SettingGroupDTO> getDefinitions(long projectId, Map<String, String> projectValues) {
        List<SettingGroupDTO> groups = new ArrayList<>();
        groups.add(getJobIndexingDefinitions(projectValues));
        groups.add(getEmailReportsDefinitions());
        groups.add(getDefectDefinitions(projectId, projectValues));
        return groups;
    }

    private SettingGroupDTO getJobIndexingDefinitions(Map<String, String> projectValues) {
        SettingGroupDTO group = new SettingGroupDTO("Execution Indexing", new ArrayList<>());

        final List<Fetcher> fetchers = fetcherService.getFetchers();

        final String defaultExecutionIndexer = FileSystemFetcher.FILESYSTEM;
        group.getSettings().add(new SettingDTO()
                .withCode(Settings.EXECUTION_INDEXER)
                .withName("Indexer")
                .withType(SettingType.SELECT)
                .withOptions(fetchers.stream()
                        .map(f -> new SettingOptionDTO(f.getCode(), f.getName()))
                        .collect(Collectors.toList()))
                .withRequired(true)
                .withDefaultValue(defaultExecutionIndexer)
                .withHelp("" +
                        "Define the crawler to use to index new test executions as they become available " +
                        "on the continuous integration server."));

        String configuredExecutionIndexer = projectValues.get(Settings.EXECUTION_INDEXER);
        String currentExecutionIndexer = (StringUtils.isEmpty(configuredExecutionIndexer) ?
                defaultExecutionIndexer :
                configuredExecutionIndexer);
        group.getSettings().addAll(
                fetchers.stream()
                        .filter(f -> currentExecutionIndexer.equals(f.getCode()))
                        .flatMap(f -> f.getSettingDefinitions().stream())
                        .collect(Collectors.toList()));

        return group;
    }

    public List<SettingDTO> getJobIndexingFileSystemDefinitions() {
        List<SettingDTO> settings = new ArrayList<>();

        final String defaultExecutionBasePath = getDefaultExecutionsFolder() + Fetcher.DEFAULT_EXECUTION_VARIABLES;
        settings.add(new SettingDTO()
                .withCode(Settings.EXECUTION_INDEXER_FILE_EXECUTION_BASE_PATH)
                .withName("Execution base path")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withDefaultValue(defaultExecutionBasePath)
                .withHelp("" +
                        "The root path of all jobs for a given branch and cycle. " +
                        "Optional variables you can use in this configuration: " +
                        Fetcher.PROJECT_VARIABLE + " is the code of the project for the given execution, " +
                        Fetcher.BRANCH_VARIABLE + " is the name of the branch for the given execution, " +
                        Fetcher.CYCLE_VARIABLE + " is the name of the given execution. " +
                        EG_QUOTE + defaultExecutionBasePath + " (on Linux) " +
                        "or \"C:/ara/data/executions/" + Fetcher.DEFAULT_EXECUTION_VARIABLES + "\" (on Windows) " +
                        "or \"classpath:/executions/" + Fetcher.DEFAULT_EXECUTION_VARIABLES + "\" (for tests)"));

        final String defaultCycleDefinitionPath = "/cycleDefinition.json";
        settings.add(new SettingDTO()
                .withCode(Settings.EXECUTION_INDEXER_FILE_CYCLE_DEFINITION_PATH)
                .withName("Cycle definition path")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withDefaultValue(defaultCycleDefinitionPath)
                .withHelp("" +
                        "Cycle definition are extracted from this path. " +
                        EG_QUOTE + defaultCycleDefinitionPath + "\", appended to the run's job folder."));

        final String defaultCucumberReportPath = "/report.json";
        settings.add(new SettingDTO()
                .withCode(Settings.EXECUTION_INDEXER_FILE_CUCUMBER_REPORT_PATH)
                .withName("Cucumber report path")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withDefaultValue(defaultCucumberReportPath)
                .withHelp("" +
                        "Cucumber reports are extracted from this path. " +
                        EG_QUOTE + defaultCucumberReportPath + "\", appended to the run's job folder."));

        settings.add(new SettingDTO()
                .withCode(Settings.EXECUTION_INDEXER_FILE_CUCUMBER_STEP_DEFINITIONS_PATH)
                .withName("Cucumber step definitions path")
                .withType(SettingType.STRING)
                .withHelp("" +
                        "Cucumber step definitions are extracted from this path. " +
                        EG_QUOTE + "/stepDefinitions.json\", appended to the run's job folder. " +
                        "If not provided, the cycle-definitions will not be downloaded"));

        final String defaultNewmanReportsPath = "/reports";
        settings.add(new SettingDTO()
                .withCode(Settings.EXECUTION_INDEXER_FILE_NEWMAN_REPORTS_PATH)
                .withName("Newman reports path")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withDefaultValue(defaultNewmanReportsPath)
                .withHelp("" +
                        "Newman reports are extracted from this path. " +
                        EG_QUOTE + defaultNewmanReportsPath + "\", appended to the run's jobUrl."));

        final String defaultBuildInformationPath = "buildInformation.json";
        settings.add(new SettingDTO()
                .withCode(Settings.EXECUTION_INDEXER_FILE_BUILD_INFORMATION_PATH)
                .withName("Build information path")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withDefaultValue(defaultBuildInformationPath)
                .withHelp("" +
                        "Build information are extracted from this path. " +
                        EG_QUOTE + defaultBuildInformationPath + "\", appended to EITHER the execution's jobUrl OR to the run's jobUrl. " +
                        "It is used to complete builds to index when the hierarchy of all deployment and NRT jobs is generated."));

        settings.add(new SettingDTO()
                .withCode(Settings.EXECUTION_INDEXER_FILE_DELETE_AFTER_INDEXING_AS_DONE)
                .withName("Delete after indexing as done")
                .withType(SettingType.BOOLEAN)
                .withDefaultValue(Boolean.TRUE.toString())
                .withHelp("" +
                        "After an execution is not running anymore and is fully indexed in ARA, " +
                        "remove the folder on the file system if checked. " +
                        "The deletion is active by default: you can disable it temporarily to debug what ARA receives, " +
                        "but be careful not to keep the option disabled for too long: disk could quickly become full " +
                        "with big Postman and/or Cucumber report files, " +
                        "and you will have to manually delete the directories yourself or use a cron job."));

        return settings;
    }

    private String getDefaultExecutionsFolder() {
        if (defaultExecutionsFolderCache == null) {
            synchronized (DEFAULT_EXECUTIONS_FOLDER_LOCK) {
                if (defaultExecutionsFolderCache == null) {
                    defaultExecutionsFolderCache = computeDefaultExecutionsFolder();
                }
            }
        }
        return defaultExecutionsFolderCache;
    }

    private String computeDefaultExecutionsFolder() {
        final String defaultDirectory = "/opt/ara/data/executions/"; // Is C:\opt\... on Windows
        try {
            // Can create the directory...
            Files.createDirectories(Paths.get(defaultDirectory));
            // ... and write to it (if created by another process)?
            final Path file = Paths.get(defaultDirectory, "writing-test");
            if (Files.exists(file)) {
                // Created by another process that was killed before deleting the file or was unable to delete the file
                Files.delete(file);
            }
            Files.createFile(file);
            Files.delete(file);
            // Then OK!
            return defaultDirectory;
        } catch (IOException e) {
            final String userDirectory = Paths.get(System.getProperty("user.home"), "ara-data", "executions")
                    .toAbsolutePath().toString() + File.separator;
            log.warn("Cannot create or write to the default executions directory: " +
                            "using {} as the default folder to store executions instead of {}",
                    userDirectory, defaultDirectory, e);
            return userDirectory;
        }
    }

    public List<SettingDTO> getJobIndexingHttpDefinitions() {
        List<SettingDTO> settings = new ArrayList<>();

        settings.add(new SettingDTO()
                .withCode(Settings.EXECUTION_INDEXER_HTTP_USER)
                .withName("User")
                .withType(SettingType.STRING)
                .withHelp("" +
                        "The user name to log in to the HTTP service used to crawl new execution results " +
                        "(may be a continuous integration server like Jenkins or a handmade API to adapt your " +
                        "continuous integration workflow to ARA)."));

        settings.add(new SettingDTO()
                .withCode(Settings.EXECUTION_INDEXER_HTTP_PASSWORD)
                .withName("Password")
                .withType(SettingType.PASSWORD)
                .withHelp("" +
                        "The password or API token to log in to the HTTP service used to crawl new execution results " +
                        "(basic HTTP authentication). If no username nor password are provided, authentication is" +
                        "disabled and requests are done anonymously."));

        settings.add(new SettingDTO()
                .withCode(Settings.EXECUTION_INDEXER_HTTP_EXECUTION_BASE_URL)
                .withName("Execution base URL")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withHelp("" +
                        "The root URL and path of all jobs for a given branch and cycle. " +
                        "Optional variables you can use in this configuration: " +
                        Fetcher.BRANCH_VARIABLE + " is the name of the branch for the given execution, " +
                        Fetcher.CYCLE_VARIABLE + " is the name of the given execution. " +
                        EG_QUOTE + "http://ci.company.com/nrt/" + Fetcher.BRANCH_VARIABLE + "/" + Fetcher.CYCLE_VARIABLE +
                        "\" (by HTTP requests to continuous integration server)."));

        final String defaultCycleDefinitionPath = "/artifact/cycleDefinition.json";
        settings.add(new SettingDTO()
                .withCode(Settings.EXECUTION_INDEXER_HTTP_CYCLE_DEFINITION_PATH)
                .withName("Cycle definition path")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withDefaultValue(defaultCycleDefinitionPath)
                .withHelp("" +
                        "Cycle definition are extracted from this path. " +
                        EG_QUOTE + defaultCycleDefinitionPath + "\", appended to the run's job URL."));

        final String defaultBuildInformationPath = "/artifact/buildInformation.json";
        settings.add(new SettingDTO()
                .withCode(Settings.EXECUTION_INDEXER_HTTP_BUILD_INFORMATION_PATH)
                .withName("Build information path")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withDefaultValue(defaultBuildInformationPath)
                .withHelp("" +
                        "Build information are extracted from this path. " +
                        EG_QUOTE + defaultBuildInformationPath + "\", appended to the run's jobUrl. " +
                        "It is used to download the comment of each NRT run job."));

        final String defaultCucumberReportPath = "/artifact/report.json";
        settings.add(new SettingDTO()
                .withCode(Settings.EXECUTION_INDEXER_HTTP_CUCUMBER_REPORT_PATH)
                .withName("Cucumber report path")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withDefaultValue(defaultCucumberReportPath)
                .withHelp("" +
                        "Cucumber reports are extracted from this path. " +
                        EG_QUOTE + defaultCucumberReportPath + "\", appended to the run's job URL."));

        settings.add(new SettingDTO()
                .withCode(Settings.EXECUTION_INDEXER_HTTP_CUCUMBER_STEP_DEFINITIONS_PATH)
                .withName("Cucumber step definitions path")
                .withType(SettingType.STRING)
                .withHelp("" +
                        "Cucumber step definitions are extracted from this path. " +
                        EG_QUOTE + "/artifact/stepDefinitions.json\", appended to the run's job URL. " +
                        "If not provided, the cycle-definitions will not be downloaded."));

        final String defaultNewmanReportsPath = "/artifact";
        settings.add(new SettingDTO()
                .withCode(Settings.EXECUTION_INDEXER_HTTP_NEWMAN_REPORTS_PATH)
                .withName("Newman reports path")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withDefaultValue(defaultNewmanReportsPath)
                .withHelp("" +
                        "Newman reports are extracted from this path. " +
                        EG_QUOTE + defaultNewmanReportsPath + "\", appended to the run's jobUrl."));

        final String defaultNewmanStartingFolderToRemove = "reports/json/";
        settings.add(new SettingDTO()
                .withCode(Settings.EXECUTION_INDEXER_HTTP_NEWMAN_STARTING_FOLDER_TO_REMOVE)
                .withName("Newman starting folder to remove")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withDefaultValue(defaultNewmanStartingFolderToRemove)
                .withHelp("" +
                        "While receiving all paths of files produced by the Newman job, remove the starting " +
                        "folder path in order to get only report paths that match the original collection paths. " +
                        EG_QUOTE + defaultNewmanStartingFolderToRemove + "\", if the job exports Newman reports in " +
                        "\"reports/json/folder/collection.json\" and collections are named like " +
                        "\"folder/collection.json\": this will allow to generate accurate \"Edit scenario\" links by " +
                        "reconstructing original collection URLs based on their report URLs."));

        final String defaultNewmanStartingFolderToPrepend = "artifact/reports/json/";
        settings.add(new SettingDTO()
                .withCode(Settings.EXECUTION_INDEXER_HTTP_NEWMAN_STARTING_FOLDER_TO_PREPEND)
                .withName("Newman starting folder to prepend")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withDefaultValue(defaultNewmanStartingFolderToPrepend)
                .withHelp("" +
                        "The URL path to prepend to the Newman report file path in order to download that report " +
                        EG_QUOTE + defaultNewmanStartingFolderToPrepend + "\", " +
                        "prepended by the job URL and appended by the report file path."));

        return settings;
    }

    private SettingGroupDTO getEmailReportsDefinitions() {
        SettingGroupDTO group = new SettingGroupDTO("Email Reports", new ArrayList<>());

        group.getSettings().add(new SettingDTO()
                .withCode(Settings.EMAIL_FROM)
                .withName("From")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withHelp("" +
                        "The email address (with an optional name) from which to send reports once a new execution is finished. " +
                        EG_QUOTE + "ARA for Project X <project-x@technical.company.com>\" or just \"project-x@technical.company.com\"."));

        final String theReceiverEmailAddress =
                "The receiver email address (or addresses, separated by commas (\",\")) for an execution ";
        final String noEmailIfNoAddress = "No email will be sent if the setting is not provided.";

        group.getSettings().add(new SettingDTO()
                .withCode(Settings.EMAIL_TO_EXECUTION_CRASHED)
                .withName("To, on crash")
                .withType(SettingType.STRING)
                .withRequired(false)
                .withHelp("" +
                        theReceiverEmailAddress +
                        "without cycleDefinition.json, so we do not know what tests are expected to run for the given cycle. " +
                        EG_QUOTE + "Technical Leader <technical-leader@company.com>\" or just \"technical-leader@company.com\". " +
                        noEmailIfNoAddress));

        group.getSettings().add(new SettingDTO()
                .withCode(Settings.EMAIL_TO_EXECUTION_RAN)
                .withName("To, on ran")
                .withType(SettingType.STRING)
                .withRequired(false)
                .withHelp("" +
                        theReceiverEmailAddress +
                        "that ran but is not set to block the workflow on failure, only run test for information. " +
                        EG_QUOTE + "Project Leader <project-leader@company.com>\" or just \"project-leader@company.com\". " +
                        noEmailIfNoAddress));

        group.getSettings().add(new SettingDTO()
                .withCode(Settings.EMAIL_TO_EXECUTION_ELIGIBLE_PASSED)
                .withName("To, on eligible and passed")
                .withType(SettingType.STRING)
                .withRequired(false)
                .withHelp("" +
                        theReceiverEmailAddress +
                        "that is set to block workflow on failure, and with a quality-status of PASSED " +
                        EG_QUOTE + "Project Team <project@lists.company.com>\" or just \"project@lists.company.com\". " +
                        noEmailIfNoAddress));

        group.getSettings().add(new SettingDTO()
                .withCode(Settings.EMAIL_TO_EXECUTION_ELIGIBLE_WARNING)
                .withName("To, on eligible but warning")
                .withType(SettingType.STRING)
                .withRequired(false)
                .withHelp("" +
                        theReceiverEmailAddress +
                        "that is set to block workflow on failure, and with a quality-status of WARNING " +
                        EG_QUOTE + "Project Team <project@lists.company.com>\" or just \"project@lists.company.com\". " +
                        noEmailIfNoAddress));

        group.getSettings().add(new SettingDTO()
                .withCode(Settings.EMAIL_TO_EXECUTION_NOT_ELIGIBLE)
                .withName("To, on not eligible")
                .withType(SettingType.STRING)
                .withRequired(false)
                .withHelp("" +
                        theReceiverEmailAddress +
                        "that is set to block workflow on failure, and with a quality-status of INCOMPLETE or FAILED " +
                        EG_QUOTE + "Project Team <project@lists.company.com>, Project Leader <leader@company.com>\". " +
                        noEmailIfNoAddress));

        return group;
    }

    private SettingGroupDTO getDefectDefinitions(long projectId, Map<String, String> projectValues) {
        SettingGroupDTO group = new SettingGroupDTO("Defects", new ArrayList<>());

        final List<DefectAdapter> defectAdapters = defectService.getAdapters();

        final List<SettingOptionDTO> indexers = new ArrayList<>();
        indexers.add(new SettingOptionDTO("", ""));
        indexers.addAll(defectAdapters.stream()
                .map(f -> new SettingOptionDTO(f.getCode(), f.getName()))
                .collect(Collectors.toList()));
        group.getSettings().add(new SettingDTO()
                .withCode(Settings.DEFECT_INDEXER)
                .withName("System")
                .withType(SettingType.SELECT)
                .withOptions(indexers)
                .withRequired(false)
                .withApplyChange(newValue -> defectService.refreshDefectExistences(projectId))
                .withHelp("" +
                        "Define the system used to store and manage defects, " +
                        "in order to update problem statuses with the defect statuses from this provider. " +
                        "If none is provided, problem's defects will not be linked: " +
                        "users will have to open/close them manually."));

        String currentDefectAdapter = projectValues.get(Settings.DEFECT_INDEXER);
        if (StringUtils.isNotEmpty(currentDefectAdapter)) {
            group.getSettings().add(new SettingDTO()
                    .withCode(Settings.DEFECT_URL_FORMAT)
                    .withName("URL format")
                    .withType(SettingType.STRING)
                    .withRequired(false)
                    .withHelp("" +
                            "Problems can be assigned a defect ID: " +
                            "the URL format is used to construct the link for users to view properties of the defect. " +
                            "The \"{{id}}\" placeholder is used to place the defect ID in the constructed URL. " +
                            "Eg. for a defect ID \"PROJECT-42\" and an URL format " +
                            "\"http://bugtracker.company.com/issues/{{id}}\", the ID will link to " +
                            "\"http://bugtracker.company.com/issues/PROJECT-42\". " +
                            "If the URL format is not defined, defect IDs will not be links.")
                    .withValidate(value -> StringUtils.isEmpty(value) || StringUtils.contains(value, "{{id}}") ? null :
                            "The \"{{id}}\" placeholder is required."));
            group.getSettings().addAll(
                    defectAdapters.stream()
                            .filter(f -> f.getCode().equals(currentDefectAdapter))
                            .flatMap(f -> f.getSettingDefinitions().stream())
                            .collect(Collectors.toList()));
        }

        return group;
    }

    public List<SettingDTO> getDefectRtcDefinitions() {
        List<SettingDTO> settings = new ArrayList<>();

        settings.add(new SettingDTO()
                .withCode(Settings.DEFECT_RTC_ROOT_URL)
                .withName("Root URL")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withHelp("" +
                        "Root URL of RTC to query work-item statuses: includes protocol, domain and port, but NO path. " +
                        EG_QUOTE + "https://rtc.my-company.com/ccm\"."));

        String defaultPreAuthenticatePath = "/authenticated/identity";
        settings.add(new SettingDTO()
                .withCode(Settings.DEFECT_RTC_PRE_AUTHENTICATE_PATH)
                .withName("Pre-authenticate path")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withDefaultValue(defaultPreAuthenticatePath)
                .withHelp("" +
                        "Path (to be appended to the root URL) " +
                        "of the page to query (GET) before and after authentication. " +
                        EG_QUOTE + defaultPreAuthenticatePath + "\""));

        String defaultAuthenticatePath = "/authenticated/j_security_check";
        settings.add(new SettingDTO()
                .withCode(Settings.DEFECT_RTC_AUTHENTICATE_PATH)
                .withName("Authenticate path")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withDefaultValue(defaultAuthenticatePath)
                .withHelp("" +
                        "Path (to be appended to the root URL) " +
                        "of the Ajax URL to query (POST) to send authentication credentials. " +
                        EG_QUOTE + defaultAuthenticatePath + "\""));

        String defaultWorkItemResourcePath = "/rpt/repository/work" + "item/";
        settings.add(new SettingDTO()
                .withCode(Settings.DEFECT_RTC_WORK_ITEM_RESOURCE_PATH)
                .withName("Work-item resource path")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withDefaultValue(defaultWorkItemResourcePath)
                .withHelp("" +
                        "Path (to be appended to the root path) " +
                        "of the URL to query all work-items (filters query will be appended). " +
                        EG_QUOTE + defaultWorkItemResourcePath + "\""));

        settings.add(new SettingDTO()
                .withCode(Settings.DEFECT_RTC_USERNAME)
                .withName("Username")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withHelp("" +
                        "Username to authenticate to RTC (only read actions will be done)."));

        settings.add(new SettingDTO()
                .withCode(Settings.DEFECT_RTC_PASSWORD)
                .withName("Password")
                .withType(SettingType.PASSWORD)
                .withRequired(true)
                .withHelp("" +
                        "Password to authenticate to RTC (only read actions will be done)."));

        settings.add(new SettingDTO()
                .withCode(Settings.DEFECT_RTC_BATCH_SIZE)
                .withName("Batch size")
                .withType(SettingType.INT)
                .withDefaultValue("100")
                .withRequired(true)
                .withHelp("" +
                        "Number of batched work-items to request at once per HTTP request to RTC. " +
                        "Will be used to form a filter passed in URL (resulting URL should not be longer than 2000 " +
                        "characters for interoperability) and as page size when requesting recently modified items."));

        String defaultWorkItemTypes = "Defect,Issue,Task";
        settings.add(new SettingDTO()
                .withCode(Settings.DEFECT_RTC_WORK_ITEM_TYPES)
                .withName("Work-item types")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withDefaultValue(defaultWorkItemTypes)
                .withHelp("" +
                        "All RTC work-item types to support (and watch) for problem defect assignation. " +
                        THE_LIST_IS_CASE_INSENSITIVE +
                        EG_QUOTE + defaultWorkItemTypes + "\""));

        String defaultClosedStates = "Closed,Done,Invalid,Resolved,Verified";
        settings.add(new SettingDTO()
                .withCode(Settings.DEFECT_RTC_CLOSED_STATES)
                .withName("Closed states")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withDefaultValue(defaultClosedStates)
                .withHelp("" +
                        "List of case-insensitive RTC states for RTC defects, tasks and issues whose identifiers " +
                        "will be used in ARA problems, and whose problems should be considered as CLOSED. " +
                        "States are separated with commas (\",\"). " +
                        EG_QUOTE + defaultClosedStates + "\". " +
                        THE_LIST_IS_CASE_INSENSITIVE +
                        "If a state is configured to be considered both CLOSED and OPEN, CLOSED wins, with a warning in logs. " +
                        "If a state is not configured, it will be considered OPEN, with a warning in logs."));

        String defaultOpenStates = "Blocked,New,In progress,Deploy ready,Reopened,Test ready,Triaged,Waiting for info,Waiting for review";
        settings.add(new SettingDTO()
                .withCode(Settings.DEFECT_RTC_OPEN_STATES)
                .withName("Open states")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withDefaultValue(defaultOpenStates)
                .withHelp("" +
                        "List of case-insensitive RTC states for RTC defects, tasks and issues whose identifiers " +
                        "will be used in ARA problems, and whose problems should be considered as OPEN. " +
                        "States are separated with commas (\",\"). " +
                        EG_QUOTE + defaultOpenStates + "\". " +
                        THE_LIST_IS_CASE_INSENSITIVE +
                        "If a state is configured to be considered both CLOSED and OPEN, CLOSED wins, with a warning in logs. " +
                        "If a state is not configured, it will be considered OPEN, with a warning in logs."));

        return settings;
    }

    public List<SettingDTO> getDefectGithubDefinitions() {
        List<SettingDTO> result = new ArrayList<>();

        result.add(new SettingDTO()
                .withCode(Settings.DEFECT_GITHUB_OWNER)
                .withName("Github Repository's owner")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withHelp("" +
                        "The owner of this project's Github repository. Usually the user or organization which " +
                        "holds the repository."));

        result.add(new SettingDTO()
                .withCode(Settings.DEFECT_GITHUB_REPONAME)
                .withName("Github Repository's name")
                .withType(SettingType.STRING)
                .withRequired(true)
                .withHelp("The name of this project's Github repository."));

        result.add(new SettingDTO()
                .withCode(Settings.DEFECT_GITHUB_TOKEN)
                .withName("Authorization token")
                .withType(SettingType.STRING)
                .withRequired(false)
                .withHelp("" +
                        "If your project's repository is a private one, you need to put here the personal token of " +
                        "a user authorized to read the repository." +
                        "To create a personal access token, on Github, go to the Settings page of your account, then " +
                        "click on the 'Developer settings' menu and click on the 'Personal Access Token' menu item. " +
                        "In this page, generate a new token (enable sso if your organization use it), and copy the " +
                        "Authorization token displayed in this field."));
        return result;
    }

}
