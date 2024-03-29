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

package com.decathlon.ara.service.support;

import com.decathlon.ara.domain.Setting;

/**
 * Holds {@link Setting}'s codes standard in ARA core application. Other custom adapters are free to provide other ones.
 */
public class Settings {

    private Settings() {
    }

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

    public static final String DEFECT_JIRA_BASE_URL = "defect.jira.baseUrl";
    public static final String DEFECT_JIRA_TOKEN = "defect.jira.token";
    public static final String DEFECT_JIRA_LOGIN = "defect.jira.login";
    public static final String DEFECT_JIRA_FILTER_PROJECTS = "defect.jira.filter.projects";

    public static final String EXECUTION_INDEXER_FILE_EXECUTION_BASE_PATH = "execution.indexer.file.executionBasePath";
    public static final String EXECUTION_INDEXER_FILE_CYCLE_DEFINITION_PATH = "execution.indexer.file.cycleDefinitionPath";
    public static final String EXECUTION_INDEXER_FILE_BUILD_INFORMATION_PATH = "execution.indexer.file.buildInformationPath";
    public static final String EXECUTION_INDEXER_FILE_DELETE_AFTER_INDEXING_AS_DONE = "execution.indexer.file.deleteAfterIndexingAsDone";
    public static final String EXECUTION_PURGE_DURATION_VALUE = "execution.purge.duration.value";
    public static final String EXECUTION_PURGE_DURATION_TYPE = "execution.purge.duration.type";

    public static final String EMAIL_FROM = "email.from";
    public static final String EMAIL_TO_EXECUTION_CRASHED = "email.to.execution.crashed";
    public static final String EMAIL_TO_EXECUTION_RAN = "email.to.execution.ran";
    public static final String EMAIL_TO_EXECUTION_ELIGIBLE_PASSED = "email.to.execution.eligiblePassed";
    public static final String EMAIL_TO_EXECUTION_ELIGIBLE_WARNING = "email.to.execution.eligibleWarning";
    public static final String EMAIL_TO_EXECUTION_NOT_ELIGIBLE = "email.to.execution.notEligible";

    // Variables
    public static final String PROJECT_VARIABLE = "{{project}}";
    public static final String BRANCH_VARIABLE = "{{branch}}";
    public static final String CYCLE_VARIABLE = "{{cycle}}";

    /**
     * The tree structure inside a given .../executions/ folder.
     */
    public static final String DEFAULT_EXECUTION_VARIABLES = PROJECT_VARIABLE + "/" + BRANCH_VARIABLE + "/" + CYCLE_VARIABLE;
}
