package com.decathlon.ara.domain;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;

import com.decathlon.ara.domain.enumeration.UserSecurityRole;

@Entity
public class UserRole {
    @Embeddable
    public static class UserRolePk implements Serializable {
        private static final long serialVersionUID = 1L;
        private String userId;
        @Enumerated(EnumType.STRING)
        private UserSecurityRole role;

        public UserRolePk() {
        }

        public UserRolePk(String userId, UserSecurityRole role) {
            this.userId = userId;
            this.role = role;
        }
    }

    @EmbeddedId
    private UserRolePk id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    public UserRole() {
    }

    public UserRole(User user, UserSecurityRole role) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(role);
        this.id = new UserRolePk(user.getId(), role);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public String getUserId() {
        return id.userId;
    }

    public UserSecurityRole getRole() {
        return id.role;
    }
}
