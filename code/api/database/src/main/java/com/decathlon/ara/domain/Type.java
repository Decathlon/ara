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
public class Type implements Comparable<Type>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "type_id")
    @SequenceGenerator(name = "type_id", sequenceName = "type_id", allocationSize = 1)
    private Long id;

    private long projectId;

    @Column(length = 16)
    private String code;

    @Column(length = 50)
    private String name;

    private boolean isBrowser;

    private boolean isMobile;

    /**
     * The source where .feature or .json (depending on source's technology) files are stored on VCS.<br>
     * CAN be null: in this case, a run of this type will not be indexed in the execution.<br>
     * If an unknown type is found during execution indexation, an error is thrown, so a type with null source must be
     * created to indicate it is not an mis-configuration.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Source source;

    @Override
    public int compareTo(Type other) {
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<Type> projectIdComparator = comparing(t -> Long.valueOf(t.getProjectId()), nullsFirst(naturalOrder()));
        Comparator<Type> codeComparator = comparing(Type::getCode, nullsFirst(naturalOrder()));
        return nullsFirst(projectIdComparator
                .thenComparing(codeComparator)).compare(this, other);
    }

}
