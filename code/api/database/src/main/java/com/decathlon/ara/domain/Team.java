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
public class Team {

    public static final Team NOT_ASSIGNED = new Team(Long.valueOf(-404), -404, "(No team)", true, false, new ArrayList<>());

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "team_id")
    @SequenceGenerator(name = "team_id", sequenceName = "team_id", allocationSize = 1)
    private Long id;

    private long projectId;

    @Column(length = 128, nullable = false)
    private String name;

    private boolean assignableToProblems;

    private boolean assignableToFunctionalities;

    // No cascade, as this collection is only used while removing a team
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "blamedTeam")
    private List<Problem> problems = new ArrayList<>();

    public Team() {
    }

    public Team(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Team(Long id, long projectId, String name, boolean assignableToProblems, boolean assignableToFunctionalities,
            List<Problem> problems) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.assignableToProblems = assignableToProblems;
        this.assignableToFunctionalities = assignableToFunctionalities;
        this.problems = problems;
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

    public boolean isAssignableToProblems() {
        return assignableToProblems;
    }

    public boolean isAssignableToFunctionalities() {
        return assignableToFunctionalities;
    }

    public List<Problem> getProblems() {
        return problems;
    }

}
