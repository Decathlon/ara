package com.decathlon.ara.coreapi.domain;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class AraRepository {

    @EmbeddedId
    private AraRepositoryId id;

    private String name;
    private String description;

    protected AraRepositoryId getId() {
        return id;
    }

    protected String getName() {
        return name;
    }

    protected String getDescription() {
        return description;
    }

    @Embeddable
    public static class AraRepositoryId implements Serializable {
        @ManyToOne
        private AraProject project;
        private String url;

        protected AraProject getProject() {
            return project;
        }

        protected String getUrl() {
            return url;
        }
    }

}
