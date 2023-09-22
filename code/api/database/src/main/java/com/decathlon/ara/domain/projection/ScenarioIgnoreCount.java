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

package com.decathlon.ara.domain.projection;

import com.decathlon.ara.domain.Source;

/**
 * This object holds key+value data: it holds the count (the value) of scenarios for a given triple [ sourceCode, severityCode, ignoredOrNot ] "key".
 */
public class ScenarioIgnoreCount {

    /**
     * Key-part: the source of the counted scenarios.
     */
    private Source source;

    /**
     * Key-part: the severity of the counted scenarios.
     */
    private String severityCode;

    /**
     * Key-part: counting ignored (true) or not ignored (false) scenarios.
     */
    private boolean ignored;

    /**
     * Value-part: the count of scenarios matching the key-part criteria.
     */
    private long count;

    public ScenarioIgnoreCount() {
    }

    public ScenarioIgnoreCount(Source source, String severityCode, boolean ignored, long count) {
        this.source = source;
        this.severityCode = severityCode;
        this.ignored = ignored;
        this.count = count;
    }

    public Source getSource() {
        return source;
    }

    public String getSeverityCode() {
        return severityCode;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public long getCount() {
        return count;
    }

}
