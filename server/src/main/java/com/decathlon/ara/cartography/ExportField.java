package com.decathlon.ara.cartography;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Holds the informations about a needed field for the functionality export behavior.
 */
@Data
@AllArgsConstructor
public class ExportField {
    /** The id of the field used in http communication */
    String id;
    /** The name of the field to display to the user */
    String name;
    /** The type of the field, to find the input to display to the user */
    String type;
    /** The description of the field to help the user */
    String description;
}
