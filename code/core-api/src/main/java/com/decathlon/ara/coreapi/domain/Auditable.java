package com.decathlon.ara.coreapi.domain;

import java.time.LocalDateTime;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class Auditable<U> {

    @CreatedBy
    private U createdBy;

    @CreatedDate
    private LocalDateTime createdDatetime;

    @LastModifiedBy
    private U lastModifiedBy;

    @LastModifiedDate
    private LocalDateTime lastModifiedDatetime;

    protected U getCreatedBy() {
        return createdBy;
    }

    protected LocalDateTime getCreatedDatetime() {
        return createdDatetime;
    }

    protected U getLastModifiedBy() {
        return lastModifiedBy;
    }

    protected LocalDateTime getLastModifiedDatetime() {
        return lastModifiedDatetime;
    }

}
