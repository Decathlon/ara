package com.decathlon.ara.domain.security.member.user.account;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.security.member.user.group.UserGroup;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@IdClass(User.UserId.class)
@Table(name = "user_account")
public class User {

    @Id
    @Column(name = "provider", length = 30)
    private String providerName;

    @Id
    @Column(length = 50)
    private String login;

    public static class UserId implements Serializable {

        private String providerName;

        private String login;

        public UserId() {
        }

        public UserId(@NonNull String providerName, @NonNull String login) {
            this.providerName = providerName;
            this.login = login;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserId that = (UserId) o;

            if (!providerName.equals(that.providerName)) return false;
            return login.equals(that.login);
        }

        @Override
        public int hashCode() {
            int result = providerName.hashCode();
            result = 31 * result + login.hashCode();
            return result;
        }
    }

    @Column(length = 150)
    private String email;

    @Column(length = 50)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @Column(length = 512)
    private String pictureUrl;

    @ManyToOne
    private Project defaultProject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(11) default 'SCOPED_USER'")
    private UserProfile profile;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<UserProjectScope> scopes = new HashSet<>();

    @ManyToMany(mappedBy = "members", fetch = FetchType.EAGER)
    private Set<UserGroup> membershipGroups = new HashSet<>();

    @ManyToMany(mappedBy = "managers", fetch = FetchType.EAGER)
    private Set<UserGroup> managedGroups = new HashSet<>();

    public User() {
        this.profile = UserProfile.SCOPED_USER;
    }

    public User(@NonNull String providerName, @NonNull String login) {
        this.providerName = providerName;
        this.login = login;
        this.profile = UserProfile.SCOPED_USER;
    }

    public User(@NonNull String providerName, @NonNull String login, @NonNull UserProfile profile) {
        this.providerName = providerName;
        this.login = login;
        this.profile = profile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!providerName.equals(user.providerName)) return false;
        return login.equals(user.login);
    }

    @Override
    public int hashCode() {
        int result = providerName.hashCode();
        result = 31 * result + login.hashCode();
        return result;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getLogin() {
        return login;
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Optional<String> getFirstName() {
        return Optional.ofNullable(firstName);
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Optional<String> getLastName() {
        return Optional.ofNullable(lastName);
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Optional<String> getPictureUrl() {
        return Optional.ofNullable(pictureUrl);
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public Optional<Project> getDefaultProject() {
        return Optional.ofNullable(defaultProject);
    }

    public void setDefaultProject(Project defaultProject) {
        this.defaultProject = defaultProject;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(@NonNull UserProfile profile) {
        this.profile = profile;
    }

    public Set<UserProjectScope> getScopes() {
        return scopes;
    }

    public void setScopes(Set<UserProjectScope> scopes) {
        this.scopes = scopes;
    }

    public Set<UserGroup> getManagedGroups() {
        return managedGroups;
    }

    public void setManagedGroups(Set<UserGroup> managedGroups) {
        this.managedGroups = managedGroups;
    }

    public Set<UserGroup> getMembershipGroups() {
        return membershipGroups;
    }

    public void setMembershipGroups(Set<UserGroup> membershipGroups) {
        this.membershipGroups = membershipGroups;
    }
}
