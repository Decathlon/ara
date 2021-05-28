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

package com.decathlon.ara.scenario.cucumber.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feature {

    private String id;
    private String name;
    private String uri;
    private String description;
    private String keyword;
    private Integer line;
    private Comment[] comments = new Comment[0];
    private Element[] elements = new Element[0];
    private Tag[] tags = new Tag[0];

    public String getReportFileName() {
        // Simplified version of
        // net.masterthought.cucumber.json.Feature.setReportFileName(int jsonFileNo, Configuration configuration) :
        // * no support for multiple report.json files (we do not use that)
        // * nor for parallel execution by official Maven plugin (we use our Cucumber fork managing parallelism more efficiently and effectively)
        return uri.replaceAll("[^\\d\\w]", "-") + ".html";
    }

}
