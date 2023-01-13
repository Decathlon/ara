package com.decathlon.ara.repository.security.member.user.entity;

import com.decathlon.ara.domain.security.member.user.entity.UserEntityRoleOnProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEntityRoleOnProjectRepository extends JpaRepository<UserEntityRoleOnProject, UserEntityRoleOnProject.UserEntityRoleOnProjectId> {

    @Modifying
    @Query(value = "delete from UserEntityRoleOnProject r where r.userEntityRoleOnProjectId = ?1")
    void deleteById(UserEntityRoleOnProject.UserEntityRoleOnProjectId id);
}
