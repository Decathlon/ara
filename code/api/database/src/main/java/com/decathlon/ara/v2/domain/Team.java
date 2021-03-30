package com.decathlon.ara.v2.domain;

import com.decathlon.ara.v2.domain.id.CodeWithProjectId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ara_team")
public class Team implements Serializable {

    @EmbeddedId
    private CodeWithProjectId id;

    @Column(length = 50)
    private String name;

    private boolean assignableToFeatures = true;

    private boolean assignableToProblems = true;

    private String description;

    @OneToMany(mappedBy = "team")
    private List<Problem> problems = Collections.emptyList();

}
