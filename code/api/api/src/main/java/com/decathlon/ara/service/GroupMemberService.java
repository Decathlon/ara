package com.decathlon.ara.service;

import org.springframework.stereotype.Service;

import com.decathlon.ara.cache.CacheService;
import com.decathlon.ara.domain.Group;
import com.decathlon.ara.domain.GroupMember;
import com.decathlon.ara.domain.User;
import com.decathlon.ara.repository.GroupMemberRepository;
import com.decathlon.ara.repository.GroupRepository;
import com.decathlon.ara.repository.UserRepository;

@Service
public class GroupMemberService extends MemberService<Group, User, GroupMember> {

    private CacheService cacheService;

    public GroupMemberService(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository, UserRepository userRepository, CacheService cacheService) {
        super(groupRepository, groupMemberRepository, userRepository);
        this.cacheService = cacheService;
    }

    @Override
    protected GroupMember constructMember(Group group, User member) {
        return new GroupMember(group, member);
    }

    @Override
    protected void afterAddMember(Group group, User member) {
        cacheService.evictCaches(member.getMemberName());
    }

    @Override
    protected void afterUpdateRole(Group group, User member) {
        cacheService.evictsUserProjectRolesCache(member.getMemberName());
    }

    @Override
    protected void afterDeleteMember(Group group, User member) {
        cacheService.evictCaches(member.getMemberName());
    }

}
