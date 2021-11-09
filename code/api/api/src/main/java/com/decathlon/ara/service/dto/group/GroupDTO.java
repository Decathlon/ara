package com.decathlon.ara.service.dto.group;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class GroupDTO {

    @NotNull(message = "name is mandatory")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Only alphanumeric characters, dash and underscore are allowed in name")
    private String name;

    public GroupDTO() {
    }

    public GroupDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
