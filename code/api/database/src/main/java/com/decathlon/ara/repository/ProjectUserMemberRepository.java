package com.decathlon.ara.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.ProjectUserMember;
import com.decathlon.ara.domain.User;

@Repository
public interface ProjectUserMemberRepository extends ProjectMemberRepository<User, ProjectUserMember> {

    @Query(value = """
            select distinct projectUserMember.project from ProjectUserMember projectUserMember
            join projectUserMember.member user
            where user.id = ?1
            """)
    Set<Project> findAllProjectByUserName(String userName);

}
