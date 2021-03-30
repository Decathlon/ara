package com.decathlon.ara.v2.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ara_repository")
public class Repository {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="repository_seq")
    private Long id;

    private String url;

    private String description;

    private String comment;

    @OneToMany
    @JoinColumn(name = "repository_id", updatable = false, insertable = false)
    private List<Branch> branches = Collections.emptyList();
}
