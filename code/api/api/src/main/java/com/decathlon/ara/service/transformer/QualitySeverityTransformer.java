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

import com.decathlon.ara.service.dto.quality.QualitySeverityDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This service provide transformation utilities (JSON - DTO) for the QualitySeverity.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.0.1
 */
@Service
@Slf4j
public class QualitySeverityTransformer {

    static final TypeReference<List<QualitySeverityDTO>> TYPE_REFERENCE
            = new TypeReference<>() {
    };

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Cast the given JSON String to a list of QualitySeverity objects.
     * <p>
     * In case of malformed JSON or not expected one, returns an empty list.
     *
     * @param jsonQualitySeverities the json representations of Quality Severities
     * @return the list of QualitySeverity.
     */
    public List<QualitySeverityDTO> toDtos(String jsonQualitySeverities) {
        if (StringUtils.isEmpty(jsonQualitySeverities)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(jsonQualitySeverities, TYPE_REFERENCE);
        } catch (IOException e) {
            log.warn("EXECUTION|Cannot parse qualitySeverities: {}", jsonQualitySeverities, e);
            return new ArrayList<>();
        }
    }
}
