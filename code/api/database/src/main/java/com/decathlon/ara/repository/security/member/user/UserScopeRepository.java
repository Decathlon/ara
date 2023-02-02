package com.decathlon.ara.repository.security.member.user;

import com.decathlon.ara.domain.security.member.user.UserScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserScopeRepository extends JpaRepository<UserScope, UserScope.UserScopeId> {

    @Modifying
    @Query(value = "delete from UserScope r where r.userScopeId = ?1")
    void deleteById(UserScope.UserScopeId id);
}
