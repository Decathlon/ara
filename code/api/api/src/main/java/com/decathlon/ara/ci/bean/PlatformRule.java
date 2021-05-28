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

package com.decathlon.ara.ci.bean;

import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class PlatformRule {

    public static final String TEST_TYPES_SEPARATOR = ",";

    /**
     * True if the quality of the severities of this country should be counted in the global NRT quality thresholds of the cycle execution.
     */
    private boolean blockingValidation;

    /**
     * Two upper-cased letters representing the country to deploy and test.
     *
     * @see Country#getCode()
     */
    private String country;

    /**
     * Comma-separated {@link Country#getCode()} list of @country-* Cucumber tags to run.<br>
     * Can contains "all" to include @country-all core scenarios.<br>
     * Eg. "be,cn" for BE+CN specific scenarios, or "all,be" for core+BE scenarios.
     */
    private String countryTags;

    /**
     * Comma-separated {@link Type#getCode()} list.<br>
     * Eg. "api,firefox".
     *
     * @see #TEST_TYPES_SEPARATOR the separator used to join test-types together
     */
    private String testTypes;

    /**
     * Eg. "all" or "sanity-check,high".
     */
    private String severityTags;

    /**
     * True to enable this country, false to ignore it (no deployment, no NRT).
     */
    private boolean enabled;

}
