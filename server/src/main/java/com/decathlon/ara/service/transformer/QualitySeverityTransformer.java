package com.decathlon.ara.service.transformer;

import com.decathlon.ara.service.dto.quality.QualitySeverityDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            = new TypeReference<List<QualitySeverityDTO>>() {};

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
            log.error("Cannot parse qualitySeverities: {} - Cause : {}", jsonQualitySeverities, e.getMessage());
            log.debug("Full error : {}", e);
            return new ArrayList<>();
        }
    }
}
