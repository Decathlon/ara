package com.decathlon.ara.v2.controller;

import com.decathlon.ara.v2.domain.Project;
import com.decathlon.ara.v2.exception.AraException;
import com.decathlon.ara.v2.service.migration.V2ProjectMigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/migration")
public class MigrationController {

    private final V2ProjectMigrationService migrationService;

    @PutMapping("project")
    public Project migrateSingleProjectByCode(
            @RequestParam("code") String code,
            @RequestParam("execution-start-date") @DateTimeFormat(pattern = "dd/MM/yyyy") Date executionStartDate
    ) throws AraException {
        return migrationService.migrateProject(code, Optional.ofNullable(executionStartDate));
    }

}
