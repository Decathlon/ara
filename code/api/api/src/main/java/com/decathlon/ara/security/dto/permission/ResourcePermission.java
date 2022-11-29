package com.decathlon.ara.security.dto.permission;

public enum ResourcePermission {

    FETCH(true),
    ALTER(false);

    private boolean readOnly;

    ResourcePermission(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isReadOnly() {
        return readOnly;
    }
}
