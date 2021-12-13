/******************************************************************************
 * Copyright (C) 2020 by the ARA Contributors                                 *
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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import com.decathlon.ara.domain.enumeration.Technology;

@Entity
public class TechnologySetting {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "technology_setting_id")
    @SequenceGenerator(name = "technology_setting_id", sequenceName = "technology_setting_id", allocationSize = 1)
    private Long id;

    private Long projectId;

    @Column(nullable = false)
    private String code;

    private String value;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Technology technology;

    public TechnologySetting() {
    }

    public TechnologySetting(Long projectId, String code, Technology technology) {
        this.projectId = projectId;
        this.code = code;
        this.technology = technology;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Technology getTechnology() {
        return technology;
    }
}
