package com.decathlon.ara.v2.domain;

import com.decathlon.ara.v2.domain.enumeration.ProblemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ara_problem")
public class Problem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "problem_seq")
    private Long id;

    private String name;

    private String description;

    private String defectCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemStatus status = ProblemStatus.OPEN;

    private LocalDateTime creationDateTime;

    private LocalDateTime firstOccurrenceDateTime;

    private LocalDateTime lastOccurrenceDateTime;

    private LocalDateTime defectClosingDateTime;

    private String comment;

    public Optional<String> getDefectCode() {
        return Optional.ofNullable(defectCode);
    }

    @ManyToOne
    @JoinColumn(name = "project_code", referencedColumnName = "project_code")
    @JoinColumn(name = "team_code", referencedColumnName = "code")
    private Team team;

    @ManyToOne
    @JoinColumn(name = "project_code", referencedColumnName = "project_code", updatable = false, insertable = false)
    @JoinColumn(name = "problem_root_cause_code", referencedColumnName = "code", updatable = false, insertable = false)
    private ProblemRootCause rootCause;

    @Column(name = "problem_root_cause_code", length = 50)
    private String rootCauseCode;

    @OneToMany(mappedBy = "problem")
    private List<ScenarioErrorTracePattern> scenarioErrorTracePatterns;
}
