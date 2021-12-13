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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

import com.decathlon.ara.domain.enumeration.CommunicationType;

@Entity
// Keep business key in sync with compareTo(): see https://developer.jboss.org/wiki/EqualsAndHashCode
public class Communication {

    public static final String EXECUTIONS = "executions";
    public static final String SCENARIO_WRITING_HELPS = "scenario-writing-helps";
    public static final String HOW_TO_ADD_SCENARIO = "how" + "to-add-scenario";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "communication_id")
    @SequenceGenerator(name = "communication_id", sequenceName = "communication_id", allocationSize = 1)
    private Long id;

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

    public Communication() {
    }

    public Communication(Project project, String code, String name, CommunicationType type, String message) {
        this.project = project;
        this.code = code;
        this.name = name;
        this.type = type;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public CommunicationType getType() {
        return type;
    }

    public void setType(CommunicationType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
