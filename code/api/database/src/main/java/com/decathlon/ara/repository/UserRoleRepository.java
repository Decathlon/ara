package com.decathlon.ara.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.decathlon.ara.domain.UserRole;
import com.decathlon.ara.domain.UserRole.UserRolePk;
import com.decathlon.ara.domain.enumeration.UserSecurityRole;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRolePk> {

    UserRole findByIdUserIdAndIdRole(String userId, UserSecurityRole role);

    List<UserRole> findAllByIdUserId(String userId);

    boolean existsByIdRole(UserSecurityRole role);
    
}
