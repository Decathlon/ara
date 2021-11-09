package com.decathlon.ara.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.decathlon.ara.domain.Group;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.ProjectGroupMember;

@Repository
public interface ProjectGroupMemberRepository extends ProjectMemberRepository<Group, ProjectGroupMember> {

    @Query(value = """
            select projectGroupMember from ProjectGroupMember projectGroupMember
            join GroupMember groupMember on projectGroupMember.member.name = groupMember.group.name
            where projectGroupMember.project.code = ?1 and groupMember.user.id = ?2
            """)

    List<ProjectGroupMember> findAllProjectGroupMemberByProjectCodeAndUserName(String projectCode, String userName);

    @Query(value = """
            select distinct projectGroupMember.project from ProjectGroupMember projectGroupMember
            join GroupMember groupMember on projectGroupMember.member.name = groupMember.group.name
            where groupMember.user.id = ?1
            """)
    Set<Project> findAllProjectByUserName(String userName);

}
