package com.decathlon.ara.service.support;

import com.decathlon.ara.domain.Setting;
import lombok.experimental.UtilityClass;

/**
 * Holds {@link Setting}'s codes standard in ARA core application. Other custom adapters are free to provide other ones.
 */
@UtilityClass
public class Settings {

    // IMPORTANT: Once in production, do not change these IDs, as they are stored in user databases.
    // If name is really required to change, you must provide a Liquibase migration change-set.

    public static final String DEFECT_URL_FORMAT = "defect.urlFormat";
    public static final String DEFECT_INDEXER = "defect.indexer";

    public static final String DEFECT_RTC_ROOT_URL = "defect.rtc.rootUrl";
    public static final String DEFECT_RTC_PRE_AUTHENTICATE_PATH = "defect.rtc.preAuthenticatePath";
    public static final String DEFECT_RTC_AUTHENTICATE_PATH = "defect.rtc.authenticatePath";
    public static final String DEFECT_RTC_WORK_ITEM_RESOURCE_PATH = "defect.rtc.workItemResourcePath";
    public static final String DEFECT_RTC_USERNAME = "defect.rtc.username";
    @SuppressWarnings("squid:S2068") // This is a key, and not a password value (Credentials should not be hard-coded)
    public static final String DEFECT_RTC_PASSWORD = "defect.rtc.password";
    public static final String DEFECT_RTC_BATCH_SIZE = "defect.rtc.batchSize";
    public static final String DEFECT_RTC_WORK_ITEM_TYPES = "defect.rtc.workItemTypes";
    public static final String DEFECT_RTC_CLOSED_STATES = "defect.rtc.closedStates";
    public static final String DEFECT_RTC_OPEN_STATES = "defect.rtc.openStates";

    public static final String DEFECT_GITHUB_OWNER = "defect.github.owner";
    public static final String DEFECT_GITHUB_REPONAME = "defect.github.repositoryName";
    public static final String DEFECT_GITHUB_TOKEN = "defect.github.authorizationToken";

    public static final String EXECUTION_INDEXER = "execution.indexer";

    public static final String EXECUTION_INDEXER_FILE_EXECUTION_BASE_PATH = "execution.indexer.file.executionBasePath";
    public static final String EXECUTION_INDEXER_FILE_CYCLE_DEFINITION_PATH = "execution.indexer.file.cycleDefinitionPath";
    public static final String EXECUTION_INDEXER_FILE_CUCUMBER_REPORT_PATH = "execution.indexer.file.cucumberReportPath";
    public static final String EXECUTION_INDEXER_FILE_CUCUMBER_STEP_DEFINITIONS_PATH = "execution.indexer.file.cucumberStepDefinitionsPath";
    public static final String EXECUTION_INDEXER_FILE_NEWMAN_REPORTS_PATH = "execution.indexer.file.newmanReportsPath";
    public static final String EXECUTION_INDEXER_FILE_BUILD_INFORMATION_PATH = "execution.indexer.file.buildInformationPath";
    public static final String EXECUTION_INDEXER_FILE_DELETE_AFTER_INDEXING_AS_DONE = "execution.indexer.file.deleteAfterIndexingAsDone";

    public static final String EXECUTION_INDEXER_HTTP_USER = "execution.indexer.http.user";
    @SuppressWarnings("squid:S2068") // This is a key, and not a password value (Credentials should not be hard-coded)
    public static final String EXECUTION_INDEXER_HTTP_PASSWORD = "execution.indexer.http.password";
    public static final String EXECUTION_INDEXER_HTTP_EXECUTION_BASE_URL = "execution.indexer.http.executionBaseUrl";
    public static final String EXECUTION_INDEXER_HTTP_CYCLE_DEFINITION_PATH = "execution.indexer.http.cycleDefinitionPath";
    public static final String EXECUTION_INDEXER_HTTP_BUILD_INFORMATION_PATH = "execution.indexer.http.buildInformationPath";
    public static final String EXECUTION_INDEXER_HTTP_CUCUMBER_REPORT_PATH = "execution.indexer.http.cucumberReportPath";
    public static final String EXECUTION_INDEXER_HTTP_CUCUMBER_STEP_DEFINITIONS_PATH = "execution.indexer.http.cucumberStepDefinitionsPath";
    public static final String EXECUTION_INDEXER_HTTP_NEWMAN_REPORTS_PATH = "execution.indexer.http.newmanReportsPath";
    public static final String EXECUTION_INDEXER_HTTP_NEWMAN_STARTING_FOLDER_TO_REMOVE = "execution.indexer.http.newmanStartingFolderToRemove";
    public static final String EXECUTION_INDEXER_HTTP_NEWMAN_STARTING_FOLDER_TO_PREPEND = "execution.indexer.http.newmanStartingFolderToPrepend";

    public static final String EMAIL_FROM = "email.from";
    public static final String EMAIL_TO_EXECUTION_CRASHED = "email.to.execution.crashed";
    public static final String EMAIL_TO_EXECUTION_RAN = "email.to.execution.ran";
    public static final String EMAIL_TO_EXECUTION_ELIGIBLE_PASSED = "email.to.execution.eligiblePassed";
    public static final String EMAIL_TO_EXECUTION_ELIGIBLE_WARNING = "email.to.execution.eligibleWarning";
    public static final String EMAIL_TO_EXECUTION_NOT_ELIGIBLE = "email.to.execution.notEligible";

}
