package com.decathlon.ara.v2.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ara_deployed_version")
public class DeployedVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="deployed_version_seq")
    private Long id;

    @Column(nullable = false)
    private String environmentName;

    @Column(nullable = false)
    private String environmentUrl;

    private String currentVersion;

    private LocalDateTime latestDeploymentDate;

    @ManyToOne
    @JoinColumn(name = "project_code", referencedColumnName = "project_code", updatable = false, insertable = false)
    @JoinColumn(name = "branch_code", referencedColumnName = "code", updatable = false, insertable = false)
    private Branch branch;
}
