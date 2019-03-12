package com.decathlon.ara.defect.bean;

import com.decathlon.ara.domain.enumeration.ProblemStatus;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Defect {

    /**
     * The ID of the defect/issue/bug/... in the external issue-tracker.
     */
    private String id;

    /**
     * The fetched status (from the external issue-tracker) converted to a problem status.
     */
    private ProblemStatus status;

    /**
     * The date and time of the last closing of the defect,
     */
    private Date closeDateTime;

}
