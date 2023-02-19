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

package com.decathlon.ara.security.dto.user;

import com.decathlon.ara.security.dto.user.scope.UserAccountScope;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class UserAccount {

    private String providerName;

    private String login;

    private String firstName;

    private String lastName;

    private String email;

    private String pictureUrl;

    private UserAccountProfile profile;

    private List<UserAccountScope> scopes = new ArrayList<>();

    @JsonProperty("default_project")
    private String defaultProjectCode;

    @JsonIgnore
    private List<Long> managedGroupIds = new ArrayList<>();

    @JsonIgnore
    private List<Long> membershipGroupIds = new ArrayList<>();

    public UserAccount(String providerName, String login) {
        this.providerName = providerName;
        this.login = login;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getLogin() {
        return login;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public UserAccountProfile getProfile() {
        return profile;
    }

    public List<UserAccountScope> getScopes() {
        return scopes;
    }

    public String getDefaultProjectCode() {
        return defaultProjectCode;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public void setProfile(UserAccountProfile profile) {
        this.profile = profile;
    }

    public void setScopes(List<UserAccountScope> scopes) {
        this.scopes = scopes;
    }

    public void setDefaultProjectCode(String defaultProjectCode) {
        this.defaultProjectCode = defaultProjectCode;
    }

    public List<Long> getManagedGroupIds() {
        return managedGroupIds;
    }

    public void setManagedGroupIds(List<Long> managedGroupIds) {
        this.managedGroupIds = managedGroupIds;
    }

    public List<Long> getMembershipGroupIds() {
        return membershipGroupIds;
    }

    public void setMembershipGroupIds(List<Long> membershipGroupIds) {
        this.membershipGroupIds = membershipGroupIds;
    }
}
