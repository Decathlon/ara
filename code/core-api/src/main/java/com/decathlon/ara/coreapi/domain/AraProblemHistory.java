package com.decathlon.ara.coreapi.domain;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import lombok.Data;

@Data
@Entity
public class AraProblemHistory {
    
    @Id
    @CreatedDate
    private LocalDateTime createdDatetime;

    @CreatedBy
    private String createdBy;

    private String status;

}