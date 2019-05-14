package com.decathlon.ara.domain;

import java.util.Comparator;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@EqualsAndHashCode(of = { "projectId", "code" })
public class Severity implements Comparable<Severity> {

    private static final String GLOBAL_NAME = "Global";

    /**
     * Fake severity that encompass all ones, to have a global summary.
     */
    public static final Severity ALL = new Severity(Long.valueOf(-1), -1, "*", Integer.MAX_VALUE, GLOBAL_NAME, GLOBAL_NAME, GLOBAL_NAME, false);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    private long projectId;

    private String code;

    /**
     * The order in which the severities should appear: the lowest position should be for the highest severity.
     */
    private int position;

    /**
     * The full name (eg. "Sanity Check").
     */
    private String name;

    /**
     * The shorter name (but still intelligible) to display on table column headers where space is constrained (eg. "Sanity Ch.").
     */
    private String shortName;

    /**
     * The shortest name to display on email subjects to help keep it very short (eg. "S.C.").
     */
    private String initials;

    /**
     * True to use that severity as a default one when a scenario does not declare its severity or has a nonexistent
     * one. Only one severity can be declared as the default.
     */
    private boolean defaultOnMissing;

    @Override
    public int compareTo(Severity other) {
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<Severity> projectIdComparator = comparing(s -> Long.valueOf(s.getProjectId()), nullsFirst(naturalOrder()));
        Comparator<Severity> codeComparator = comparing(Severity::getCode, nullsFirst(naturalOrder()));
        return nullsFirst(projectIdComparator
                .thenComparing(codeComparator)).compare(this, other);
    }

    public static final class SeverityPositionComparator implements Comparator<Severity> {

        @Override
        public int compare(Severity o1, Severity o2) {
            Comparator<Severity> projectIdComparator = comparing(s -> Long.valueOf(s.getProjectId()), nullsFirst(naturalOrder()));
            Comparator<Severity> positionComparator = comparing(s -> Integer.valueOf(s.getPosition()), nullsFirst(naturalOrder()));
            return nullsFirst(projectIdComparator
                    .thenComparing(positionComparator)).compare(o1, o2);
        }

    }

}
