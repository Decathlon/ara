package com.decathlon.ara.repository.custom.impl;

import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.repository.ProblemRepository;
import com.decathlon.ara.util.TransactionalSpringIntegrationTest;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static com.decathlon.ara.util.TestUtil.longs;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@TransactionalSpringIntegrationTest
public class ProblemRepositoryIT {

    @Autowired
    private ProblemRepository cut;

    @Test
    @DatabaseSetup({ "/dbunit/ProblemRepositoryIT-findAllByProjectIdAndDefectIdIsNotEmpty.xml" })
    public void findAllByProjectIdAndDefectIdIsNotEmpty_ShouldReturnProblemsWithNonEmptyDefectId_WhenCalledForAProject() {
        // GIVEN
        final long projectId = 1;

        // WHEN
        final List<Problem> problems = cut.findAllByProjectIdAndDefectIdIsNotEmpty(projectId);

        // THEN
        assertThat(problems.stream().map(Problem::getId)).containsExactly(longs(2, 3));
    }

}
