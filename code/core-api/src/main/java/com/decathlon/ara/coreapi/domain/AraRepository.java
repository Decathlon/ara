package com.decathlon.ara.coreapi.domain;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
public class AraRepository {

    @EmbeddedId
    private AraRepositoryId id;

    private String name;
    private String description;

    @Data
    @Embeddable
    public static class AraRepositoryId implements Serializable {
        @ManyToOne
        private AraProject project;
        private String url;
    }

}
