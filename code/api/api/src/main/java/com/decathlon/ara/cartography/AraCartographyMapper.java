package com.decathlon.ara.cartography;

import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * AraCartographyMapper handles all serialization/deserialization of ARA functionalities to export/import feature.
 *
 * @author  Sylvain Nieuwlandt
 * @since 4.1.0
 */
@Slf4j
public class AraCartographyMapper {

    private static final TypeReference<List<FunctionalityDTO>> TYPE_REFERENCE = new TypeReference<List<FunctionalityDTO>>() {};

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Serialize the given list of functionalities into a String representation (typically JSON).
     *
     * If an error occurs during the Serialization process, then log the exception in error level and return an empty
     * JSON object (which makes it easy to differentiate from an empty functionalities list which will return an empty
     * JSON array).
     *
     * @param functionalities the functionalities to serialize
     * @return the String representation of all the functionalities.
     */
    String asString(List<FunctionalityDTO> functionalities) {
        try {
            return objectMapper.writeValueAsString(functionalities);
        } catch (JsonProcessingException ex) {
            log.error("FEATURE|Unable to serialize the wanted cartography into an ARA export.", ex);
            return "{}";
        }
    }

    /**
     * Unserialize the given String into a list of Functionality objects.
     *
     * @param jsonRepresentation the serialized string (mostly JSON)
     * @return the List of functionalities or an empty list if the JSON is empty or malformed.
     */
    public List<FunctionalityDTO> asFunctionalities(String jsonRepresentation) {
        try {
            return objectMapper.readValue(jsonRepresentation, TYPE_REFERENCE);
        } catch (IOException ex) {
            log.error("FEATURE|Unable to deserialize the given JSON into a list of Functionalities.", ex);
            return new ArrayList<>();
        }
    }
}
