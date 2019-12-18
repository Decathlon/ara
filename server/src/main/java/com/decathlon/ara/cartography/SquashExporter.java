package com.decathlon.ara.cartography;

import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SquashExporter is an Exporter which serialize the functionalities in order to make them importable in SquashTM.
 *
 * @author Sylvain Nieuwlandt
 * @since 4.1.0
 */
public class SquashExporter extends Exporter {
    @Override
    public String getName() {
        return "SquashTM";
    }

    @Override
    public String getDescription() {
        return "Export this cartography to import it as requirements in SquashTM";
    }

    @Override
    public String getFormat() {
        return "xls";
    }

    @Override
    public byte[] generate(List<FunctionalityDTO> functionalities, Map<String, String> requiredInfos) {
        // TODO
        return new byte[0];
    }

    @Override
    public List<ExportField> listRequiredFields() {
        List<ExportField> fields = new ArrayList<>();
        fields.add(new ExportField("squash_project_name", "Targeted project's name", "string",
                "The name of targeted project in SquashTM. Note that the project must be created before doing the import."));
        fields.add(new ExportField("squash_user", "Targeted User's name", "string",
                "The name of the user whom will act as the creator of the requirements in SquashTM. Note that the user must exist before doing the import."));
        fields.add(new ExportField("squash_sev_critical", "Critical severity", "string",
                "The severity in this ARA project, which correspond to a Critical severity in SquashTM."));
        fields.add(new ExportField("squash_sev_major", "Major severity", "string",
                "The severity in this ARA project, which correspond to a Major severity in SquashTM."));
        fields.add(new ExportField("squash_sev_minor", "Minor severity", "string",
                "The severity in this ARA project, which correspond to a Minor severity in SquashTM."));
        return fields;
    }
}
