package com.decathlon.ara.v2.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ara_subdeployment_validation_job")
public class SubdeploymentValidationJob {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subdeployment_validation_job_seq")
    private Long id;

    private LocalDateTime startDate;

    private String jobUrl;
}
