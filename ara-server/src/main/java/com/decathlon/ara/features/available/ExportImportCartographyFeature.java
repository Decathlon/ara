package com.decathlon.ara.features.available;

import com.decathlon.ara.features.IFeature;

/**
 * ExportImportCartographyFeature is the Feature flipping element which enables or not the ability to export and/or
 * import a list of functionalities in the Cartography section.
 *
 * @author Sylvain Nieuwlandt
 * @since 4.1.0
 */
public class ExportImportCartographyFeature implements IFeature {
    @Override
    public String getCode() {
        return "xprt-mprt-crtg";
    }

    @Override
    public String getName() {
        return "Export/Import a Cartography";
    }

    @Override
    public String getDescription() {
        return "Enable Export and/or Import of a Cartography (filtered or not) of functionalities.";
    }
}
