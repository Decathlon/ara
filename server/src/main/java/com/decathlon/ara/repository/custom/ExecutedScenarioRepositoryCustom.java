package com.decathlon.ara.repository.custom;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.projection.ExecutedScenarioWithErrorAndProblemJoin;
import java.util.List;
import java.util.Set;

public interface ExecutedScenarioRepositoryCustom {

    List<ExecutedScenario> findHistory(long projectId, String cucumberId, String branch, String cycleName, String countryCode, String runTypeCode);

    /**
     * @param runIds the IDs of the Runs where to find ExecutedScenarios
     * @return all executed-scenario of the runs, with minimal information (id, runId, name, severity) and count of errors and problem-patterns
     */
    // NO projectId: runIds is already restrained to the correct project
    List<ExecutedScenarioWithErrorAndProblemJoin> findAllErrorAndProblemCounts(Set<Long> runIds);

}
