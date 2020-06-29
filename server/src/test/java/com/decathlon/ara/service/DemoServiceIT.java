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

package com.decathlon.ara.service;

import com.decathlon.ara.defect.DefectAdapter;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.repository.*;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.support.Settings;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static com.decathlon.ara.loader.DemoLoaderConstants.BRANCH_MASTER;
import static com.decathlon.ara.loader.DemoLoaderConstants.PROJECT_CODE_DEMO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

// FIXME : Should be replaced by Selenium tests on a dedicated infra.
//@RunWith(SpringRunner.class)
//@TransactionalSpringIntegrationTest
public class DemoServiceIT {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CommunicationRepository communicationRepository;

    @Autowired
    private RootCauseRepository rootCauseRepository;

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private TypeRepository typeRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private SeverityRepository severityRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private FunctionalityRepository functionalityRepository;

    @Autowired
    private CycleDefinitionRepository cycleDefinitionRepository;

    @SpyBean
    private SettingService settingService;

    @MockBean
    private ApplicationContext applicationContext;

    @Autowired
    private DemoService cut;

    /**
     * Project creation is mostly about inserting stuff in database, so unit testing is pointless for these parts.<br>
     * Here is one big integration test to roughly check data is inserted and new constraints are respected, instead of
     * several integration tests, because this creation is time consuming and creates temporary folders.
     *
     * @throws BadRequestException when something goes wrong with the method under test
     * @throws IOException         when somethings goes wrong with temporary directory used for this test
     */
    // @Test
    public void create_ShouldPopulateTablesAndCreateExecutionFiles_WhenCreatingAProject()
            throws BadRequestException, IOException {
        // GIVEN
        final Path tempDirectory = Files.createTempDirectory("ara_temp_integration_test_directory_");
        try {
            doReturn(tempDirectory.toString() + "/" + Settings.DEFAULT_EXECUTION_VARIABLES)
                    .when(settingService).get(anyLong(), eq(Settings.EXECUTION_INDEXER_FILE_EXECUTION_BASE_PATH));
            doReturn(Collections.emptyMap()).when(applicationContext).getBeansOfType(DefectAdapter.class);

            // WHEN
            final ProjectDTO createdProject = cut.create();

            // THEN
            assertThat(createdProject.getId()).isNotNull();
            assertThatDemoProjectHasBeenInserted(createdProject);
            assertThatAllProjectDataAreInitialized(createdProject.getId());
        } finally {
            FileUtils.deleteQuietly(tempDirectory.toFile());
        }
    }

    // @Test
    //@DatabaseSetup("/dbunit/DemoServiceIT-delete.xml")
    public void delete_ShouldDeleteProjectAndFiles_WhenCalled() throws NotFoundException, IOException {
        // GIVEN
        final Path tempDirectory = Files.createTempDirectory("ara_temp_integration_test_directory_");
        try {
            doReturn(tempDirectory.toString() + "/executions/" + Settings.DEFAULT_EXECUTION_VARIABLES)
                    .when(settingService).get(anyLong(), eq(Settings.EXECUTION_INDEXER_FILE_EXECUTION_BASE_PATH));
            Path projectFolder = tempDirectory.resolve("executions/" + PROJECT_CODE_DEMO);
            Path folder = projectFolder.resolve("master/day/1525442442556/fr/firefox-desktop");
            Files.createDirectories(folder);
            Files.createFile(folder.resolve("buildInformation.json"));

            // WHEN
            cut.delete();

            // THEN
            assertThat(projectRepository.findOneByCode(PROJECT_CODE_DEMO)).isNull();
            assertThat(Files.exists(projectFolder)).as("Folder has been removed").isFalse();
        } finally {
            FileUtils.deleteQuietly(tempDirectory.toFile());
        }
    }

    private void assertThatDemoProjectHasBeenInserted(ProjectDTO createdProject) {
        final Project project = projectRepository.findOneByCode(PROJECT_CODE_DEMO);
        assertThat(project.getId()).isEqualTo(createdProject.getId());
    }

    private void assertThatAllProjectDataAreInitialized(long projectId) {
        assertThat(communicationRepository.findAllByProjectIdOrderByCode(projectId)).isNotEmpty();
        assertThat(rootCauseRepository.findAllByProjectIdOrderByName(projectId)).isNotEmpty();
        assertThat(settingRepository.findAll()).isNotEmpty();
        assertThat(sourceRepository.findAllByProjectIdOrderByName(projectId)).isNotEmpty();
        assertThat(typeRepository.findAllByProjectIdOrderByCode(projectId)).isNotEmpty();
        assertThat(countryRepository.findAllByProjectIdOrderByCode(projectId)).isNotEmpty();
        assertThat(severityRepository.findAllByProjectIdOrderByPosition(projectId)).isNotEmpty();
        assertThat(problemRepository.findByProjectId(projectId)).isNotEmpty();
        assertThat(functionalityRepository.findAllByProjectIdOrderByOrder(projectId)).isNotEmpty();
        assertThat(cycleDefinitionRepository.findAllByProjectIdAndBranch(projectId, BRANCH_MASTER)).isNotEmpty();
    }
}
