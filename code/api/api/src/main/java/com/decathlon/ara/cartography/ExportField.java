package com.decathlon.ara.cartography;

/**
 * Holds the informations about a needed field for the functionality export behavior.
 */
public class ExportField {
    /** The id of the field used in http communication */
    String id;
    /** The name of the field to display to the user */
    String name;
    /** The type of the field, to find the input to display to the user */
    String type;
    /** The description of the field to help the user */
    String description;

    public ExportField(String id, String name, String type, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
