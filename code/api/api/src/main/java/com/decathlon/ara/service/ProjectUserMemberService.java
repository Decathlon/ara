package com.decathlon.ara.service;

import org.springframework.stereotype.Service;

import com.decathlon.ara.cache.CacheService;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.ProjectUserMember;
import com.decathlon.ara.domain.User;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.ProjectUserMemberRepository;
import com.decathlon.ara.repository.UserRepository;

@Service
public class ProjectUserMemberService extends ProjectMemberService<User, ProjectUserMember> {

    private CacheService cacheService;

    protected ProjectUserMemberService(ProjectRepository projectRepository, ProjectUserMemberRepository projectUserMemberRepository, UserRepository userRepository, CacheService cacheService) {
        super(projectRepository, projectUserMemberRepository, userRepository);
        this.cacheService = cacheService;
    }

    @Override
    protected ProjectUserMember constructMember(Project project, User member) {
        return new ProjectUserMember(project, member);
    }

    @Override
    protected void afterAddMember(Project project, User member) {
        cacheService.evictCaches(project, member.getMemberName());
    }

    @Override
    protected void afterUpdateRole(Project project, User member) {
        cacheService.evictsUserProjectRolesCache(project, member.getMemberName());
    }

    @Override
    protected void afterDeleteMember(Project project, User member) {
        cacheService.evictCaches(project, member.getMemberName());
    }

}
