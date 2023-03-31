package com.decathlon.ara.service.dto.member;

import javax.validation.constraints.NotNull;

import com.decathlon.ara.domain.enumeration.MemberRole;

public class MemberDTO {

    @NotNull(message = "name is mandatory")
    private String name;
    @NotNull(message = "role is mandatory")
    private MemberRole role;

    public MemberDTO() {
    }

    public MemberDTO(String name, MemberRole role) {
        this.name = name;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public MemberRole getRole() {
        return role;
    }

}
