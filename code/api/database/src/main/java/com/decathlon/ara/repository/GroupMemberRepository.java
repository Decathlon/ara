package com.decathlon.ara.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.decathlon.ara.domain.Group;
import com.decathlon.ara.domain.GroupMember;
import com.decathlon.ara.domain.User;

@Repository
public interface GroupMemberRepository extends MemberRelationshipRepository<Group, User, GroupMember> {

    @Override
    default List<GroupMember> findAllByContainerIdentifier(String identifier) {
        return findAllByIdGroupName(identifier);
    }

    List<GroupMember> findAllByIdGroupName(String groupName);

    List<GroupMember> findAllByIdUserName(String memberName);

    @Override
    default GroupMember findByContainerIdentifierAndMemberName(String identifier, String memberName) {
        return findByIdGroupNameAndIdUserName(identifier, memberName);
    }

    GroupMember findByIdGroupNameAndIdUserName(String groupName, String userName);

    void deleteByIdGroupName(String groupName);

}
