package com.decathlon.ara;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Messages {

    public static final String NOT_FOUND_COMMUNICATION = "The communication does not exist: it has perhaps been removed.";
    public static final String NOT_FOUND_COUNTRY = "The country does not exist: it has perhaps been removed.";
    public static final String NOT_FOUND_CYCLE_DEFINITION = "The cycle definition does not exist: it has perhaps been removed.";
    public static final String NOT_FOUND_DEFECT = "The work item does not exist: please verify the ID, or it has perhaps been removed.";
    public static final String NOT_FOUND_ERROR = "The error does not exist: it has perhaps been removed.";
    public static final String NOT_FOUND_EXECUTION = "The execution does not exist: it has perhaps been removed.";
    public static final String NOT_FOUND_FUNCTIONALITY = "The functionality does not exist: it has perhaps been removed.";
    public static final String NOT_FOUND_FUNCTIONALITY_FOLDER = "The folder does not exist: it has perhaps been removed.";
    public static final String NOT_FOUND_FUNCTIONALITY_OR_FOLDER = "The functionality or folder does not exist: it has perhaps been removed.";
    public static final String NOT_FOUND_FUNCTIONALITY_OR_FOLDER_REFERENCE = "The functionality or folder where to insert does not exist: it has perhaps been removed.";
    public static final String NOT_FOUND_FUNCTIONALITY_OR_FOLDER_TO_MOVE = "The functionality or folder to move does not exist: it has perhaps been removed.";
    public static final String NOT_FOUND_PATTERN = "The pattern does not exist: it has perhaps been removed.";
    public static final String NOT_FOUND_PATTERN_TO_MOVE = "The pattern to move does not exist: it has perhaps been removed.";
    public static final String NOT_FOUND_PROBLEM = "The problem does not exist: it has perhaps been removed.";
    public static final String NOT_FOUND_PROBLEM_DESTINATION = "The problem where to move the pattern does not exist: it has perhaps been removed.";
    public static final String NOT_FOUND_PROBLEM_PATTERN = "The rule does not exist: it has perhaps been removed.";
    public static final String NOT_FOUND_PROJECT = "The project does not exist: it has perhaps been removed.";
    public static final String NOT_FOUND_ROOT_CAUSE = "The root cause does not exist: it has perhaps been removed.";
    public static final String NOT_FOUND_SETTING = "This setting is not allowed for this project.";
    public static final String NOT_FOUND_SETTING_OPTION = "The option is not supported by this setting.";
    public static final String NOT_FOUND_SOURCE = "The source does not exist: it has perhaps been removed.";
    public static final String NOT_FOUND_TEAM = "The team does not exist: it has perhaps been removed.";
    public static final String NOT_FOUND_TYPE = "The type does not exist: it has perhaps been removed.";

    public static final String NOT_UNIQUE_COUNTRY_CODE = "The code is already used by another country.";
    public static final String NOT_UNIQUE_COUNTRY_NAME = "The name is already used by another country.";
    public static final String NOT_UNIQUE_CYCLE_DEFINITION_NAME_BRANCH = "The branch and name couple is already used by another cycle.";
    public static final String NOT_UNIQUE_FUNCTIONALITY_FOLDER_NAME = "The name is already used by another folder in the same parent folder.";
    public static final String NOT_UNIQUE_FUNCTIONALITY_NAME = "The name is already used by another functionality in the same folder.";
    public static final String NOT_UNIQUE_PATTERN_IN_PROBLEM = "A pattern with the same criterion already exists for this problem.";
    public static final String NOT_UNIQUE_PROBLEM_DEFECT_ID = "The defect ID is already assigned to another problem.";
    public static final String NOT_UNIQUE_PROBLEM_NAME = "The name is already used by another problem.";
    public static final String NOT_UNIQUE_PROJECT_CODE = "The code is already used by another project.";
    public static final String NOT_UNIQUE_PROJECT_DEFAULT_AT_STARTUP = "There is already another default project.";
    public static final String NOT_UNIQUE_PROJECT_NAME = "The name is already used by another project.";
    public static final String NOT_UNIQUE_ROOT_CAUSE_NAME = "The name is already used by another root cause.";
    public static final String NOT_UNIQUE_SEVERITY_CODE = "A severity with this code already exists.";
    public static final String NOT_UNIQUE_SEVERITY_DEFAULT_ON_MISSION = "There is already another default severity.";
    public static final String NOT_UNIQUE_SEVERITY_INITIALS = "A severity with these initials already exists.";
    public static final String NOT_UNIQUE_SEVERITY_NAME = "A severity with this name already exists.";
    public static final String NOT_UNIQUE_SEVERITY_POSITION = "A severity with this position already exists.";
    public static final String NOT_UNIQUE_SEVERITY_SHORT_NAME = "A severity with this short name already exists.";
    public static final String NOT_UNIQUE_SOURCE_CODE = "The code is already used by another source.";
    public static final String NOT_UNIQUE_SOURCE_LETTER = "The letter is already used by another source.";
    public static final String NOT_UNIQUE_SOURCE_NAME = "The name is already used by another source.";
    public static final String NOT_UNIQUE_TEAM_NAME = "The name is already used by another team.";
    public static final String NOT_UNIQUE_TYPE_CODE = "The code is already used by another type.";
    public static final String NOT_UNIQUE_TYPE_NAME = "The name is already used by another type.";

    public static final String PROCESS_ERROR_WHILE_CONTACTING_DEFECT_TRACKING_SYSTEM = "A problem occurred while contacting %s.";

    public static final String RULE_COUNTRY_USED_BY_COUNTRY_DEPLOYMENT = "The country is used by at least one deployment in an execution: please wait for executions with such deployments to be purged.";
    public static final String RULE_COUNTRY_USED_BY_FUNCTIONALITY = "The country is used by at least one functionality: please remove the country from such functionalities.";
    public static final String RULE_COUNTRY_USED_BY_PROBLEM_PATTERN = "The country is used by at least one rule of problem: please remove such rules and/or problems.";
    public static final String RULE_COUNTRY_USED_BY_RUN = "The country is used by at least one run in an execution: please wait for executions with such runs to be purged.";
    public static final String RULE_COUNTRY_USED_BY_SCENARIO = "The country is used by at least one scenario in Version Control System: please remove the country from such scenarios.";
    public static final String RULE_CYCLE_DEFINITION_USED_BY_EXECUTION = "The cycle definition is used by at least one execution: please wait for such executions to be purged.";
    public static final String RULE_DEMO_PROJECT_ALREADY_EXISTS = "The demo project already exists.";
    public static final String RULE_SOURCE_USED_BY_SCENARIO = "The source is used by at least one scenario in Version Control System: you cannot remove such source.";
    public static final String RULE_SOURCE_USED_BY_TYPE = "The source is used by at least one type: please remove such types.";
    public static final String RULE_DISCARDED_EXECUTIONS_MUST_HAVE_REASON = "A reason is mandatory when discarding an execution.";
    public static final String RULE_EXECUTED_SCENARIO_HISTORY_MANDATORY_CUCUMBER_ID = "The cucumber ID of the scenario to get history is mandatory.";
    public static final String RULE_FUNCTIONALITY_EXCLUSIVE_STARTED_AND_NOT_AUTOMATABLE = "A functionality cannot be both non-automatable and started.";
    public static final String RULE_FUNCTIONALITY_FOLDER_MANDATORY_NAME = "A folder must have a name.";
    public static final String RULE_FUNCTIONALITY_MANDATORY_COUNTRY_CODES = "A functionality must have at least one country.";
    public static final String RULE_FUNCTIONALITY_MANDATORY_SEVERITY = "A functionality must have a severity.";
    public static final String RULE_FUNCTIONALITY_MANDATORY_TEAM = "A functionality must have a team.";
    public static final String RULE_FUNCTIONALITY_FOLDER_HAVE_NO_COVERAGE = "A folder cannot have coverage.";
    public static final String RULE_FUNCTIONALITY_FOLDER_ONLY_NAME = "A folder can only have a name.";
    public static final String RULE_FUNCTIONALITY_HAVE_NO_CHILDREN = "Functionality cannot have children.";
    public static final String RULE_FUNCTIONALITY_MANDATORY_NAME = "A functionality must have a name.";
    public static final String RULE_FUNCTIONALITY_MOVE_TO_ITSELF_OR_SUB_FOLDER = "You cannot move a folder or functionality to itself or a sub-folder.";
    public static final String RULE_FUNCTIONALITY_NO_REFERENCE = "Attempting to insert above or below no functionality nor folder.";
    public static final String RULE_FUNCTIONALITY_SEVERITY_WRONG = "Wrong severity: it does not exist.";
    public static final String RULE_FUNCTIONALITY_UNKNOWN_TYPE = "Unknown type.";
    public static final String RULE_PATTERN_MOVE_NO_OP = "You tried to move the pattern into its own problem.";
    public static final String RULE_PROBLEM_CLOSED_MUST_HAVE_ROOT_CAUSE = "A closed problem must have a root cause.";
    public static final String RULE_PROBLEM_DEFECT_REFRESH_WITHOUT_DEFECT_SYSTEM = "The project has no defect tracker system link configured anymore in ARA. Refresh the page to be able to close or open the problem manually.";
    public static final String RULE_PROBLEM_WITH_DEFECT_CANNOT_CHANGE_STATUS_MANUALLY = "A problem cannot be closed nor reopened manually when having a defect: please change the tracked defect status instead, and the problem will be updated.";
    public static final String RULE_PROBLEM_WITH_WRONG_DEFECT_ID_FORMAT = "The work item id is malformed: %s.";
    public static final String RULE_SCENARIO_UPLOAD_TO_WRONG_TECHNOLOGY = "Trying to upload %s scenarios to a %s source.";
    public static final String RULE_SETTING_REQUIRED = "This setting is required and has no default value.";
    public static final String RULE_SETTING_WRONG_FORMAT_BOOLEAN = "This setting must be a boolean.";
    public static final String RULE_SETTING_WRONG_FORMAT_INT = "This setting must be an integer number.";
    public static final String RULE_TEAM_HAS_ASSIGNED_FUNCTIONALITIES = "There are functionalities assigned to this team: please remove or change assignations before forbidding functionality assignation with this team.";
    public static final String RULE_TEAM_HAS_ASSIGNED_PROBLEMS = "There are problems assigned to this team: please remove or change assignations before forbidding problem assignation with this team.";
    public static final String RULE_TEAM_HAS_FUNCTIONALITIES = "There are functionalities assigned to this team: please remove or change assignations before deleting this team.";
    public static final String RULE_TEAM_HAS_PROBLEMS = "There are problems assigned to this team: please remove or change assignations before deleting this team.";
    public static final String RULE_TEAM_NOT_ASSIGNABLE_TO_FUNCTIONALITIES = "The team cannot be assigned to a functionality.";
    public static final String RULE_TEAM_NOT_ASSIGNABLE_TO_PROBLEMS = "The team cannot be assigned to a problem.";
    public static final String RULE_TYPE_USED_BY_PROBLEM_PATTERN = "The type is used by at least one rule of problem: please remove such rules and/or problems.";
    public static final String RULE_TYPE_USED_BY_RUN = "The type is used by at least one run in an execution: please wait for executions with runs of such types to be purged.";

    public static final String IMPORT_POSTMAN_ZIP_ERROR = "The given Postman ZIP reports can't be extracted.";
    public static final String IMPORT_POSTMAN_NOT_FS_INDEXER = "The given project should use the FileSystem Indexer for this action to work.";
}
