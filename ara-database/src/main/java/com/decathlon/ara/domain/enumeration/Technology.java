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

package com.decathlon.ara.domain.enumeration;

/**
 * Reporting technologies supported by ARA, for it to know how to index reports of a run.
 */
public enum Technology {

    /**
     * Cucumber job (no matter if it runs Selenium or other technologies like RestAssured or Karate): index its
     * report.json result.
     */
    CUCUMBER,

    /**
     * Job running one or more Postman collection(s) using Newman: parse all its reports/*.json reports.
     */
    POSTMAN

}
