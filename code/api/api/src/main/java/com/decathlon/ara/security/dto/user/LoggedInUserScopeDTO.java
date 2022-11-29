package com.decathlon.ara.security.dto.user;

public class LoggedInUserScopeDTO {

    private String project;

    private String role;

    public LoggedInUserScopeDTO(String project, String role) {
        this.project = project;
        this.role = role;
    }

    public String getProject() {
        return project;
    }

    public String getRole() {
        return role;
    }
}
