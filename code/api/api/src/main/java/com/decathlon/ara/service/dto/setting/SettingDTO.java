/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

package com.decathlon.ara.service.dto.setting;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    private UnaryOperator<String> validate;

    /**
     * Called when a setting value has been changed by a user, to apply side-effects (clear a cache, update dependent
     * data, etc.).
     */
    @JsonIgnore
    private Consumer<String> applyChange;

    public List<SettingOptionDTO> getOptions() {
        return options;
    }

    public void setOptions(List<SettingOptionDTO> options) {
        this.options = options;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public UnaryOperator<String> getValidate() {
        return validate;
    }

    public void setValidate(UnaryOperator<String> validate) {
        this.validate = validate;
    }

    public Consumer<String> getApplyChange() {
        return applyChange;
    }

    public void setApplyChange(Consumer<String> applyChange) {
        this.applyChange = applyChange;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public SettingType getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public String getHelp() {
        return help;
    }

    public static class SettingDTOBuilder {
        private String code;
        private String name;
        private SettingType type;
        private List<SettingOptionDTO> options;
        private boolean required;
        private String help;
        private String defaultValue;
        private String value;
        UnaryOperator<String> validate;
        Consumer<String> applyChange;

        public SettingDTOBuilder withCode(String code) {
            this.code = code;
            return this;
        }

        public SettingDTOBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public SettingDTOBuilder withType(SettingType type) {
            this.type = type;
            return this;
        }

        public SettingDTOBuilder withOptions(List<SettingOptionDTO> options) {
            this.options = options;
            return this;
        }

        public SettingDTOBuilder withRequired(boolean required) {
            this.required = required;
            return this;
        }

        public SettingDTOBuilder withHelp(String help) {
            this.help = help;
            return this;
        }

        public SettingDTOBuilder withDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public SettingDTOBuilder withValue(String value) {
            this.value = value;
            return this;
        }

        public SettingDTOBuilder withValidate(UnaryOperator<String> validate) {
            this.validate = validate;
            return this;
        }

        public SettingDTOBuilder withApplyChange(Consumer<String> applyChange) {
            this.applyChange = applyChange;
            return this;
        }

        public SettingDTO build() {
            SettingDTO settings = new SettingDTO();
            settings.code = code;
            settings.name = name;
            settings.type = type;
            settings.options = options;
            settings.required = required;
            settings.help = help;
            settings.defaultValue = defaultValue;
            settings.value = value;
            settings.validate = validate;
            settings.applyChange = applyChange;
            return settings;
        }
    }

}
