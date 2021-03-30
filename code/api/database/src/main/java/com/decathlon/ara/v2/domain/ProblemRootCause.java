package com.decathlon.ara.v2.domain;

import com.decathlon.ara.v2.domain.id.CodeWithProjectId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ara_problem_root_cause")
public class ProblemRootCause implements Serializable {

    @EmbeddedId
    private CodeWithProjectId id;

    @Column(length = 50)
    private String name;

    private String description;
}
