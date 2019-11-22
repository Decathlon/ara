package com.decathlon.ara.cartography;

import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;

import java.util.List;

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
    public byte[] generate(List<FunctionalityDTO> functionalities) {
        // TODO
        return new byte[0];
    }
}
