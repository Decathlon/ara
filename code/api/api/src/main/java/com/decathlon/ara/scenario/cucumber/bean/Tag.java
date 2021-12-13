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

package com.decathlon.ara.scenario.cucumber.bean;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Tag {

    private static final Logger LOG = LoggerFactory.getLogger(Tag.class);

    public static final String IGNORE = "@ignore";
    public static final String SEVERITY_PREFIX = "@severity-";
    public static final String COUNTRY_ALL = "all";
    private static final String COUNTRY_PREFIX = "@country-";

    private Integer line;
    private String name;

    /**
     * @param tags raw tags from parsed Cucumber's report.json
     * @return non-duplicate set of tag names, in alphabetical order
     */
    public static Set<String> names(Tag[] tags) {
        // TreeSet to sort alphabetically
        return Arrays.stream(tags).map(Tag::getName).collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * @param tags tag names, starting with "@"
     * @return country-codes from all country tags (without duplicate, and by alphabetical order)
     */
    public static Set<String> extractCountryCodes(Collection<String> tags) {
        Set<String> countryCodes = new TreeSet<>(); // TreeSet to sort alphabetically
        for (String tag : tags) {
            if (tag.startsWith(Tag.COUNTRY_PREFIX)) {
                countryCodes.add(tag.substring(Tag.COUNTRY_PREFIX.length())); // Without "@country-"
            }
        }

        // If "all" is present, other are redundant
        if (countryCodes.contains(COUNTRY_ALL)) {
            countryCodes.clear();
            countryCodes.add(COUNTRY_ALL);
        }

        return countryCodes;
    }

    /**
     * @param tags         tag names, starting with "@"
     * @param scenarioName the name of the scenario, to put in the logs if there are several different severities
     * @return the "@severity-..." tag assigned to the scenario (without this tag prefix), or "" if no one assigned (never null)
     */
    public static String extractSeverity(Collection<String> tags, String scenarioName) {
        // Find all @severity-... tags assigned to the scenario
        final Set<String> severities = new TreeSet<>(); // TreeSet to sort alphabetically
        for (String tag : tags) {
            if (tag.startsWith(SEVERITY_PREFIX)) {
                severities.add(tag.substring(SEVERITY_PREFIX.length())); // Without "@severity-"
            }
        }

        // A scenario have at most one severity
        if (severities.size() > 1) {
            LOG.error("Scenario \"{}\" has several severities: {}: only one will be used (in a non-deterministic way)", scenarioName, severities);
        }

        // No severity assigned to the scenario
        if (severities.isEmpty()) {
            return "";
        }

        // Return ONE severity (pseudo-randomly, depending on the implementation of the Set)
        return severities.iterator().next();
    }

    public Integer getLine() {
        return line;
    }

    public String getName() {
        return name;
    }

}
