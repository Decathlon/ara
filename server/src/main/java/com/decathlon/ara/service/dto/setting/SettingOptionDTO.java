package com.decathlon.ara.service.dto.setting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class SettingOptionDTO {

    /**
     * Technical value to save in database for the option.
     */
    private String value;

    /**
     * User-visible text value to show in the GUI for the option.
     */
    private String label;

}
