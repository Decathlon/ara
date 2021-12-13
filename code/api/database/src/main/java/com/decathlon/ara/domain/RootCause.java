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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

@Entity
public class RootCause {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "root_cause_id")
    @SequenceGenerator(name = "root_cause_id", sequenceName = "root_cause_id", allocationSize = 1)
    private Long id;

    private long projectId;

    @Column(length = 128, nullable = false)
    private String name;

    // No cascade, as this collection is only used while removing a rootCause
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "rootCause")
    private List<Problem> problems = new ArrayList<>();

    public RootCause() {
    }

    public RootCause(long projectId, String name) {
        this.projectId = projectId;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public List<Problem> getProblems() {
        return problems;
    }

}
