package com.decathlon.ara.loader;

import com.decathlon.ara.repository.RootCauseRepository;
import com.decathlon.ara.service.ProblemService;
import com.decathlon.ara.service.dto.problem.ProblemWithPatternsDTO;
import com.decathlon.ara.service.dto.problempattern.ProblemPatternDTO;
import com.decathlon.ara.service.dto.rootcause.RootCauseDTO;
import com.decathlon.ara.service.dto.team.TeamDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for loading problems into the Demo project.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DemoProblemLoader {

    @NonNull
    private final ProblemService problemService;

    @NonNull
    private final RootCauseRepository rootCauseRepository;

    public void createProblems(long projectId, List<TeamDTO> teams) throws BadRequestException {
        final Long rootCauseId = rootCauseRepository.findByProjectIdAndName(projectId, "Fragile test").getId();

        problemService.create(projectId, new ProblemWithPatternsDTO(
                "Cannot add several products to cart",
                "Whatever the number of products added to cart, only one remains.",
                teams.get(2), // Buy
                "42",
                new RootCauseDTO().withId(rootCauseId),
                Collections.singletonList(
                        new ProblemPatternDTO()
                                .withStepDefinition("^the cart page shows (\\d+) product[s]?$")
                                .withException("java.lang.AssertionError: expected:<[%]> but was:<[1]>"))));
    }

}
