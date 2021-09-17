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

package com.decathlon.ara.repository.custom.impl;

import static com.decathlon.ara.util.TestUtil.longs;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.decathlon.ara.domain.Problem;
import com.decathlon.ara.repository.ProblemRepository;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@SpringBootTest
@TestExecutionListeners({
    TransactionalTestExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DbUnitTestExecutionListener.class
})
@TestPropertySource(properties = {
        "ara.database.target=h2"
})
@Transactional
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
