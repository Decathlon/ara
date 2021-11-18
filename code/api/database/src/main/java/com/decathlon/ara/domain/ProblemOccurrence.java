package com.decathlon.ara.domain;

import com.decathlon.ara.domain.id.ProblemOccurrenceId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@NoArgsConstructor
@AssociationOverride(name = "problemOccurrenceId.error", joinColumns = @JoinColumn(name = "error_id"))
@AssociationOverride(name = "problemOccurrenceId.problemPattern", joinColumns = @JoinColumn(name = "problem_pattern_id"))
public class ProblemOccurrence implements Serializable {

    @EmbeddedId
    private ProblemOccurrenceId problemOccurrenceId;

    @Column(name = "error_id", insertable = false, updatable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Long errorId;

    @Column(name = "problem_pattern_id", insertable = false, updatable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Long problemPatternId;

    public ProblemOccurrence(Error error, ProblemPattern problemPattern) {
        problemOccurrenceId = new ProblemOccurrenceId(error, problemPattern);
    }

    @Transient
    public Error getError() {
        return problemOccurrenceId.getError();
    }

    public void setError(Error error) {
        problemOccurrenceId.setError(error);
    }

    @Transient
    public ProblemPattern getProblemPattern() {
        return problemOccurrenceId.getProblemPattern();
    }

    public void setProblemPattern(ProblemPattern problemPattern) {
        problemOccurrenceId.setProblemPattern(problemPattern);
    }

}
