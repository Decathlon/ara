package com.decathlon.ara.domain;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;

@Entity
public class UserPreference {

    @Embeddable
    public static class UserPreferencePk implements Serializable {
        private static final long serialVersionUID = 1L;
        private String userId;
        private String key;

        public UserPreferencePk() {
        }

        public UserPreferencePk(String userId, String key) {
            this.userId = userId;
            this.key = key;
        }
    }

    @EmbeddedId
    private UserPreferencePk id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    private String value;

    public UserPreference() {
    }

    public UserPreference(User user, String key) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(key);
        this.id = new UserPreferencePk(user.getId(), key);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUserId() {
        return id.userId;
    }

    public String getKey() {
        return id.key;
    }

}
