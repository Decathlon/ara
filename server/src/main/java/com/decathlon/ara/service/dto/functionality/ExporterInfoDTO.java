package com.decathlon.ara.service.dto.functionality;

import com.decathlon.ara.cartography.ExportField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents the informations about a functionality exporter that the backend will expose.
 *
 * @author Sylvain Nieuwlandt
 * @version 4.1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExporterInfoDTO {
    private String id;
    private String name;
    private String description;
    private String format;
    private List<ExportField> requiredFields;
}
