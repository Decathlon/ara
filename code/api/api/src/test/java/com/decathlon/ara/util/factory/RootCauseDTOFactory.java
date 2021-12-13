package com.decathlon.ara.util.factory;

import com.decathlon.ara.service.dto.rootcause.RootCauseDTO;
import com.decathlon.ara.util.TestUtil;

public class RootCauseDTOFactory {

    private RootCauseDTOFactory() {
    }

    public static RootCauseDTO get(Long id) {
        return get(id, null);
    }

    public static RootCauseDTO get(String name) {
        return get(null, name);
    }

    public static RootCauseDTO get(Long id, String name) {
        RootCauseDTO rootCauseDTO = new RootCauseDTO(id);
        rootCauseDTO.setId(id);
        TestUtil.setField(rootCauseDTO, "name", name);
        return rootCauseDTO;
    }

}
