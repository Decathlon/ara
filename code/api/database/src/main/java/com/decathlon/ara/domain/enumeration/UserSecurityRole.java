package com.decathlon.ara.domain.enumeration;

public enum UserSecurityRole {

    ADMIN(null),
    PROJECT_OR_GROUP_CREATOR(ADMIN),
    AUDITING(null);

    private UserSecurityRole parent;

    private UserSecurityRole(UserSecurityRole parent) {
        this.parent = parent;
    }

    public UserSecurityRole getParent() {
        return parent;
    }

}
