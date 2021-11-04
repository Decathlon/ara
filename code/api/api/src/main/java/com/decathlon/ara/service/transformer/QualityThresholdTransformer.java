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

import com.decathlon.ara.ci.bean.QualityThreshold;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This service provide transformation utilities (JSON - DTO) for the QualityThreshold.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.0.1
 */
@Service
@Slf4j
public class QualityThresholdTransformer {

    static final TypeReference<Map<String, QualityThreshold>> TYPE_REFERENCE
            = new TypeReference<Map<String, QualityThreshold>>() {};

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Cast the given JSON String to a map of QualityThreshold objects.
     * <p>
     * In case of malformed JSON or not expected one, returns an empty map.
     *
     * @param jsonQualityThreshold the json representations of Quality Severities
     * @return the map of QualityThreshold.
     */
    public Map<String, QualityThreshold> toMap(String jsonQualityThreshold) {
        final Map<String, QualityThreshold> result = new HashMap<>();
        if (!StringUtils.isEmpty(jsonQualityThreshold)) {
            try {
                return objectMapper.readValue(jsonQualityThreshold, TYPE_REFERENCE);
            } catch (IOException e) {
                log.warn("EXECUTION|Cannot parse qualityThresholds: {}", jsonQualityThreshold, e);
                return new HashMap<>();
            }
        }
        return result;
    }
}
