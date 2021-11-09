package com.decathlon.ara.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface MemberContainerRepository<C, I> extends JpaRepository<C, I> {

    C findByContainerIdentifier(String identifier);

}
