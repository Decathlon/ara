/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

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
