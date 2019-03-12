package com.decathlon.ara.repository.custom;

import com.decathlon.ara.domain.Execution;
import java.util.Collection;
import java.util.List;

public interface ExecutionRepositoryCustom {

    List<Execution> findTop10ByProjectIdAndBranchAndNameOrderByTestDateTimeDesc(long projectId, String branch, String name);

    List<Execution> findLatestOfEachCycleByProjectId(long projectId);

    // NO projectId: referenceExecutions is already restrained to the correct project
    List<Execution> findNextOf(Collection<Execution> referenceExecutions);

    // NO projectId: referenceExecutions is already restrained to the correct project
    List<Execution> findPreviousOf(Collection<Execution> referenceExecutions);

    List<Execution> getLatestEligibleVersionsByProjectId(long projectId);

}
