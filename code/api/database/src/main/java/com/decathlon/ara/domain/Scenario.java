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

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

@Entity
public class Scenario implements Comparable<Scenario> {

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
        Comparator<Scenario> sourceComparator = comparing(Scenario::getSource, nullsFirst(naturalOrder()));
        Comparator<Scenario> featureFileComparator = comparing(Scenario::getFeatureFile, nullsFirst(naturalOrder()));
        Comparator<Scenario> nameComparator = comparing(Scenario::getName, nullsFirst(naturalOrder()));
        Comparator<Scenario> lineComparator = comparing(s -> Long.valueOf(s.getLine()), nullsFirst(naturalOrder()));
        return nullsFirst(sourceComparator
                .thenComparing(featureFileComparator)
                .thenComparing(nameComparator)
                .thenComparing(lineComparator)).compare(this, other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(featureFile, line, name, source);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Scenario)) {
            return false;
        }
        Scenario other = (Scenario) obj;
        return Objects.equals(featureFile, other.featureFile) && line == other.line && Objects.equals(name, other.name)
                && Objects.equals(source, other.source);
    }

    public static String getCountryCodesSeparator() {
        return COUNTRY_CODES_SEPARATOR;
    }

    public Long getId() {
        return id;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getFeatureFile() {
        return featureFile;
    }

    public void setFeatureFile(String featureFile) {
        this.featureFile = featureFile;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String getFeatureTags() {
        return featureTags;
    }

    public void setFeatureTags(String featureTags) {
        this.featureTags = featureTags;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public String getCountryCodes() {
        return countryCodes;
    }

    public void setCountryCodes(String countryCodes) {
        this.countryCodes = countryCodes;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWrongFunctionalityIds() {
        return wrongFunctionalityIds;
    }

    public void setWrongFunctionalityIds(String wrongFunctionalityIds) {
        this.wrongFunctionalityIds = wrongFunctionalityIds;
    }

    public String getWrongCountryCodes() {
        return wrongCountryCodes;
    }

    public void setWrongCountryCodes(String wrongCountryCodes) {
        this.wrongCountryCodes = wrongCountryCodes;
    }

    public String getWrongSeverityCode() {
        return wrongSeverityCode;
    }

    public void setWrongSeverityCode(String wrongSeverityCode) {
        this.wrongSeverityCode = wrongSeverityCode;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Set<Functionality> getFunctionalities() {
        return functionalities;
    }

}
