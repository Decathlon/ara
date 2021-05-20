package com.decathlon.ara.web.rest;

import com.decathlon.ara.service.ExecutionHistoryService;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.dto.execution.ExecutionHistoryPointDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TemplateResourceTest {

    private final ExecutionHistoryPointDTO executionHistory = new ExecutionHistoryPointDTO();
    @Mock
    private ExecutionHistoryService executionHistoryService;
    @Mock
    private ProjectService projectService;
    @InjectMocks
    private TemplateResource controller;
    @Spy
    private ExtendedModelMap model;

    @BeforeEach
    public void mockData() throws NotFoundException {
        executionHistory.setBranch("main");
        executionHistory.setName("day");
        model.addAttribute("execution", null);

        lenient().when(projectService.toId("test")).thenReturn(1L);
        lenient().when(executionHistoryService.getLatestExecutionHistories(1L)).thenReturn(List.of(executionHistory));
    }

    @Test
    public void shouldBeNotFoundProject() {
        assertThrows(NotFoundException.class, () -> controller.nrtCycle("not-project", "main", "day", model));
    }

    @Test
    public void shouldBeNotFoundExecution() {
        assertThrows(NotFoundException.class, () -> controller.nrtCycle("test", "develop", "day", model));
    }

    @Test
    public void shouldBeFound() throws NotFoundException {
        controller.nrtCycle("test", "main", "day", model);
        assertEquals(model.getAttribute("execution"), executionHistory);
    }

}
