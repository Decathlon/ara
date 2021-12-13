package com.decathlon.ara.service.dto.functionality;

import java.util.List;

import com.decathlon.ara.cartography.ExportField;

/**
 * Represents the informations about a functionality exporter that the backend will expose.
 *
 * @author Sylvain Nieuwlandt
 * @version 4.1.0
 */
public class ExporterInfoDTO {
    private String id;
    private String name;
    private String description;
    private String format;
    private List<ExportField> requiredFields;

    public ExporterInfoDTO() {
    }

    public ExporterInfoDTO(String id, String name, String description, String format,
            List<ExportField> requiredFields) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.format = format;
        this.requiredFields = requiredFields;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getFormat() {
        return format;
    }

    public List<ExportField> getRequiredFields() {
        return requiredFields;
    }
}
