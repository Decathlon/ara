package com.decathlon.ara.service.dto.user;

import java.util.List;

import com.decathlon.ara.domain.enumeration.UserSecurityRole;

public class UserDTO {

    private String memberName;
    private String name;
    private String issuer;
    private List<UserSecurityRole> roles;

    public UserDTO() {
    }

    public UserDTO(String memberName, String name, String issuer) {
        this(memberName, name, issuer, null);
    }
    
    public UserDTO(String memberName, String name, String issuer, List<UserSecurityRole> roles) {
        this.memberName = memberName;
        this.name = name;
        this.issuer = issuer;
        this.roles = roles;
    }
    
    public String getMemberName() {
        return memberName;
    }

    public String getName() {
        return name;
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public List<UserSecurityRole> getRoles() {
        return roles;
    }
}
