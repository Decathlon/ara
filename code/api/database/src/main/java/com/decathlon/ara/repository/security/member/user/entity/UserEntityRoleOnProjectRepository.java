package com.decathlon.ara.repository.security.member.user.entity;

import com.decathlon.ara.domain.security.member.user.entity.UserEntityRoleOnProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEntityRoleOnProjectRepository extends JpaRepository<UserEntityRoleOnProject, UserEntityRoleOnProject.UserEntityRoleOnProjectId> {
}
