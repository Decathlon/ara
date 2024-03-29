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

package com.decathlon.ara.domain.enumeration;

public enum CoverageLevel {

    COVERED("Covered (no ignored)", "Functionalities having active scenarios, none of them are ignored"),
    PARTIALLY_COVERED("Partially covered (few ignored)", "Functionalities having active scenarios as well as ignored scenarios"),
    IGNORED_COVERAGE("Ignored coverage (all ignored)", "Functionalities having ignored scenarios and no active scenarios"),
    STARTED("Started", "Functionalities marked as 'Started' without any active nor ignored scenario"),
    NOT_AUTOMATABLE("Not automatable", "Functionalities marked as 'Not automatable' without any active nor ignored scenario"),
    NOT_COVERED("Not covered", "Functionalities that are not started nor not-automatable and have no active nor ignored scenarios");

    private final String label;
    private final String tooltip;

    CoverageLevel(String label, String tooltip) {
        this.label = label;
        this.tooltip = tooltip;
    }

    public String getLabel() {
        return label;
    }

    public String getTooltip() {
        return tooltip;
    }

}
