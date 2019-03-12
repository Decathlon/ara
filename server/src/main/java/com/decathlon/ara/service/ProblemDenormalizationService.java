package com.decathlon.ara.service;

import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.domain.projection.FirstAndLastProblemOccurrence;
import com.decathlon.ara.repository.ProblemRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for updating de-normalized fields of Problem.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProblemDenormalizationService {

    @NonNull
    private final ProblemRepository problemRepository;

    public void updateFirstAndLastSeenDateTimes(Collection<Problem> problems) {
        final List<FirstAndLastProblemOccurrence> occurrences =
                problemRepository.findFirstAndLastProblemOccurrences(problems);

        for (Problem problem : problems) {
            final Optional<FirstAndLastProblemOccurrence> occurrence = occurrences.stream()
                    .filter(o -> problem.getId().equals(o.getProblemId()))
                    .findFirst();
            problem.setFirstSeenDateTime(occurrence
                    .map(FirstAndLastProblemOccurrence::getFirstSeenDateTime)
                    .orElse(null));
            problem.setLastSeenDateTime(occurrence
                    .map(FirstAndLastProblemOccurrence::getLastSeenDateTime)
                    .orElse(null));
        }
    }

}
