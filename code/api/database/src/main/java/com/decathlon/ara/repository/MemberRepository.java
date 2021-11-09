package com.decathlon.ara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.decathlon.ara.domain.Member;

@NoRepositoryBean
public interface MemberRepository<M extends Member> extends JpaRepository<M, String> {

    M findByMemberName(String memberName);

}
