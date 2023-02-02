package com.decathlon.ara.domain.security.member.user;

import com.decathlon.ara.domain.Project;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private List<UserScope> scopes = new ArrayList<>();

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

    public List<UserScope> getScopes() {
        return scopes;
    }

    public void setScopes(List<UserScope> scopes) {
        this.scopes = scopes;
    }
}
