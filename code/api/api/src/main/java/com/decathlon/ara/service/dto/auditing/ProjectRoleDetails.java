package com.decathlon.ara.service.dto.auditing;

import java.util.ArrayList;
import java.util.List;

import com.decathlon.ara.domain.enumeration.MemberRole;

public class ProjectRoleDetails {
    
    private String code;
    private List<MemberRoleDetails> roles;
    
    public ProjectRoleDetails(String code) {
        this.code = code;
        this.roles = new ArrayList<>();
    }
    
    public void addRole(MemberRole role, String inheritedFrom) {
        roles.add(new MemberRoleDetails(role, inheritedFrom));
    }
    
    public String getCode() {
        return code;
    }
    
    public List<MemberRoleDetails> getRoles() {
        return roles;
    }

}
