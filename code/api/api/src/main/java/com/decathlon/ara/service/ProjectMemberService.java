package com.decathlon.ara.service;

import com.decathlon.ara.domain.Member;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.ProjectMember;
import com.decathlon.ara.repository.MemberRepository;
import com.decathlon.ara.repository.ProjectMemberRepository;
import com.decathlon.ara.repository.ProjectRepository;

public abstract class ProjectMemberService<M extends Member, R extends ProjectMember<M>> extends MemberService<Project, M, R> {

    static final String CACHE_NAME = "security.user.project.roles";

    protected ProjectMemberService(ProjectRepository projectRepository, ProjectMemberRepository<M, R> projectMemberRepository, MemberRepository<M> memberRepository) {
        super(projectRepository, projectMemberRepository, memberRepository);
    }

}
