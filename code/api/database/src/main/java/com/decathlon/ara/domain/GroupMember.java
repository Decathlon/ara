package com.decathlon.ara.domain;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;

import com.decathlon.ara.domain.enumeration.MemberRole;

@Entity
public class GroupMember implements MemberRelationship<Group, User> {

    @Embeddable
    public static class GroupMemberPk implements Serializable {
        private static final long serialVersionUID = 1L;
        private String groupName;
        private String userName;

        public GroupMemberPk() {
        }

        public GroupMemberPk(String groupName, String userName) {
            this.groupName = groupName;
            this.userName = userName;
        }
    }

    @EmbeddedId
    private GroupMemberPk id;

    @MapsId("groupName")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_name")
    private Group group;

    @MapsId("userName")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_name")
    private User user;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    public GroupMember() {
    }

    public GroupMember(Group group, User user) {
        Objects.requireNonNull(group);
        Objects.requireNonNull(user);
        this.id = new GroupMemberPk(group.getName(), user.getMemberName());
        this.group = group;
        this.user = user;
    }

    public Group getGroup() {
        return group;
    }

    public String getGroupName() {
        return id.groupName;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Group getContainer() {
        return getGroup();
    }

    @Override
    public User getMember() {
        return getUser();
    }

    @Override
    public String getMemberName() {
        return id.userName;
    }

    @Override
    public MemberRole getRole() {
        return role;
    }

    @Override
    public void setRole(MemberRole role) {
        this.role = role;
    }

}
