package com.decathlon.ara.v2.domain;

import com.decathlon.ara.v2.domain.enumeration.FeatureStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ara_feature")
public class Feature implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "feature_seq")
    private Long id;

    @Column(length = 50)
    private String name;

    @Column(length = 30)
    private String code;

    private String description;

    @Column(length = 500)
    private String path;
    
    private String versionWhenCreated;

    private int position;

    @Enumerated(EnumType.STRING)
    private FeatureStatus status;

    private LocalDateTime creationDateTime;

    private LocalDateTime updateDateTime;

    private String comment;

    @ManyToMany
    @JoinTable(
            name = "ara_features_tags",
            joinColumns = {
                    @JoinColumn(name = "feature_id", referencedColumnName = "id", nullable = false)
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "project_code", referencedColumnName = "project_code", updatable = false, insertable = false),
                    @JoinColumn(name = "tag_code", referencedColumnName = "code", nullable = false)
            }
    )
    private List<Tag> tags = Collections.emptyList();

    @ManyToOne
    @JoinColumn(name = "project_code", referencedColumnName = "project_code", updatable = false, insertable = false)
    @JoinColumn(name = "feature_priority_code", referencedColumnName = "code", updatable = false, insertable = false)
    private FeaturePriority priority;

    @Column(name = "feature_priority_code", length = 50, nullable = false)
    private String featurePriorityCode;

}
