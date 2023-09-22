package com.decathlon.ara.cartography;

import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * AraExporter is a Exporter which serialize the functionalities into a ARA-specific format (typically the JSON object
 * passed when the backend respond with all the functionalities).
 *
 * @author Sylvain Nieuwlandt
 * @since 4.1.0
 */
@Service
public class AraExporter extends Exporter {

    private static final AraCartographyMapper MAPPER = new AraCartographyMapper();

    @Override
    public String getName() {
        return "ARA";
    }

    @Override
    public String getDescription() {
        return "Export this cartography to import it in another ARA project";
    }

    @Override
    public String getFormat() {
        return "json";
    }

    @Override
    public byte[] generate(List<FunctionalityDTO> functionalities, Map<String, String> requiredInfos) {
        return MAPPER.asString(functionalities).getBytes(StandardCharsets.UTF_8);
    }
}
