package com.decathlon.ara.web.rest;

import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.service.dto.functionality.FunctionalityDTO;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

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
