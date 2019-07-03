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

package com.decathlon.ara.report.asset;

/**
 * Save (write to disk, upload to SSH, etc.) part of the data from Cucumber and Postman reports (Cucumber screenshots
 * and Postman HTTP logs), and return URLs where they can be accessed.
 */
public interface AssetService {

    /**
     * Save a Cucumber scenario screenshot: it is extracted from reports for it to be accessible by other applications
     * by a simple URL.
     *
     * @param screenshot   the PNG bytes of the screenshot
     * @param scenarioName the name of the scenario for which the screenshot was taken (date is prepended, and png
     *                     extension is appended to generate file name)
     * @return the complete URL of the file having been saved, or null if save failed
     */
    String saveScreenshot(byte[] screenshot, String scenarioName);

    /**
     * Save a Postman HTTP logs: they are extracted as an HTML from reports for them to be accessible by other
     * applications by a simple URL.
     *
     * @param html the HTML representing the HTTP logs
     * @return the complete URL of the file having been saved, or null if save failed
     */
    String saveHttpLogs(String html);

}
