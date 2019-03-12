package com.decathlon.ara.service;

import com.decathlon.ara.loader.DemoExecutionLoader;
import com.decathlon.ara.loader.DemoFunctionalityLoader;
import com.decathlon.ara.loader.DemoProblemLoader;
import com.decathlon.ara.loader.DemoScenarioLoader;
import com.decathlon.ara.loader.DemoSettingsLoader;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.service.dto.project.ProjectDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static com.decathlon.ara.loader.DemoLoaderConstants.PROJECT_CODE_DEMO;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DemoServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private SettingService settingService;

    @Mock
    private DemoExecutionLoader demoExecutionLoader;

    @Mock
    private DemoFunctionalityLoader demoFunctionalityLoader;

    @Mock
    private DemoProblemLoader demoProblemLoader;

    @Mock
    private DemoScenarioLoader demoScenarioLoader;

    @Mock
    private DemoSettingsLoader demoSettingsLoader;

    @InjectMocks
    private DemoService cut;

    @Test(expected = BadRequestException.class)
    public void create_ShouldFail_WhenDemoProjectAlreadyExists() throws BadRequestException {
        // GIVEN
        when(projectService.findOne(PROJECT_CODE_DEMO)).thenReturn(Optional.of(new ProjectDTO()));

        // WHEN
        cut.create();
    }

    @Test(expected = NotFoundException.class)
    public void delete_ShouldFail_WhenDemoProjectDoesNotExist() throws NotFoundException {
        // GIVEN
        when(projectRepository.findOneByCode(PROJECT_CODE_DEMO)).thenReturn(null);

        // WHEN
        cut.delete();
    }

}
