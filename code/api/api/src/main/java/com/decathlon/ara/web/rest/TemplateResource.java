package com.decathlon.ara.web.rest;

import com.decathlon.ara.Entities;
import com.decathlon.ara.service.ExecutionHistoryService;
import com.decathlon.ara.service.ProjectService;
import com.decathlon.ara.service.exception.NotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.transaction.Transactional;

import static com.decathlon.ara.web.rest.util.RestConstants.PROJECT_API_PATH;

@Controller
@RequestMapping(TemplateResource.PATH)
@Transactional(Transactional.TxType.REQUIRED)
public class TemplateResource {
    private static final String NAME = "template";
    /**
     * The full path to this Rest resource from the basename.
     */
    static final String PATH = PROJECT_API_PATH + "/" + NAME + "s";
    public static final String PATHS = PATH + "/**";

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
                .orElseThrow(() -> new NotFoundException("The branch and cycle couple for this project does not exists: it has perhaps been removed.", Entities.CYCLE_DEFINITION));
        model.addAttribute("execution", execution);
        return "cycle-execution";
    }

}
