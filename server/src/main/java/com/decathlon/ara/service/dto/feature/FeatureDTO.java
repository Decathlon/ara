package com.decathlon.ara.service.dto.feature;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Contains the basic informations (code, state) of a Feature Flipping.
 *
 * @author Sylvain Nieuwlandt
 */
@Data
@AllArgsConstructor
public class FeatureDTO {

    private String code;
    private boolean enabled;
}
