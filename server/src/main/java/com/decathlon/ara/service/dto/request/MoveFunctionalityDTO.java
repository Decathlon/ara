package com.decathlon.ara.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveFunctionalityDTO {

    /**
     * The functionality ID to move.
     */
    private long sourceId;

    /**
     * The destination reference ID: null if reference is the (virtual) root folder.
     */
    private Long referenceId;

    /**
     * The position where to move sourceId relative to referenceId.
     */
    private FunctionalityPosition relativePosition;

}
