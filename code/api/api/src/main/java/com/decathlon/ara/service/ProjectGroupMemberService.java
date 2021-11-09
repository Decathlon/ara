package com.decathlon.ara.service;

import java.util.function.BiConsumer;

import org.springframework.stereotype.Service;

import com.decathlon.ara.cache.CacheService;
import com.decathlon.ara.domain.Group;
import com.decathlon.ara.domain.GroupMember;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.ProjectGroupMember;
import com.decathlon.ara.repository.GroupMemberRepository;
import com.decathlon.ara.repository.GroupRepository;
import com.decathlon.ara.repository.ProjectGroupMemberRepository;
import com.decathlon.ara.repository.ProjectRepository;

@Service
public class ProjectGroupMemberService extends ProjectMemberService<Group, ProjectGroupMember> {

    private GroupMemberRepository groupMemberRepository;
    private CacheService cacheService;

    public ProjectGroupMemberService(ProjectRepository projectRepository, ProjectGroupMemberRepository projectGroupMemberRepository, GroupRepository groupRepository, GroupMemberRepository groupMemberRepository, CacheService cacheService) {
        super(projectRepository, projectGroupMemberRepository, groupRepository);
        this.groupMemberRepository = groupMemberRepository;
        this.cacheService = cacheService;
    }

    @Override
    protected ProjectGroupMember constructMember(Project project, Group member) {
        return new ProjectGroupMember(project, member);
    }

    @Override
    protected void afterAddMember(Project project, Group member) {
        evictCaches(project, member, cacheService::evictCaches);
    }

    @Override
    protected void afterUpdateRole(Project project, Group member) {
        evictCaches(project, member, cacheService::evictsUserProjectRolesCache);
    }

    @Override
    protected void afterDeleteMember(Project project, Group member) {
        evictCaches(project, member, cacheService::evictCaches);
    }

    private void evictCaches(Project project, Group member, BiConsumer<Project, String> groupMemberEvictCache) {
        for (GroupMember groupMember : groupMemberRepository.findAllByIdGroupName(member.getName())) {
            groupMemberEvictCache.accept(project, groupMember.getMemberName());
        }
    }

}
