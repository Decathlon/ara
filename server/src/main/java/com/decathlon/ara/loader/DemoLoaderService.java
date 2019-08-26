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

package com.decathlon.ara.loader;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Utility methods used by data loads for the the Demo project.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DemoLoaderService {

    /**
     * Replace all functionality placeholders by their real IDs.<br>
     * Eg. "Functionality {{F-A}}: Important Functionality" => "Functionality 42: Important Functionality"
     *
     * @param functionalityIds a map of letters (eg. "A" for the placeholder "{{F-A}}") as keys, and functionality IDs
     *                         as values
     * @param fileContent      a text file content where to replace all functionality placeholders
     * @return the modified fileContent with replaced placeholders
     */
    String replaceFunctionalityIdPlaceholders(Map<String, Long> functionalityIds, String fileContent) {
        String replaced = fileContent;
        for (Map.Entry<String, Long> entry : functionalityIds.entrySet()) {
            replaced = replaced.replace("{{F-" + entry.getKey() + "}}", entry.getValue().toString());
        }
        return replaced;
    }

}
