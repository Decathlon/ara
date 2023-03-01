package com.decathlon.ara.repository.security.member.user.group;

import com.decathlon.ara.domain.security.member.user.group.UserGroupProjectScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupProjectScopeRepository extends JpaRepository<UserGroupProjectScope, UserGroupProjectScope.UserGroupProjectScopeId> {

    @Modifying
    @Query(value = "delete from UserGroupProjectScope scope where scope.scopeId = ?1")
    void deleteById(UserGroupProjectScope.UserGroupProjectScopeId id);
}
