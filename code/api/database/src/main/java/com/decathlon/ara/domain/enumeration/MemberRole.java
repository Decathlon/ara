package com.decathlon.ara.domain.enumeration;

import java.util.HashSet;
import java.util.Set;

public enum MemberRole {

    MEMBER(null, Permission.READ),
    MAINTAINER(MEMBER, Permission.WRITE),
    ADMIN(MAINTAINER, Permission.ADMIN);

    private Set<Permission> permissions = new HashSet<>();

    private MemberRole(MemberRole included, Permission... permissions) {
        if (included != null) {
            this.permissions.addAll(included.permissions);
        }
        for (Permission permission : permissions) {
            this.permissions.add(permission);
        }
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

}
