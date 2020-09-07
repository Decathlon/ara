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

package com.decathlon.ara.web.rest;

import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;

@Disabled
@RunWith(Suite.class)
@Suite.SuiteClasses({
        FunctionalityResourceCreateIT.class,
        FunctionalityResourceDeleteIT.class,
        FunctionalityResourceGetAllIT.class,
        FunctionalityResourceGetScenariosIT.class,
        FunctionalityResourceMoveIT.class,
        FunctionalityResourceUpdatePropertiesIT.class
})
public class FunctionalityResourceITSuite {

    // Test suite to run all tests associated with FunctionalityResource at once

    static Long TEAM_ID_NOT_ASSIGNABLE_TO_FUNCTIONALITIES = Long.valueOf(101);

    static FunctionalityDTO functionality() {
        FunctionalityDTO functionality = new FunctionalityDTO();
        functionality.setType(FunctionalityType.FUNCTIONALITY.name());
        functionality.setName("A name");
        functionality.setTeamId(Long.valueOf(1));
        functionality.setSeverity("LOW");
        functionality.setCountryCodes("be");
        return functionality;
    }

    static FunctionalityDTO folder(String name) {
        return new FunctionalityDTO().withType(FunctionalityType.FOLDER.name()).withName(name);
    }

}
