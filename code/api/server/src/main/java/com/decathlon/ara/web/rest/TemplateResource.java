package com.decathlon.ara.web.rest;

import com.decathlon.ara.service.ExecutionHistoryService;
import com.decathlon.ara.web.rest.util.RestConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping(TemplateResource.PATH)
@RequiredArgsConstructor
public class TemplateResource {
    private static final String NAME = "template";
    /**
     * The full path to this Rest resource from the basename.
     */
    static final String PATH = RestConstants.API_PATH + "/" + NAME + "s";

    private final ExecutionHistoryService executionHistoryService;

    @GetMapping("cycle-execution")
    public String nrtCycle(@RequestParam long projectId,
                           @RequestParam String branch,
                           @RequestParam String cycle,
                           Model model) {
        var latestExecutions = executionHistoryService.getLatestExecutionHistories(projectId);
        var execution = latestExecutions.stream().filter( e -> branch.equals(e.getBranch()) && cycle.equals(e.getName())).findFirst().get();
        model.addAttribute("execution", execution);
        return "mail/html/cycle-execution";
    }

}
