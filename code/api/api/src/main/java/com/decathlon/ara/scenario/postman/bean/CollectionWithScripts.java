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

package com.decathlon.ara.scenario.postman.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectionWithScripts {

    /**
     * Root folders of the tree of folders and requests of the Postman collection.<br>
     * Items INCLUDE scripts (memory intensive to store: use {@link Collection} if not needed).
     */
    private ItemWithScripts[] item;

    /**
     * Contains the name of the Postman collection.
     */
    private Info info;

    public ItemWithScripts[] getItem() {
        return item;
    }

    public Info getInfo() {
        return info;
    }

}
