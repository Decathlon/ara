package com.decathlon.ara.domain;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;

@Entity
public class ProblemOccurrence {

    public static class ProblemOccurrenceId implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long errorId;

        private Long problemPatternId;

        public ProblemOccurrenceId() {
        }

        public ProblemOccurrenceId(Long errorId, Long problemPatternId) {
            this.errorId = errorId;
            this.problemPatternId = problemPatternId;
        }

    }

    @EmbeddedId
    private ProblemOccurrenceId problemOccurrenceId;

    @MapsId("errorId")
    @ManyToOne(fetch = FetchType.LAZY)
    private Error error;

    @MapsId("problemPatternId")
    @ManyToOne(fetch = FetchType.LAZY)
    private ProblemPattern problemPattern;

    public ProblemOccurrence() {
    }

    public ProblemOccurrence(Error error, ProblemPattern problemPattern) {
        problemOccurrenceId = new ProblemOccurrenceId(error.getId(), problemPattern.getId());
        this.error = error;
        this.problemPattern = problemPattern;
    }

    public Error getError() {
        return error;
    }

    public ProblemPattern getProblemPattern() {
        return problemPattern;
    }

    public Long getErrorId() {
        return problemOccurrenceId.errorId;
    }

    public Long getProblemPatternId() {
        return problemOccurrenceId.problemPatternId;
    }

}
