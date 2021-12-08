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

import static java.util.Comparator.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
@Entity
// Keep business key in sync with compareTo(): see https://developer.jboss.org/wiki/EqualsAndHashCode
@EqualsAndHashCode(of = { "projectId", "code" })
public class Severity implements Comparable<Severity>, Serializable {

    private static final String GLOBAL_NAME = "Global";

    /**
     * Fake severity that encompass all ones, to have a global summary.
     */
    public static final Severity ALL = new Severity(Long.valueOf(-1), -1, "*", Integer.MAX_VALUE, GLOBAL_NAME, GLOBAL_NAME, GLOBAL_NAME, false);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "severity_id")
    @SequenceGenerator(name = "severity_id", sequenceName = "severity_id", allocationSize = 1)
    private Long id;

    private long projectId;

    @Column(length = 32, nullable = false)
    private String code;

    /**
     * The order in which the severities should appear: the lowest position should be for the highest severity.
     */
    @Column(nullable = false)
    private int position;

    /**
     * The full name (eg. "Sanity Check").
     */
    @Column(length = 32, nullable = false)
    private String name;

    /**
     * The shorter name (but still intelligible) to display on table column headers where space is constrained (eg. "Sanity Ch.").
     */
    @Column(length = 16, nullable = false)
    private String shortName;

    /**
     * The shortest name to display on email subjects to help keep it very short (eg. "S.C.").
     */
    @Column(length = 8, nullable = false)
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
