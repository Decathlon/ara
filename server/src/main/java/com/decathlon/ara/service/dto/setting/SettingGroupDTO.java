package com.decathlon.ara.service.dto.setting;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class SettingGroupDTO {

    /**
     * User-visible name of the group of settings to display in the GUI.
     */
    private String name;

    /**
     * The list of settings for the current project and group.
     */
    private List<SettingDTO> settings;

}
