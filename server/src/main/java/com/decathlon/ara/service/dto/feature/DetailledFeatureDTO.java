package com.decathlon.ara.service.dto.feature;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Contains the detailled informations (code, state, name, description) of a FeatureFlipping.
 *
 * @author Sylvain Nieuwlandt
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DetailledFeatureDTO extends FeatureDTO {

    private String name;
    private String description;

    public DetailledFeatureDTO(String code, boolean enabled, String name, String description) {
        super(code, enabled);
        this.description = description;
        this.name = name;
    }
}
