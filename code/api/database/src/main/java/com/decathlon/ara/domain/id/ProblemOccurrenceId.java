package com.decathlon.ara.domain.id;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ProblemPattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
@Embeddable
public class ProblemOccurrenceId implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    private Error error;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProblemPattern problemPattern;
}
