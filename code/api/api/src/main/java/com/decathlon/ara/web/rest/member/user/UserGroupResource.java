package com.decathlon.ara.web.rest.member.user;

import com.decathlon.ara.security.dto.user.group.UserAccountGroup;
import com.decathlon.ara.security.service.user.group.UserAccountGroupService;
import com.decathlon.ara.service.exception.ForbiddenException;
import com.decathlon.ara.web.rest.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static com.decathlon.ara.Entities.GROUP;
import static com.decathlon.ara.security.configuration.SecurityConfiguration.MEMBER_USER_BASE_API_PATH;
import static com.decathlon.ara.web.rest.member.user.UserGroupResource.MEMBER_USER_GROUP_BASE_API_PATH;

@RestController
@RequestMapping(MEMBER_USER_GROUP_BASE_API_PATH)
public class UserGroupResource {
    
    private final UserAccountGroupService userAccountGroupService;
    
    public static final String MEMBER_USER_GROUP_BASE_API_PATH = MEMBER_USER_BASE_API_PATH + "/groups";

    private static final String GROUP_ID = "/{groupId:[0-9]+}";
    public static final String MEMBER_USER_GROUP_BY_ID_API_PATH = MEMBER_USER_GROUP_BASE_API_PATH + GROUP_ID;

    private static final String ACCOUNT = "/account";
    private static final String CURRENT = ACCOUNT + "/current";
    private static final String USER_LOGIN = ACCOUNT + "/login/{userLogin}";

    private static final String ALL_GROUPS = "/all";
    public static final String MEMBER_USER_GROUP_ALL_GROUPS_API_PATH = MEMBER_USER_GROUP_BASE_API_PATH + ALL_GROUPS;

    private static final String GROUPS_CONTAINING_USER = "/containing";
    private static final String GROUPS_CONTAINING_USER_FROM_LOGIN = GROUPS_CONTAINING_USER + USER_LOGIN;
    private static final String GROUPS_CONTAINING_CURRENT_USER = GROUPS_CONTAINING_USER + CURRENT;
    public static final String MEMBER_USER_GROUPS_CONTAINING_USER_FROM_LOGIN_API_PATH = MEMBER_USER_GROUP_BASE_API_PATH + GROUPS_CONTAINING_USER_FROM_LOGIN;
    public static final String MEMBER_USER_GROUPS_CONTAINING_CURRENT_USER_API_PATH = MEMBER_USER_GROUP_BASE_API_PATH + GROUPS_CONTAINING_CURRENT_USER;

    private static final String MANAGED_GROUPS = "/managed";
    private static final String MANAGED_GROUPS_FROM_USER_LOGIN = MANAGED_GROUPS + USER_LOGIN;
    private static final String MANAGED_GROUPS_BY_CURRENT_USER = MANAGED_GROUPS + CURRENT;
    public static final String MEMBER_USER_GROUP_MANAGED_GROUPS_FROM_USER_LOGIN_API_PATH = MEMBER_USER_GROUP_BASE_API_PATH + MANAGED_GROUPS_FROM_USER_LOGIN;
    public static final String MEMBER_USER_GROUP_MANAGED_GROUPS_BY_CURRENT_USER_API_PATH = MEMBER_USER_GROUP_BASE_API_PATH + MANAGED_GROUPS_BY_CURRENT_USER;

    public UserGroupResource(UserAccountGroupService userAccountGroupService) {
        this.userAccountGroupService = userAccountGroupService;
    }

    @PostMapping
    public ResponseEntity<UserAccountGroup> createGroup(@RequestBody UserAccountGroup group) {
        try {
            var createdGroup = userAccountGroupService.createGroup(group);
            return ResponseEntity.ok(createdGroup);
        } catch (ForbiddenException e) {
            return ResponseUtil.handle(e);
        }
    }

    @PutMapping(GROUP_ID)
    public ResponseEntity<UserAccountGroup> updateGroup(@PathVariable Long groupId, @RequestBody @NonNull UserAccountGroup group) {
        try {
            if (group.getId() == null || !Objects.equals(group.getId(), groupId)) {
                return ResponseEntity.badRequest().build();
            }

            var updatedGroup = userAccountGroupService.updateGroup(group);
            return ResponseEntity.ok(updatedGroup);
        } catch (ForbiddenException e) {
            return ResponseUtil.handle(e);
        }
    }

    @DeleteMapping(GROUP_ID)
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId) {
        try {
            userAccountGroupService.deleteGroup(groupId);
            return ResponseUtil.deleted(GROUP, groupId);
        } catch (ForbiddenException e) {
            return ResponseUtil.handle(e);
        }
    }

    @GetMapping(ALL_GROUPS)
    public ResponseEntity<List<UserAccountGroup>> getAllGroups() {
        try {
            var groups = userAccountGroupService.getAllUserAccountGroupsForCurrentProviderName();
            return ResponseEntity.ok(groups);
        } catch (ForbiddenException e) {
            return ResponseUtil.handle(e);
        }
    }

    @GetMapping(GROUPS_CONTAINING_USER_FROM_LOGIN)
    public ResponseEntity<List<UserAccountGroup>> getGroupsContainingAccount(@PathVariable String userLogin) {
        try {
            var groups = userAccountGroupService.getGroupsContainingUser(userLogin);
            return ResponseEntity.ok(groups);
        } catch (ForbiddenException e) {
            return ResponseUtil.handle(e);
        }
    }

    @GetMapping(GROUPS_CONTAINING_CURRENT_USER)
    public ResponseEntity<List<UserAccountGroup>> getGroupsContainingCurrentUser() {
        try {
            var groups = userAccountGroupService.getGroupsContainingCurrentUser();
            return ResponseEntity.ok(groups);
        } catch (ForbiddenException e) {
            return ResponseUtil.handle(e);
        }
    }

    @GetMapping(MANAGED_GROUPS_FROM_USER_LOGIN)
    public ResponseEntity<List<UserAccountGroup>> getGroupsManagedByUserAccount(@PathVariable String userLogin) {
        try {
            var groups = userAccountGroupService.getGroupsManagedByUser(userLogin);
            return ResponseEntity.ok(groups);
        } catch (ForbiddenException e) {
            return ResponseUtil.handle(e);
        }
    }

    @GetMapping(MANAGED_GROUPS_BY_CURRENT_USER)
    public ResponseEntity<List<UserAccountGroup>> getGroupsManagedByCurrentUser() {
        try {
            var groups = userAccountGroupService.getGroupsManagedByCurrentUser();
            return ResponseEntity.ok(groups);
        } catch (ForbiddenException e) {
            return ResponseUtil.handle(e);
        }
    }
}
