package com.decathlon.ara.service.dto.auditing;

import java.util.ArrayList;
import java.util.List;

import com.decathlon.ara.domain.enumeration.UserSecurityRole;

public class UserRoleDetails {
    
    private String userName;
    private List<UserSecurityRole> roles;
    private List<ProjectRoleDetails> projects;
    
    public UserRoleDetails(String userName) {
        this.userName = userName;
        roles = new ArrayList<>();
        projects = new ArrayList<>();
    }
    
    public ProjectRoleDetails getProject(String code) {
        for (ProjectRoleDetails project : projects) {
            if (code.equals(project.getCode())){
                return project;
            }
        }
        ProjectRoleDetails newProject = new ProjectRoleDetails(code);
        projects.add(newProject);
        return newProject;
    }
    
    public void addRoles(UserSecurityRole role) {
        roles.add(role);
    }
    
    public String getUserName() {
        return userName;
    }
    
    public List<UserSecurityRole> getRoles() {
        return roles;
    }
    
    public List<ProjectRoleDetails> getProjects() {
        return projects;
    }

}
