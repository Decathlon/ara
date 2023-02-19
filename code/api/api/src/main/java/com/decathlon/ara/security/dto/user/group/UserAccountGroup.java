package com.decathlon.ara.security.dto.user.group;

import com.decathlon.ara.security.dto.user.UserAccount;
import com.decathlon.ara.security.dto.user.scope.UserAccountScope;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

import static com.decathlon.ara.service.util.DateService.DATE_FORMAT_YEAR_TO_SECOND;

public class UserAccountGroup {

    private String providerName;

    private Long id;

    private String name;

    private String description;

    @JsonProperty("creation_date")
    @JsonFormat(pattern = DATE_FORMAT_YEAR_TO_SECOND)
    private Date creationDate;

    @JsonProperty("creation_user")
    private String creationUserLogin;

    @JsonProperty("update_date")
    @JsonFormat(pattern = DATE_FORMAT_YEAR_TO_SECOND)
    private Date updateDate;

    @JsonProperty("update_user")
    private String updateUserLogin;

    private List<UserAccount> members = new ArrayList<>();

    private List<UserAccount> managers = new ArrayList<>();

    private List<UserAccountScope> scopes = new ArrayList<>();

    public UserAccountGroup(String providerName, Long id, String name, Date creationDate, String creationUserLogin) {
        this.providerName = providerName;
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
        this.creationUserLogin = creationUserLogin;
    }

    public String getProviderName() {
        return providerName;
    }

    public Long getId() {
        return id;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public String getCreationUserLogin() {
        return creationUserLogin;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getUpdateUserLogin() {
        return updateUserLogin;
    }

    public void setUpdateUserLogin(String updateUserLogin) {
        this.updateUserLogin = updateUserLogin;
    }

    public List<UserAccount> getMembers() {
        return members;
    }

    public void setMembers(List<UserAccount> members) {
        this.members = members;
    }

    public List<UserAccount> getManagers() {
        return managers;
    }

    public void setManagers(List<UserAccount> managers) {
        this.managers = managers;
    }

    public List<UserAccountScope> getScopes() {
        return scopes;
    }

    public void setScopes(List<UserAccountScope> scopes) {
        this.scopes = scopes;
    }
}
