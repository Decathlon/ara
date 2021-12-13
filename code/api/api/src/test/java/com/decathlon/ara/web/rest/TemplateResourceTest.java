package com.decathlon.ara.web.rest;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import com.decathlon.ara.service.ExecutionHistoryService;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.dto.execution.ExecutionHistoryPointDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.util.TestUtil;

@ExtendWith(MockitoExtension.class)
class TemplateResourceTest {

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
    void mockData() throws NotFoundException {
        TestUtil.setField(executionHistory, "branch", "main");
        TestUtil.setField(executionHistory, "name", "day");
        model.addAttribute("execution", null);

        lenient().when(projectService.toId("test")).thenReturn(1L);
        lenient().when(executionHistoryService.getLatestExecutionHistories(1L)).thenReturn(List.of(executionHistory));
    }

    @Test
    void shouldBeNotFoundProject() {
        assertThrows(NotFoundException.class, () -> controller.nrtCycle("not-project", "main", "day", model));
    }

    @Test
    void shouldBeNotFoundExecution() {
        assertThrows(NotFoundException.class, () -> controller.nrtCycle("test", "develop", "day", model));
    }

    @Test
    void shouldBeFound() throws NotFoundException {
        controller.nrtCycle("test", "main", "day", model);
        assertEquals(model.getAttribute("execution"), executionHistory);
    }

}
