package com.decathlon.ara.domain;

import com.decathlon.ara.domain.enumeration.MemberRole;

public interface MemberRelationship<C, M extends Member> {

    C getContainer();

    M getMember();

    String getMemberName();

    MemberRole getRole();

    void setRole(MemberRole role);
}
