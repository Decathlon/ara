package com.decathlon.ara.ci.fetcher;

import com.decathlon.ara.ci.bean.Build;
import com.decathlon.ara.ci.bean.CycleDef;
import com.decathlon.ara.ci.bean.ExecutionTree;
import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.ci.util.JsonParserConsumer;
import com.decathlon.ara.domain.Execution;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.report.bean.Feature;
import com.decathlon.ara.service.dto.setting.SettingDTO;
import java.util.List;
import java.util.Optional;

public interface Fetcher {

    String PROJECT_VARIABLE = "{{project}}";
    String BRANCH_VARIABLE = "{{branch}}";
    String CYCLE_VARIABLE = "{{cycle}}";

    /**
     * The tree structure inside a given .../executions/ folder.
     */
    String DEFAULT_EXECUTION_VARIABLES = PROJECT_VARIABLE + "/" + BRANCH_VARIABLE + "/" + CYCLE_VARIABLE;

    /**
     * @return the code to uniquely identify this fetcher class (stored in the project settings in database)
     */
    String getCode();

    /**
     * @return the displayed name of this fetcher class (in a list box in the project settings interface)
     */
    String getName();

    /**
     * @return the list of settings to configure this fetcher for a given project
     */
    List<SettingDTO> getSettingDefinitions();

    /**
     * @param projectId the ID of the project in which to work
     * @param build     an execution job on continuous integration
     * @return the hierarchy of all deployment and NRT jobs ran for this execution (can be empty if not ran yet or if
     * there was an error preventing all or few to run)
     * @throws FetchException on error while fetching the data (from file system or network errors, or any other input
     *                        source errors)
     */
    ExecutionTree getTree(long projectId, Build build) throws FetchException;

    /**
     * Complete build object passed in parameter (with data from files, api, ...)
     *
     * @param projectId the ID of the project in which to work
     * @param build     an execution or run job on continuous integration, to be completed by the method
     * @throws FetchException on error while fetching the data (from file system or network errors, or any other input
     *                        source errors)
     */
    default void completeBuildInformation(long projectId, Build build) throws FetchException {
    }

    /**
     * @param projectId the ID of the project in which to work
     * @param build     an execution job on continuous integration
     * @return the downloaded and parsed content of the artifact cycleDefinition.json of the job; empty if the server returned
     * 404 (not generated yet, or build crashed before generating it)
     * @throws FetchException on error while fetching the data (from file system or network errors, or any other input
     *                        source errors)
     */
    Optional<CycleDef> getCycleDefinition(long projectId, Build build) throws FetchException;

    /**
     * @param projectId the ID of the project in which to work
     * @param run       a given run job execution on continuous integration
     * @return the downloaded and parsed content of the Cucumber's artifact report.json of the job; empty if the server
     * returned 404 (not generated yet, or build crashed before generating it)
     * @throws FetchException on error while fetching the data (from file system or network errors, or any other input
     *                        source errors)
     */
    Optional<List<Feature>> getCucumberReport(long projectId, Run run) throws FetchException;

    /**
     * @param projectId the ID of the project in which to work
     * @param run       a given run job execution on continuous integration
     * @return the downloaded and parsed content of the artifact stepDefinitions.json of the job; empty if the server
     * returned 404 (not generated yet or at all, or build crashed before generating it). This data fetching is NOT
     * mandatory: when not provided, step definitions are faked
     * @throws FetchException on error while fetching the data (from file system or network errors, or any other input
     *                        source errors)
     */
    Optional<List<String>> getCucumberStepDefinitions(long projectId, Run run) throws FetchException;

    /**
     * @param projectId the ID of the project in which to work
     * @param run       a given run job execution on continuous integration
     * @return the downloaded list of artifact relative paths for the job; empty the job has no artifact (yet?)
     * @throws FetchException on error while fetching the data (from file system or network errors, or any other input
     *                        source errors)
     */
    List<String> getNewmanReportPaths(long projectId, Run run) throws FetchException;

    /**
     * Call StreamService to stream json data from different resources
     *
     * @param projectId        the ID of the project in which to work
     * @param run              a given run job execution on continuous integration
     * @param newmanReportPath relative path of the artifact to fetch
     * @param consumer         used to consume data during stream.
     * @throws FetchException on error while fetching the data (from file system or network errors, or any other input
     *                        source errors)
     */
    void streamNewmanResult(long projectId, Run run, String newmanReportPath, JsonParserConsumer consumer) throws FetchException;

    /**
     * Run after a DONE execution has been fully indexed and committed to the database.<br>
     * Can be used to clean-up resources used by running or currently-indexing executions...<br>
     * Note: a RUNNING execution can be indexed several times to live-update it in ARA: this method will only be called
     * after the execution is marked as DONE, and NOT between each indexing.<br>
     * Note: any change to the execution object will not be committed to database.
     *
     * @param projectId the ID of the project in which to work
     * @param execution the DONE indexed execution
     * @throws FetchException if clean-up failed for some reason (from file system or network errors, or any other input
     *                        source errors)
     */
    default void onDoneExecutionIndexingFinished(long projectId, Execution execution) throws FetchException {
    }

}
