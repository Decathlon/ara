package com.decathlon.ara.coreapi.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.Collections;
import java.util.Set;

@Data
@Entity
public class AraRule {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ara_rule_id")
    @SequenceGenerator(name = "ara_rule_id", sequenceName = "ara_rule_id", allocationSize = 1)
    private Long id;

    @OneToOne(optional = false)
    private AraProject project;

    @OneToMany
    private Set<AraTag> runTags = Collections.emptySet();

    @OneToMany
    private Set<AraTag> executionTags = Collections.emptySet();

}
