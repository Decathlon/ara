package com.decathlon.ara.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.decathlon.ara.domain.Member;
import com.decathlon.ara.domain.MemberRelationship;
import com.decathlon.ara.domain.ProjectMember.ProjectMemberPk;

@NoRepositoryBean
public interface MemberRelationshipRepository<C, M extends Member, R extends MemberRelationship<C, M>> extends JpaRepository<R, ProjectMemberPk> {

    List<R> findAllByContainerIdentifier(String identifier);

    R findByContainerIdentifierAndMemberName(String identifier, String memberName);

}
