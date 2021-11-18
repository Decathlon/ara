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
public class Setting implements Comparable<Setting>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "setting_id")
    @SequenceGenerator(name = "setting_id", sequenceName = "setting_id", allocationSize = 1)
    private Long id;

    // No access to the parent project entity: settings are obtained from a project, so the project is already known
    private long projectId;

    @Column(length = 64)
    private String code;

    @Column(length = 512)
    private String value;

    @Override
    public int compareTo(Setting other) {
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<Setting> projectIdComparator = comparing(e -> Long.valueOf(e.projectId), nullsFirst(naturalOrder()));
        Comparator<Setting> codeComparator = comparing(e -> e.code, nullsFirst(naturalOrder()));
        return nullsFirst(projectIdComparator
                .thenComparing(codeComparator)).compare(this, other);
    }

}
