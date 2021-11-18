package com.decathlon.ara.domain.id;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ProblemPattern;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
@Embeddable
public class ProblemOccurrenceId implements Serializable {

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Error error;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ProblemPattern problemPattern;
}
