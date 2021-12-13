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

package com.decathlon.ara.scenario.generic.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.decathlon.ara.scenario.generic.bean.description.GenericExecutedScenarioDescription;
import com.decathlon.ara.scenario.generic.bean.display.GenericExecutedScenarioResultsDisplay;
import com.decathlon.ara.scenario.generic.bean.error.GenericExecutedScenarioError;
import com.decathlon.ara.scenario.generic.bean.feature.GenericExecutedScenarioFeature;
import com.decathlon.ara.scenario.generic.bean.log.GenericExecutedScenarioLogs;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericExecutedScenarioReport {

    private String code;

    private String name;

    private GenericExecutedScenarioFeature feature;

    private GenericExecutedScenarioDescription description;

    @JsonProperty("start")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date startDate;

    private boolean ignored;

    private List<GenericExecutedScenarioError> errors;

    private List<Long> cartography;

    private GenericExecutedScenarioResultsDisplay display;

    private GenericExecutedScenarioLogs logs;

    private List<String> tags;

    private String severity;

    @JsonProperty("server")
    private String serverName;

    private String comment;

    /**
     * Get functionalities name
     * @return the functionalities name
     */
    public String getFunctionalitiesName() {
        String cartographyAsString = "";
        if (!CollectionUtils.isEmpty(cartography)) {
            String cartographyIdsAsString = cartography
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            cartographyAsString = String.format("Functionality %s", cartographyIdsAsString);
        }
        String separator = "";
        if (StringUtils.isNotBlank(cartographyAsString) && StringUtils.isNotBlank(name)) {
            separator = ": ";
        }
        return String.format("%s%s%s", cartographyAsString, separator, StringUtils.isBlank(name) ? "" : name);
    }

    /**
     * Convert tags into a string. If no tags, then return an empty string
     * @param tags the tags to convert
     * @return the string representation of the tags
     */
    public static String convertTagsToString(List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return "";
        }
        return tags
                .stream()
                .map(tag -> String.format("@%s", tag))
                .collect(Collectors.joining(" "));
    }

    /**
     * Get tags representation as string
     * @return tags representation as string
     */
    public String getTagsAsString() {
        return convertTagsToString(tags);
    }

    /**
     * Get country codes as string from tags and feature tags
     * @return country codes as string
     */
    public String getCountryCodesAsString() {
        List<String> countryCodes = getCountryCodes();
        if (CollectionUtils.isEmpty(countryCodes)) {
            return "all";
        }
        return countryCodes.stream().collect(Collectors.joining(","));
    }

    /**
     * Get country codes from tags and feature tags
     * @return country codes
     */
    public List<String> getCountryCodes() {
        List<String> countryCodesFromTags = getCountryCodesFromTags(tags);
        List<String> countryCodesFromFeatureTags = feature != null ? getCountryCodesFromTags(feature.getTags()) : new ArrayList<>();
        return Stream.of(countryCodesFromTags, countryCodesFromFeatureTags)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Get country codes from tags
     * @param tags the tags to get the country codes from
     * @return country codes
     */
    private static List<String> getCountryCodesFromTags(List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return new ArrayList<>();
        }
        return tags.stream()
                .filter(tag -> tag.startsWith("country-"))
                .map(tag -> tag.substring("country-".length()))
                .distinct()
                .collect(Collectors.toList());
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public GenericExecutedScenarioFeature getFeature() {
        return feature;
    }

    public GenericExecutedScenarioDescription getDescription() {
        return description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public List<GenericExecutedScenarioError> getErrors() {
        return errors;
    }

    public List<Long> getCartography() {
        return cartography;
    }

    public GenericExecutedScenarioResultsDisplay getDisplay() {
        return display;
    }

    public GenericExecutedScenarioLogs getLogs() {
        return logs;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getSeverity() {
        return severity;
    }

    public String getServerName() {
        return serverName;
    }

    public String getComment() {
        return comment;
    }
}
