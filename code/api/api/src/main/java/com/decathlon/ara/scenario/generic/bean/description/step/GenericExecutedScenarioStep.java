/******************************************************************************
 * Copyright (C) 2020 by the ARA Contributors                                 *
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

package com.decathlon.ara.scenario.generic.bean.description.step;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericExecutedScenarioStep {

    private Long line;

    private String status;

    private Long value;

    private String content;

    public Optional<Long> getOptionalValue() {
        return Optional.ofNullable(value);
    }

    /**
     * Get step line as string
     * @return get step line
     */
    public String getStepLine() {
        String valueAsString = getOptionalValue()
                .map(String::valueOf)
                .map(v -> ":" + v)
                .orElse("");
        return String.format("%d:%s%s:%s", line, status, valueAsString, content);
    }

    public Long getLine() {
        return line;
    }

    public String getStatus() {
        return status;
    }

    public Long getValue() {
        return value;
    }

    public String getContent() {
        return content;
    }
}
