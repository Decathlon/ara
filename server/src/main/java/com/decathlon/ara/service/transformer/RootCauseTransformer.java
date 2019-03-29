package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.RootCause;
import com.decathlon.ara.service.dto.rootcause.RootCauseDTO;
import org.springframework.stereotype.Service;

/**
 * This service provide transformation utilities (DTO - DO and DO - DTO) for the RootCause.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.0.1
 */
@Service
class RootCauseTransformer {

    /**
     * Transform the given RootCause DO to a RootCauseDTO object.
     * <p>
     * Returns an empty RootCauseDTO if the parameter is null.
     *
     * @param rootCause the DO to transform
     * @return the result DTO.
     */
    RootCauseDTO toDto(RootCause rootCause) {
        RootCauseDTO result = new RootCauseDTO();
        result.setId(0L);
        if (null != rootCause) {
            result.setId(rootCause.getId());
            result.setName(rootCause.getName());
        }
        return result;
    }
}
