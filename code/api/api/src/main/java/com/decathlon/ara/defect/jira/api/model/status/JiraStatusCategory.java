/******************************************************************************
 * Copyright (C) 2020 by the ARA Contributors                                 *
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

package com.decathlon.ara.defect.jira.api.model.status;

public class JiraStatusCategory {

    public static final String UNDEFINED = "undefined";
    public static final String NEW = "new";
    public static final String DONE = "done";
    public static final String INDETERMINATE = "indeterminate";

    private String self;

    private Integer id;

    private String key;

    private String colorName;

    private String name;

    public static String getUndefined() {
        return UNDEFINED;
    }

    public static String getNew() {
        return NEW;
    }

    public static String getDone() {
        return DONE;
    }

    public static String getIndeterminate() {
        return INDETERMINATE;
    }

    public String getSelf() {
        return self;
    }

    public Integer getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getColorName() {
        return colorName;
    }

    public String getName() {
        return name;
    }
}
