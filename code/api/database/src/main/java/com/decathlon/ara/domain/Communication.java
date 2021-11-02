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

import com.decathlon.ara.domain.enumeration.CommunicationType;
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
public class Communication implements Comparable<Communication>, Serializable {

    public static final String EXECUTIONS = "executions";
    public static final String SCENARIO_WRITING_HELPS = "scenario-writing-helps";
    public static final String HOW_TO_ADD_SCENARIO = "how" + "to-add-scenario";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "communication_id")
    @SequenceGenerator(name = "communication_id", sequenceName = "communication_id", allocationSize = 1)
    private Long id;

    // 1/2 for @EqualsAndHashCode to work: used when an entity is fetched by JPA
    @Column(name = "project_id", insertable = false, updatable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(length = 32, nullable = false)
    private String code;

    @Column(length = 64, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 4, nullable = false)
    private CommunicationType type;

    @Lob
    @org.hibernate.annotations.Type(type = "org.hibernate.type.TextType")
    private String message;

    // 2/2 for @EqualsAndHashCode to work: used for entities created outside of JPA
    public void setProject(Project project) {
        this.project = project;
        this.projectId = (project == null ? null : project.getId());
    }

    @Override
    public int compareTo(Communication other) {
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<Communication> projectIdComparator = comparing(c -> c.projectId, nullsFirst(naturalOrder()));
        Comparator<Communication> codeComparator = comparing(Communication::getCode, nullsFirst(naturalOrder()));
        return nullsFirst(projectIdComparator
                .thenComparing(codeComparator)).compare(this, other);
    }

}
