package com.decathlon.ara.repository.security.member.user.account;

import com.decathlon.ara.domain.security.member.user.account.UserProjectScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProjectScopeRepository extends JpaRepository<UserProjectScope, UserProjectScope.UserProjectScopeId> {

    @Modifying
    @Query(value = "delete from UserProjectScope scope where scope.scopeId = ?1")
    void deleteById(UserProjectScope.UserProjectScopeId id);
}
