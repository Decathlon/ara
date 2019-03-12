package com.decathlon.ara.postman.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {

    /**
     * UUID of the folder or request, used to map a request with its execution and failure(s).
     */
    private String id;

    /**
     * Displayed name of the folder or request.
     */
    private String name;

    /**
     * Description of the request that needs to be issued (not for a folder):
     * it has variable names in it, and the corresponding Execution has variable values at the time the request did execute.
     */
    private Request request;

    /**
     * Sub-folders and sub-requests of a folder (not for a request).
     */
    @JsonProperty("item")
    private Item[] children;

    /**
     * True if the item represents a sub-folder (not a root-folder, nor a leaf-request) in a Postman collection (not in a Newman report).<br>
     * In Postman collections, item._postman_isSubFolder is true for sub-folders (root items are folders).<br>
     * In Newman reports, item._.postman_isSubFolder does the same thing, but we do not parse it: an item without execution (including folders) is not indexed.
     */
    @JsonProperty("_postman_isSubFolder")
    private boolean isSubFolder;

}
