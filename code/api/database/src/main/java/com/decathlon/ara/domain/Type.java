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
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

@Entity
public class Type implements Comparable<Type> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "type_id")
    @SequenceGenerator(name = "type_id", sequenceName = "type_id", allocationSize = 1)
    private Long id;

    private long projectId;

    @Column(length = 16, nullable = false)
    private String code;

    @Column(length = 50, nullable = false)
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
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @Override
    public int compareTo(Type other) {
        Comparator<Type> projectIdComparator = comparing(t -> Long.valueOf(t.getProjectId()), nullsFirst(naturalOrder()));
        Comparator<Type> codeComparator = comparing(Type::getCode, nullsFirst(naturalOrder()));
        return nullsFirst(projectIdComparator
                .thenComparing(codeComparator)).compare(this, other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, projectId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Type)) {
            return false;
        }
        Type other = (Type) obj;
        return Objects.equals(code, other.code) && projectId == other.projectId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean isBrowser() {
        return isBrowser;
    }

    public boolean isMobile() {
        return isMobile;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

}
