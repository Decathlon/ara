package com.decathlon.ara.coreapi.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper=true)
@Entity
public class AraFeature extends Auditable<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ara_feature_id")
    @SequenceGenerator(name = "ara_feature_id", sequenceName = "ara_feature_id", allocationSize = 1)
    private Long id;

    private String name;
    private String description;
    private String code;
    private String path;
    private String status;
    private String comment;

    @ManyToOne
    private AraPriority priority;

    @ManyToOne
    private AraTeam team;
}
