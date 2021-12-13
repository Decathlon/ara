package com.decathlon.ara.coreapi.domain;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

@Entity
public class AraProblemHistory {
    
    @Id
    @CreatedDate
    private LocalDateTime createdDatetime;

    @CreatedBy
    private String createdBy;

    private String status;

    protected LocalDateTime getCreatedDatetime() {
        return createdDatetime;
    }

    protected String getCreatedBy() {
        return createdBy;
    }

    protected String getStatus() {
        return status;
    }

}