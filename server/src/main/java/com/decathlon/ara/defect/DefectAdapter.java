package com.decathlon.ara.defect;

import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.service.dto.setting.SettingDTO;
import com.decathlon.ara.defect.bean.Defect;
import java.util.Date;
import java.util.List;

/**
 * The link with the defect tracking system used by the project.<br>
 * Tracked defects are assigned to problems to follow their resolution.<br>
 * Each project needs to re-implement this interface to link with
 * their specific defect tracking system (JIRA, Mantis, Redmine, Bugzilla...).
 */
public interface DefectAdapter {

    /**
     * Called the first time defects get queried or if a long period has passed from last incremental indexation.<br>
     * The parameter will only contain defects assigned to ARA problems, to reduce initialisation workload.<br>
     * It is up to the implementation to query them in bulk or one by one.
     *
     * @param projectId the ID of the project in which to work
     * @param ids       all IDs of defects/issues/bugs/... to query in the external issue-tracker
     * @return the statuses of the requested defects: a requested defect missing from the returned list or having a null
     * status is considered nonexistent and an error will be shown to users (can be empty but NEVER null)
     * @throws FetchException on any network issue, wrong HTTP response status code or parsing issue
     */
    List<Defect> getStatuses(long projectId, List<String> ids) throws FetchException;

    /**
     * Called at regular interval for incremental indexation of defects.<br>
     * Return all defects that were modified since the given date:<br>
     * ARA will update concerned problems and ignore defects that are not assigned to any problem.
     *
     * @param projectId the ID of the project in which to work
     * @param since     the date from which to get updated defects
     * @return all defects that were modified since the given date (can be empty but NEVER null)
     * @throws FetchException on any network issue, wrong HTTP response status code or parsing issue
     */
    List<Defect> getChangedDefects(long projectId, Date since) throws FetchException;

    /**
     * Validate a user input for a defect ID in the tracker.
     *
     * @param projectId the ID of the project in which to work
     * @param id        a user-typed defect ID
     * @return true if it is a valid ID for the backed defect tracking system
     */
    boolean isValidId(long projectId, String id);

    /**
     * Get the guiding help text to display to users when they type a wrongly-formatted defect ID.
     *
     * @param projectId the ID of the project in which to work
     * @return the message to be inserted in a sentence (no capital-case, no end-point: eg. "must be a positive number")
     */
    String getIdFormatHint(long projectId);

    /**
     * @return the code to uniquely identify this defect adapter class (stored in the project settings in database)
     */
    String getCode();

    /**
     * @return the name of the defect tracking system this adapter implements (eg. "JIRA", "Mantis", "Redmine"...),
     * to be used in settings and error message while refreshing defect status
     */
    String getName();

    /**
     * @return the list of settings to configure this fetcher for a given project
     */
    List<SettingDTO> getSettingDefinitions();

}
