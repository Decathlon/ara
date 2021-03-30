package com.decathlon.ara.v2.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.*;
import java.io.Serializable;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ara_job_platform")
public class JobPlatform implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "job_platform_seq")
    private Long id;

    @Column(length = 50)
    private String name;

    private String url;

    private String comment;

    private String description;
}
