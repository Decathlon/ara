package com.decathlon.ara.service.mapper;

import com.decathlon.ara.common.NotGonnaHappenException;
import com.decathlon.ara.ci.bean.QualityThreshold;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Mapper for a serialized JSON String and its deserialized Map&lt;String, QualityThreshold&gt;.
 */
@Mapper
@Slf4j
public abstract class QualityThresholdMapper {

    private static final TypeReference<Map<String, QualityThreshold>> TYPE_REFERENCE_TO_MAP_STRING_QUALITY_THRESHOLD = new TypeReference<Map<String, QualityThreshold>>() {
    };

    @Autowired
    private ObjectMapper objectMapper;

    public String map(Map<String, QualityThreshold> thresholds) {
        if (thresholds == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(thresholds);
        } catch (JsonProcessingException e) {
            throw new NotGonnaHappenException("JSON serializing should not have failed when serializing to a String", e);
        }
    }

    /**
     *
     * @param json
     * @return
     * @deprecated
     * @see com.decathlon.ara.service.transformer.QualityThresholdTransformer#toMap(String)
     */
    @Deprecated
    public Map<String, QualityThreshold> map(String json) {
        if (StringUtils.isEmpty(json)) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(json, TYPE_REFERENCE_TO_MAP_STRING_QUALITY_THRESHOLD);
        } catch (IOException e) {
            log.error("Cannot parse qualityThresholds: {}", json, e);
            return new HashMap<>();
        }
    }

}
