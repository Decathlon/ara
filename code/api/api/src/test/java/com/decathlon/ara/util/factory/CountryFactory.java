package com.decathlon.ara.util.factory;

import com.decathlon.ara.domain.Country;
import com.decathlon.ara.util.TestUtil;

public class CountryFactory {

    private CountryFactory() {
    }

    public static Country get(String code) {
        return get(code, null);
    }

    public static Country get(String code, String name) {
        return get(null, 0l, code, name);
    }

    public static Country get(Long id, long projectId, String code, String name) {
        Country country = new Country();
        country.setId(id);
        country.setProjectId(projectId);
        TestUtil.setField(country, "code", code);
        TestUtil.setField(country, "name", name);
        return country;
    }

}
