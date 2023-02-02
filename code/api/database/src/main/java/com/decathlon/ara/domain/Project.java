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

import com.decathlon.ara.domain.security.member.user.User;
import com.decathlon.ara.domain.security.member.user.UserScope;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_id")
    @SequenceGenerator(name = "project_id", sequenceName = "project_id", allocationSize = 1)
    private Long id;

    @Column(length = 32, nullable = false, unique = true)
    private String code;

    @Column(length = 64, nullable = false, unique = true)
    private String name;

    @Column(length = 512)
    private String description;

    private ZonedDateTime creationDate;

    @ManyToOne
    @JoinColumn(name = "creation_user_account_login", referencedColumnName = "login")
    @JoinColumn(name = "creation_user_account_provider", referencedColumnName = "provider")
    private User creationUser;

    private ZonedDateTime updateDate;

    @ManyToOne
    @JoinColumn(name = "update_user_account_login", referencedColumnName = "login")
    @JoinColumn(name = "update_user_account_provider", referencedColumnName = "provider")
    private User updateUser;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "project", orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Communication> communications = new ArrayList<>();

    @OneToMany(mappedBy = "project")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<UserScope> userScopes = new ArrayList<>();

    public Project() {
        this.creationDate = ZonedDateTime.now();
    }

    public Project(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.creationDate = ZonedDateTime.now();
    }

    public Project(String code, String name) {
        this.code = code;
        this.name = name;
        this.creationDate = ZonedDateTime.now();
    }

    public Project(String code, String name, User creationUser) {
        this.code = code;
        this.name = name;
        this.creationDate = ZonedDateTime.now();
        this.creationUser = creationUser;
    }

    public void addCommunication(Communication communication) {
        // Set the child-entity's foreign-key BEFORE adding the child-entity to the TreeSet,
        // as the foreign-key is required to place the child-entity in the right order (with child-entity's compareTo)
        // and is required not to change while the child-entity is in the TreeSet
        communication.setProject(this);
        communications.add(communication);
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public User getCreationUser() {
        return creationUser;
    }

    public void setCreationUser(User creationUser) {
        this.creationUser = creationUser;
    }

    public ZonedDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(ZonedDateTime updateDate) {
        this.updateDate = updateDate;
    }

    public User getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(User updateUser) {
        this.updateUser = updateUser;
    }

    public List<Communication> getCommunications() {
        return communications;
    }

    public void setCommunications(List<Communication> communications) {
        this.communications = communications;
    }

    public List<UserScope> getUserScopes() {
        return userScopes;
    }
}
