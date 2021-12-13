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

package com.decathlon.ara.coverage;

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.repository.CountryRepository;
import com.decathlon.ara.service.dto.coverage.AxisPointDTO;

@Service
@Transactional
public class CountryAxisGenerator implements AxisGenerator {

    private final CountryRepository countryRepository;

    public CountryAxisGenerator(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @Override
    public String getCode() {
        return "countries";
    }

    @Override
    public String getName() {
        return "Countries";
    }

    @Override
    public Stream<AxisPointDTO> getPoints(long projectId) {
        return countryRepository.findAllByProjectIdOrderByCode(projectId).stream()
                .map(country -> new AxisPointDTO(country.getCode(), country.getCode().toUpperCase(), country.getName()));
    }

    @Override
    public String[] getValuePoints(Functionality functionality) {
        if (StringUtils.isEmpty(functionality.getCountryCodes())) {
            return null;
        }
        return functionality.getCountryCodes().split(Functionality.COUNTRY_CODES_SEPARATOR);
    }

}
