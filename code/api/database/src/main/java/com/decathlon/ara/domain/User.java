package com.decathlon.ara.domain;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "name", "issuer" }))
public class User implements Member {

    @Id
    private String id;

    private String name;
    private String issuer;

    public User() {
    }

    public User(String name, String issuer) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.issuer = issuer;
    }

    @Override
    public String getMemberName() {
        return getId();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIssuer() {
        return issuer;
    }

}
