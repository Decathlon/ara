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
import java.util.Comparator;

import static java.util.Comparator.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
@Entity
// Keep business key in sync with compareTo(): see https://developer.jboss.org/wiki/EqualsAndHashCode
@EqualsAndHashCode(of = { "projectId", "code" })
public class Country implements Comparable<Country> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "country_id")
    @SequenceGenerator(name = "country_id", sequenceName = "country_id", allocationSize = 1)
    private Long id;

    private long projectId;

    @Column(length = 2)
    private String code;

    @Column(length = 40)
    private String name;

    @Override
    public int compareTo(Country other) {
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<Country> projectIdComparator = comparing(c -> Long.valueOf(c.getProjectId()), nullsFirst(naturalOrder()));
        Comparator<Country> codeComparator = comparing(Country::getCode, nullsFirst(naturalOrder()));
        return nullsFirst(projectIdComparator
                .thenComparing(codeComparator)).compare(this, other);
    }

}
