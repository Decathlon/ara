/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

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
public class ItemWithScripts {

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
     * Sub-folders and sub-requests of a folder (not for a request).<br>
     * INCLUDING scripts (memory intensive to store: use {@link Item} if not needed).
     */
    @JsonProperty("item")
    private ItemWithScripts[] children;

    /**
     * True if the item represents a sub-folder (not a root-folder, nor a leaf-request) in a Postman collection (not in a Newman report).<br>
     * In Postman collections, item._postman_isSubFolder is true for sub-folders (root items are folders).<br>
     * In Newman reports, item._.postman_isSubFolder does the same thing, but we do not parse it: an item without execution (including folders) is not indexed.
     */
    @JsonProperty("_postman_isSubFolder")
    private boolean isSubFolder;

    @JsonProperty("event")
    private Event[] events;

}
