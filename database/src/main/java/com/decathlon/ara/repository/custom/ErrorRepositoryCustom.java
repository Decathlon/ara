package com.decathlon.ara.repository.custom;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.ProblemPattern;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ErrorRepositoryCustom {

    Page<Error> findMatchingErrors(long projectId, ProblemPattern pattern, Pageable pageable);

    void assignPatternToErrors(long projectId, ProblemPattern pattern);

    // NO projectId: errors is already restrained to the correct project
    Map<Error, List<Problem>> getErrorsProblems(Collection<Error> errors);

    /**
     * When new errors get indexed into ARA, this method will assign them existing problems if at least one of the
     * problems's patterns match the errors.
     *
     * @param projectId the ID of the project in which to work
     * @param errorIds  the IDs of the new errors that were just created
     * @return all problems that were assigned one of the given new errors by this method
     */
    Set<Problem> autoAssignProblemsToNewErrors(long projectId, List<Long> errorIds);

}
