package com.decathlon.ara.web.rest.audits;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.decathlon.ara.service.auditing.AuditingService;
import com.decathlon.ara.service.dto.auditing.UserRoleDetails;
import com.decathlon.ara.web.rest.util.RestConstants;

@RestController
@RequestMapping(AuditingResource.PATH)
public class AuditingResource {
    
    static final String PATH = RestConstants.API_PATH + "/auditing";
    
    private AuditingService auditingService;
    
    public AuditingResource(AuditingService auditsService) {
        this.auditingService = auditsService;
    }

    @GetMapping("/users-roles")
    public List<UserRoleDetails> auditUserRole(){
        return auditingService.auditUsersRoles();
    }

}
