package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.Country;
import com.decathlon.ara.service.dto.country.CountryDTO;
import org.springframework.stereotype.Service;

/**
 * This service provide transformation utilities (DTO - DO and DO - DTO) for the Country.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.0.1
 */
@Service
class CountryTransformer {

    /**
     * Transform the given Country DO to a CountryDTO object.
     * <p>
     * Returns an empty CountryDTO if the parameter is null.
     *
     * @param country the DO to transform
     * @return the result DTO.
     */
    CountryDTO toDto(Country country) {
        CountryDTO result = new CountryDTO();
        if (null != country) {
            result.setCode( country.getCode() );
            result.setName( country.getName() );
        }

        return result;
    }
}
