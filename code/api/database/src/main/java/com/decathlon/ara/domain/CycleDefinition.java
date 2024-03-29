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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class CycleDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cycle_definition_id")
    @SequenceGenerator(name = "cycle_definition_id", sequenceName = "cycle_definition_id", allocationSize = 1)
    private Long id;

    private long projectId;

    @Column(length = 16, nullable = false)
    private String branch;

    @Column(length = 16, nullable = false)
    private String name;

    @Column(nullable = false)
    private int branchPosition;

    public Long getId() {
        return id;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getBranch() {
        return branch;
    }

    public String getName() {
        return name;
    }

    public int getBranchPosition() {
        return branchPosition;
    }

    public void setBranchPosition(int branchPosition) {
        this.branchPosition = branchPosition;
    }
}
