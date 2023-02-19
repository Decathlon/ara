package com.decathlon.ara.domain.security.member.user.group;

import com.decathlon.ara.domain.security.member.user.account.User;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class UserGroup {

    @Column(name = "provider", length = 30, nullable = false)
    private String providerName;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_group_id_generator")
    @SequenceGenerator(name = "user_group_id_generator", sequenceName = "user_group_id_sequence")
    private Long id;

    @Column(length = 64, nullable = false, unique = true)
    private String name;

    @Column(length = 512)
    private String description;

    private ZonedDateTime creationDate;

    @ManyToOne
    @JoinColumn(name = "creation_user_account_provider", referencedColumnName = "provider")
    @JoinColumn(name = "creation_user_account_login", referencedColumnName = "login")
    private User creationUser;

    private ZonedDateTime updateDate;

    @ManyToOne
    @JoinColumn(name = "update_user_account_provider", referencedColumnName = "provider")
    @JoinColumn(name = "update_user_account_login", referencedColumnName = "login")
    private User updateUser;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_group_membership",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = {
                    @JoinColumn(name = "member_account_provider", referencedColumnName = "provider"),
                    @JoinColumn(name = "member_account_login", referencedColumnName = "login")
            })
    private Set<User> members = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_group_management",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = {
                    @JoinColumn(name = "manager_account_provider", referencedColumnName = "provider"),
                    @JoinColumn(name = "manager_account_login", referencedColumnName = "login")
            })
    private Set<User> managers = new HashSet<>();

    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<UserGroupProjectScope> scopes = new HashSet<>();

    public UserGroup() {
        this.creationDate = ZonedDateTime.now();
    }

    public UserGroup(@NonNull String name, @NonNull User creationUser) {
        this.providerName = creationUser.getProviderName();
        this.name = name;
        this.creationDate = ZonedDateTime.now();
        this.creationUser = creationUser;
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

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public User getCreationUser() {
        return creationUser;
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

    public Set<User> getMembers() {
        return members;
    }

    public void setMembers(Set<User> members) {
        this.members = members;
    }

    public Set<User> getManagers() {
        return managers;
    }

    public void setManagers(Set<User> managers) {
        this.managers = managers;
    }

    public void addManager(User manager) {
        this.managers.add(manager);
    }

    public Set<UserGroupProjectScope> getScopes() {
        return scopes;
    }

    public void setScopes(Set<UserGroupProjectScope> scopes) {
        this.scopes = scopes;
    }
}
