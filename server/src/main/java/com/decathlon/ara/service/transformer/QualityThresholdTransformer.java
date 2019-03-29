package com.decathlon.ara.service.transformer;

import com.decathlon.ara.ci.bean.QualityThreshold;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import liquibase.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
                log.error("Cannot parse qualityThresholds: {} - Cause : {}", jsonQualityThreshold, e.getMessage());
                log.debug("Full error is {}", e);
                return new HashMap<>();
            }
        }
        return result;
    }
}
