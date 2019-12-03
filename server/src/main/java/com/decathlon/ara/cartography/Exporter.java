package com.decathlon.ara.cartography;

import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;

/**
 * Describe the API to implement for exporting a ARA Cartography to a file.
 *
 * @author Sylvain Nieuwlandt
 * @since 4.1.0
 */
public abstract class Exporter {
    /**
     * The name of the exporter to display in ARA.
     *
     * @return the name of the exporter
     */
    public abstract String getName();

    /**
     * The description of the exporter to display in ARA.
     *
     * @return the description of the exporter
     */
    public abstract String getDescription();

    /**
     * The format of the export used by the implemented exporter.
     *
     * @return the format of the export
     */
    public abstract String getFormat();

    public abstract byte[] generate(List<FunctionalityDTO> functionalities);

    /**
     * Check if the implemented exporter is suitable for the given export name.
     *
     * @param exportName the name of the wanted exporter.
     * @return true if the implemented exporter is suitable for the given name, false if not.
     */
    public boolean suitableFor(String exportName) {
        if (null == exportName) {
            return false;
        }
        return exportName.equals(this.getId());
    }

    /**
     * Return the id of the implemented exporter.
     *
     * The id is the exporter's name in lowercase, with all spaces replaced by a '_' char.
     *
     * @return the unique id of the exporter.
     */
    public final String getId() {
        return this.getName().toLowerCase().replace(" ", "_");
    }
}
