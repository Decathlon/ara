package com.decathlon.ara.domain;


import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import org.hibernate.annotations.GenericGenerator;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
@Entity
// Keep business key in sync with compareTo(): see https://developer.jboss.org/wiki/EqualsAndHashCode
@EqualsAndHashCode(of = { "source", "featureFile", "name", "line" })
public class Scenario implements Comparable<Scenario> {

    public static final String COUNTRY_CODES_SEPARATOR = ",";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
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
    private String countryCodes;

    /**
     * The severity code of the scenario, as stated in Version Control System.<br>
     * This is not a strict foreign-key association because developers can commit typos and we want to surface these.
     */
    private String severity;

    private String name;

    private String wrongFunctionalityIds;

    private String wrongCountryCodes;

    private String wrongSeverityCode;

    private int line;

    @Lob
    private String content;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "scenarios")
    private Set<Functionality> functionalities = new HashSet<>();

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
