package com.decathlon.ara.repository;

import java.util.List;

import org.springframework.data.repository.NoRepositoryBean;

import com.decathlon.ara.domain.Member;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.ProjectMember;

@NoRepositoryBean
public interface ProjectMemberRepository<M extends Member, T extends ProjectMember<M>> extends MemberRelationshipRepository<Project, M, T> {

    @Override
    default java.util.List<T> findAllByContainerIdentifier(String identifier) {
        return findAllByProjectCode(identifier);
    }

    List<T> findAllByProjectCode(String projectCode);

    List<T> findAllByIdMemberName(String memberName);

    @Override
    default T findByContainerIdentifierAndMemberName(String identifier, String memberName) {
        return findByProjectCodeAndIdMemberName(identifier, memberName);
    }

    T findByProjectCodeAndIdMemberName(String projectCode, String memberName);

    void deleteByProjectCode(String code);

}
