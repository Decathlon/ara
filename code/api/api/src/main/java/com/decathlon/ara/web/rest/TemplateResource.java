package com.decathlon.ara.web.rest;

import com.decathlon.ara.service.ExecutionHistoryService;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.exception.NotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.transaction.Transactional;

import static com.decathlon.ara.Entities.CYCLE_DEFINITION;
import static com.decathlon.ara.web.rest.ProjectResource.PROJECT_CODE_BASE_API_PATH;
import static com.decathlon.ara.web.rest.TemplateResource.TEMPLATE_BASE_API_PATH;

@Controller
@RequestMapping(TEMPLATE_BASE_API_PATH)
@Transactional(Transactional.TxType.REQUIRED)
public class TemplateResource {

    public static final String TEMPLATE_BASE_API_PATH = PROJECT_CODE_BASE_API_PATH + "/templates";
    public static final String TEMPLATE_ALL_API_PATHS = TEMPLATE_BASE_API_PATH + "/**";

    private final ExecutionHistoryService executionHistoryService;
    private final ProjectService projectService;

    public TemplateResource(ExecutionHistoryService executionHistoryService, ProjectService projectService) {
        this.executionHistoryService = executionHistoryService;
        this.projectService = projectService;
    }

    @GetMapping("/cycle-execution/branches/{branch}/cycles/{cycle}")
    public String nrtCycle(@PathVariable String projectCode,
                           @PathVariable String branch,
                           @PathVariable String cycle,
                           Model model) throws NotFoundException {
        var projectId = projectService.toId(projectCode);
        var latestExecutions = executionHistoryService.getLatestExecutionHistories(projectId);
        var execution = latestExecutions.stream()
                .filter(e -> branch.equals(e.getBranch()))
                .filter(e -> cycle.equals(e.getName()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("The branch and cycle couple for this project does not exists: it has perhaps been removed.", CYCLE_DEFINITION));
        model.addAttribute("execution", execution);
        return "cycle-execution";
    }

}
