package com.decathlon.ara.repository.security.member.user;

import com.decathlon.ara.domain.security.member.user.role.ProjectRole;
import com.decathlon.ara.domain.security.member.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, User.UserId> {

    /**
     * Get all scoped users concerning a given provider
     * @param providerName the provider name (same as in the oauth2 configuration file - registrationId field)
     * @param projectCode optional: if not null, all scoped users having this project in their scope
     * @param role optional: if not null, all scoped users having at least one project with this role in their scope
     *             Note that if the projectCode parameter above is not null, then it concerns all scoped user having this specific role on this project (projectCode)
     * @return the matching scoped users
     */
    @Query("""
        select distinct user from User user
        left join user.scopes scope
        left join scope.project project
        where
            user.profile = com.decathlon.ara.domain.security.member.user.UserProfile.SCOPED_USER and
            user.providerName = :providerName and
            (:projectCode is null or project.code = :projectCode) and
            (:role is null or scope.role = :role)
    """)
    List<User> findAllScopedUsersByProviderName(@Param("providerName") String providerName, @Param("projectCode") String projectCode, @Param("role") ProjectRole role);

    List<User> findAllByProviderName(String providerName);
}
