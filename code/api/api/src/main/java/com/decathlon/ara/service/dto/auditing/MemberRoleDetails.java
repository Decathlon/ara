package com.decathlon.ara.service.dto.auditing;

import com.decathlon.ara.domain.enumeration.MemberRole;

public class MemberRoleDetails {
    
    private MemberRole memberRole;
    private String inheritFromGroup;
    
    public MemberRoleDetails(MemberRole memberRole, String inheritFromGroup) {
        this.memberRole = memberRole;
        this.inheritFromGroup = inheritFromGroup;
    }
    
    public MemberRole getMemberRole() {
        return memberRole;
    }
    
    public String getInheritFromGroup() {
        return inheritFromGroup;
    }

}
