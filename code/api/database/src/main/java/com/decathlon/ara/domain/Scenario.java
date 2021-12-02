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

import javax.persistence.*;
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

}
