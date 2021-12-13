package com.decathlon.ara.service.dto.setting;

import java.util.List;

import com.decathlon.ara.domain.enumeration.Technology;

public class TechnologySettingGroupDTO extends SettingGroupDTO {

    private Technology technology;

    public TechnologySettingGroupDTO() {
    }

    public TechnologySettingGroupDTO(String name, List<SettingDTO> settings, Technology technology) {
        super(name, settings);
        this.technology = technology;
    }

    public Technology getTechnology() {
        return technology;
    }

}
