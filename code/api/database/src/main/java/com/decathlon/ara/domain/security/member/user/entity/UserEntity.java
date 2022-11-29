package com.decathlon.ara.domain.security.member.user.entity;

import com.decathlon.ara.domain.Project;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@IdClass(UserEntity.UserEntityId.class)
public class UserEntity {

    private static final Logger LOG = LoggerFactory.getLogger(UserEntity.class);

    @Id
    @Column(length = 50)
    private String login;

    @Id
    @Column(name = "provider", length = 30)
    private String providerName;

    public static class UserEntityId implements Serializable {

        private String login;

        private String providerName;

        public UserEntityId() {
        }

        public UserEntityId(String login, String providerName) {
            this.login = login;
            this.providerName = providerName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserEntityId that = (UserEntityId) o;

            if (!login.equals(that.login)) return false;
            return providerName.equals(that.providerName);
        }

        @Override
        public int hashCode() {
            int result = login.hashCode();
            result = 31 * result + providerName.hashCode();
            return result;
        }
    }

    @Column(length = 150)
    private String email;

    @Column(length = 50)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @ManyToOne
    private Project defaultProject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(11) default 'SCOPED_USER'")
    private UserEntityProfile profile;

    public enum UserEntityProfile {
        SUPER_ADMIN,
        AUDITOR,
        SCOPED_USER;

        /**
         * Get the authority matching this user profile
         * @return the granted authority
         */
        public GrantedAuthority getMatchingAuthority() {
            return () -> String.format("USER_PROFILE:%s", this);
        }

        /**
         * Get the profile matching a string, when found
         * @param profileAsString the profile as string
         * @return the matching user profile
         */
        public static Optional<UserEntityProfile> getProfileFromString(String profileAsString) {
            if (StringUtils.isBlank(profileAsString)) {
                return Optional.empty();
            }

            try {
                return Optional.of(UserEntityProfile.valueOf(profileAsString.toUpperCase()));
            } catch (IllegalArgumentException exception) {
                LOG.warn("The user profile \"{}\" doesn't exist!", profileAsString);
                return Optional.empty();
            }
        }
    }

    @OneToMany(mappedBy = "userEntity", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<UserEntityRoleOnProject> rolesOnProjectWhenScopedUser = new ArrayList<>();

    public UserEntity() {
        this.profile = UserEntityProfile.SCOPED_USER;
    }

    public UserEntity(String login, String providerName) {
        this.login = login;
        this.providerName = providerName;
        this.profile = UserEntityProfile.SCOPED_USER;
    }

    public UserEntity(String login, String providerName, UserEntityProfile profile) {
        this.login = login;
        this.providerName = providerName;
        this.profile = profile;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
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

    public Optional<Project> getDefaultProject() {
        return Optional.ofNullable(defaultProject);
    }

    public void setDefaultProject(Project defaultProject) {
        this.defaultProject = defaultProject;
    }

    public UserEntityProfile getProfile() {
        return profile;
    }

    public void setProfile(UserEntityProfile profile) {
        this.profile = profile;
    }

    public List<UserEntityRoleOnProject> getRolesOnProjectWhenScopedUser() {
        return rolesOnProjectWhenScopedUser;
    }

    public void setRolesOnProjectWhenScopedUser(List<UserEntityRoleOnProject> rolesOnProjectWhenScopedUser) {
        this.rolesOnProjectWhenScopedUser = rolesOnProjectWhenScopedUser;
    }

    /**
     * Get all the user's matching authorities
     * @return the granted authorities
     */
    public Set<GrantedAuthority> getMatchingAuthorities() {
        var userProfile = profile == null ? UserEntityProfile.SCOPED_USER : profile;
        var profileAuthority = Set.of(userProfile.getMatchingAuthority());
        if (UserEntityProfile.SCOPED_USER.equals(userProfile)) {
            var scopedAuthorities = CollectionUtils.isEmpty(rolesOnProjectWhenScopedUser) ?
                    new HashSet<GrantedAuthority>() :
                    rolesOnProjectWhenScopedUser
                            .stream()
                            .map(UserEntityRoleOnProject::getMatchingAuthority)
                            .collect(Collectors.toSet());
            return Stream.of(profileAuthority, scopedAuthorities)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
        }

        return profileAuthority;
    }
}
