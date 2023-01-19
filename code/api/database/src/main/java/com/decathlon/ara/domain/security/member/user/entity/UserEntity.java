package com.decathlon.ara.domain.security.member.user.entity;

import com.decathlon.ara.domain.Project;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@IdClass(UserEntity.UserEntityId.class)
public class UserEntity {

    @Id
    @Column(name = "provider", length = 30)
    private String providerName;

    @Id
    @Column(length = 50)
    private String login;

    public static class UserEntityId implements Serializable {

        private String providerName;

        private String login;

        public UserEntityId() {
        }

        public UserEntityId(@NonNull String providerName, @NonNull String login) {
            this.providerName = providerName;
            this.login = login;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserEntityId that = (UserEntityId) o;

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
    private UserEntityProfile profile;

    public enum UserEntityProfile {
        SUPER_ADMIN,
        AUDITOR,
        SCOPED_USER
    }

    @OneToMany(mappedBy = "userEntity", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<UserEntityRoleOnProject> rolesOnProjectWhenScopedUser = new ArrayList<>();

    public UserEntity() {
        this.profile = UserEntityProfile.SCOPED_USER;
    }

    public UserEntity(@NonNull String providerName, @NonNull String login) {
        this.providerName = providerName;
        this.login = login;
        this.profile = UserEntityProfile.SCOPED_USER;
    }

    public UserEntity(@NonNull String providerName, @NonNull String login, @NonNull UserEntityProfile profile) {
        this.providerName = providerName;
        this.login = login;
        this.profile = profile;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
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

    public UserEntityProfile getProfile() {
        return profile;
    }

    public void setProfile(@NonNull UserEntityProfile profile) {
        this.profile = profile;
    }

    public List<UserEntityRoleOnProject> getRolesOnProjectWhenScopedUser() {
        return rolesOnProjectWhenScopedUser;
    }

    public void setRolesOnProjectWhenScopedUser(List<UserEntityRoleOnProject> rolesOnProjectWhenScopedUser) {
        this.rolesOnProjectWhenScopedUser = rolesOnProjectWhenScopedUser;
    }
}
