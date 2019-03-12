package com.decathlon.ara.service.dto.setting;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class SettingDTO {

    /**
     * The technical code of the setting, to be saved in database and used by server code.
     */
    private String code;

    /**
     * The user-visible name of the setting to be displayed in GUI.
     */
    private String name;

    /**
     * The data-type of the setting, defining how to edit in in the GUI.
     */
    private SettingType type;

    /**
     * If {@link #type} is {@link SettingType#SELECT}, defines the list of allowed options in the select drop-down list.
     */
    private List<SettingOptionDTO> options;

    /**
     * True if the setting is required for the application to work.
     */
    private boolean required;

    /**
     * A small user-visible help message to inform user what this setting is for and how to set it.
     */
    private String help;

    /**
     * The value to use when the setting is not configured in database.
     */
    private String defaultValue;

    /**
     * The actual value of the setting for the requested project.
     */
    private String value;

    /**
     * Used when editing the setting, to validate the user entered data fulfilling the setting's business rules. Returns
     * null if valid, and the error message when not valid. When {@link #type} is {@link SettingType#SELECT}, the value
     * is automatically compared to allowed values: no need to provide a validator. Format is also checked for
     * {@link SettingType#BOOLEAN} and {@link SettingType#INT}. Such validations are done BEFORE calling validate, so
     * you can be sure the data format is correct at the time validate is called: you can focus on additional business
     * validation.
     */
    @JsonIgnore
    private Function<String, String> validate;

    /**
     * Called when a setting value has been changed by a user, to apply side-effects (clear a cache, update dependent
     * data, etc.).
     */
    @JsonIgnore
    private Consumer<String> applyChange;

}
