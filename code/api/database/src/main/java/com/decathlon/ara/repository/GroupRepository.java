package com.decathlon.ara.repository;

import org.springframework.stereotype.Repository;

import com.decathlon.ara.domain.Group;
import com.decathlon.ara.domain.MemberContainerRepository;

@Repository
public interface GroupRepository extends MemberRepository<Group>, MemberContainerRepository<Group, String> {

    @Override
    default Group findByContainerIdentifier(String identifier) {
        return findByName(identifier);
    }

    @Override
    default Group findByMemberName(String memberName) {
        return findByName(memberName);
    }

    Group findByName(String name);

}
