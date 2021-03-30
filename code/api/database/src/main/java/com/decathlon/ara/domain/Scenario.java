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

package com.decathlon.ara.domain;


import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static java.util.Comparator.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
@Entity
// Keep business key in sync with compareTo(): see https://developer.jboss.org/wiki/EqualsAndHashCode
@EqualsAndHashCode(of = { "source", "featureFile", "name", "line" })
public class Scenario implements Comparable<Scenario>, Serializable {

    public static final String COUNTRY_CODES_SEPARATOR = ",";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scenario_id")
    @SequenceGenerator(name = "scenario_id", sequenceName = "scenario_id", allocationSize = 1)
    private Long id;

    /**
     * The version-control-system where the {@link #featureFile} is stored, as well as the technology used by this file.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Source source;

    private String featureFile;

    private String featureName;

    private String featureTags;

    private String tags;

    private boolean ignored;

    /**
     * The country code of all the countries where this scenario is configured to run.<br>
     * Can be COUNTRY_ALL or one or more codes, separated by commas.<br>
     * Eg. "all" or "fr,us"...<br><br>
     * This is not a strict foreign-key association because developers can commit typos and we want to surface these.
     * Used to show the functionality coverage per country.
     *
     * @see #COUNTRY_CODES_SEPARATOR the separator used to join country-codes together
     */
    @Column(length = 128)
    private String countryCodes;

    /**
     * The severity code of the scenario, as stated in Version Control System.<br>
     * This is not a strict foreign-key association because developers can commit typos and we want to surface these.
     */
    @Column(length = 32)
    private String severity;

    @Column(length = 512)
    private String name;

    private String wrongFunctionalityIds;

    private String wrongCountryCodes;

    private String wrongSeverityCode;

    private int line;

    @Lob
    @org.hibernate.annotations.Type(type = "org.hibernate.type.TextType")
    private String content;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "scenarios")
    private Set<Functionality> functionalities = new HashSet<>();

    public void addFunctionality(Functionality functionality) {
        this.functionalities.add(functionality);
        functionality.getScenarios().add(this);
    }

    public void removeFunctionality(Functionality functionality) {
        this.functionalities.remove(functionality);
        functionality.getScenarios().remove(this);
    }

    @Override
    public int compareTo(Scenario other) {
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<Scenario> sourceComparator = comparing(Scenario::getSource, nullsFirst(naturalOrder()));
        Comparator<Scenario> featureFileComparator = comparing(Scenario::getFeatureFile, nullsFirst(naturalOrder()));
        Comparator<Scenario> nameComparator = comparing(Scenario::getName, nullsFirst(naturalOrder()));
        Comparator<Scenario> lineComparator = comparing(s -> Long.valueOf(s.getLine()), nullsFirst(naturalOrder()));
        return nullsFirst(sourceComparator
                .thenComparing(featureFileComparator)
                .thenComparing(nameComparator)
                .thenComparing(lineComparator)).compare(this, other);
    }

    /**
     * Split the (raw) name into 2 parts:
     *      - the name without functionality codes
     *      - a list containing those functionality codes
     * @return a pair
     */
    public Pair<String, List<String>> getNameWithoutCodesAndFunctionalityCodesFromScenarioName() {
        return getNameWithoutCodesAndFunctionalityCodesFromScenarioName(name);
    }

    /**
     * Split the (raw) name into 2 parts:
     *      - the name without functionality codes
     *      - a list containing those functionality codes
     * @param scenarioName the (raw) scenario name
     * @return a pair
     */
    public static Pair<String, List<String>> getNameWithoutCodesAndFunctionalityCodesFromScenarioName(String scenarioName) {
        if (StringUtils.isBlank(scenarioName)) {
            return Pair.of("", new ArrayList<>());
        }

        var splitScenarioName = scenarioName.split("\\s*:\\s*", 2);
        if (splitScenarioName.length == 0) {
            return Pair.of("", new ArrayList<>());
        }

        if (splitScenarioName.length == 1) {
            var scenarioNameWithoutFeatures = splitScenarioName[0];
            return Pair.of(scenarioNameWithoutFeatures, new ArrayList<>());
        }

        var scenarioNameWithoutFeatures = splitScenarioName[1];
        var rawFeatures = splitScenarioName[0];
        var featureCodes = getFeatureCodesFromRawFeatureString(rawFeatures)
                .stream()
                .filter(Scenario::isInteger)
                .sorted(Comparator.comparingInt(Integer::parseInt))
                .distinct()
                .collect(Collectors.toList());
        return Pair.of(scenarioNameWithoutFeatures, featureCodes);
    }

    /**
     * Get feature codes from raw string
     * @param rawFeatureString raw feature string
     * @return the feature codes
     */
    private static List<String> getFeatureCodesFromRawFeatureString(String rawFeatureString) {
        if (StringUtils.isBlank(rawFeatureString)) {
            return new ArrayList<>();
        }

        var rawFeaturesSplit = rawFeatureString.split("(F|f)unctionalit(y|ies)\\s*");
        if (rawFeaturesSplit.length != 2) {
            return new ArrayList<>();
        }

        var rawFeatureCodes = rawFeaturesSplit[1].replaceAll("\\s+","");
        var featureCodes = Arrays.stream(rawFeatureCodes.split(",|&")).collect(Collectors.toList());
        return featureCodes;
    }

    /**
     * Get the scenario steps
     * @return the scenario steps
     */
    public List<ScenarioStep> getScenarioSteps() {
        var splitContents = getSplitContents(content);
        return splitContents.stream()
                .map(splitLine -> new ScenarioStep()
                        .withLine(Integer.valueOf(splitLine[0]))
                        .withContent(splitLine[splitLine.length - 1])
                )
                .collect(Collectors.toList());
    }

    /**
     * Get raw content lines
     * @param rawContent the raw content
     * @return the raw content lines
     */
    public static List<String[]> getSplitContents(String rawContent) {
        var rawLines = StringUtils.isNotBlank(rawContent) ? rawContent.split("\n") : new String[0];
        return Arrays.stream(rawLines)
                .map(rawLine -> rawLine.split("\\s*:\\s*"))
                .filter(splitLine -> splitLine.length == 3 || splitLine.length == 4)
                .filter(splitLine -> isInteger(splitLine[0]))
                .collect(Collectors.toList());
    }

    /**
     * Check that the string is an integer
     * @param integerAsString the string to check
     * @return true, iff the string is an integer
     */
    private static boolean isInteger(String integerAsString) {
        if (StringUtils.isBlank(integerAsString)) {
            return false;
        }

        try {
            Integer.parseInt(integerAsString);
        } catch(NumberFormatException e) {
            return false;
        }

        return true;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @With
    public static class ScenarioStep {

        protected int line;

        protected String content;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScenarioStep that = (ScenarioStep) o;
            return line == that.line && Objects.equals(content, that.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(line, content);
        }
    }
}
