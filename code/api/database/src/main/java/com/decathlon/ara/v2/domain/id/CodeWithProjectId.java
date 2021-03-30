package com.decathlon.ara.v2.domain.id;

import com.decathlon.ara.v2.domain.Project;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class CodeWithProjectId implements Serializable {

    @Column(length = 50)
    private String code;

    @ManyToOne
    private Project project;
}
