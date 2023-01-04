package com.decathlon.ara.repository.security.member.user.entity;

import com.decathlon.ara.domain.security.member.user.entity.UserEntity;
import com.decathlon.ara.domain.security.member.user.entity.UserEntityRoleOnProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, UserEntity.UserEntityId> {

    /**
     * Get all scoped users concerning a given provider
     * @param providerName the provider name (same as in the oauth2 configuration file - registrationId field)
     * @param projectCode optional: if not null, all scoped users having this project in their scope
     * @param role optional: if not null, all scoped users having at least one project with this role in their scope
     *             Note that if the projectCode parameter above is not null, then it concerns all scoped user having this specific role on this project (projectCode)
     * @return the matching scoped users
     */
    @Query("""
        select distinct user from UserEntity user
        left join user.rolesOnProjectWhenScopedUser scope
        left join scope.project project
        where
            user.profile = com.decathlon.ara.domain.security.member.user.entity.UserEntity$UserEntityProfile.SCOPED_USER and
            user.providerName = :providerName and
            (:projectCode is null or project.code = :projectCode) and
            (:role is null or scope.role = :role)
    """)
    List<UserEntity> findAllScopedUsersByProviderName(@Param("providerName") String providerName, @Param("projectCode") String projectCode, @Param("role") UserEntityRoleOnProject.ScopedUserRoleOnProject role);

    List<UserEntity> findAllByProviderName(String providerName);
}
