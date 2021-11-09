package com.decathlon.ara.service.dto.user;

import javax.validation.constraints.NotNull;

import com.decathlon.ara.domain.enumeration.UserSecurityRole;

public class RoleDTO {

    @NotNull(message = "role is mandatory")
    private UserSecurityRole role;

    public RoleDTO() {
    }

    public RoleDTO(UserSecurityRole role) {
        this.role = role;
    }

    public UserSecurityRole getRole() {
        return role;
    }
}
