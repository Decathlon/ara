package com.decathlon.ara.coreapi.domain;

import javax.persistence.*;

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

    protected Long getId() {
        return id;
    }

    protected String getName() {
        return name;
    }

    protected String getDescription() {
        return description;
    }

    protected String getCode() {
        return code;
    }

    protected String getPath() {
        return path;
    }

    protected String getStatus() {
        return status;
    }

    protected String getComment() {
        return comment;
    }

    protected AraPriority getPriority() {
        return priority;
    }

    protected AraTeam getTeam() {
        return team;
    }
}
