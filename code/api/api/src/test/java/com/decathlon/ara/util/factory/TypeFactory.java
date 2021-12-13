package com.decathlon.ara.util.factory;

import com.decathlon.ara.domain.Source;
import com.decathlon.ara.domain.Type;
import com.decathlon.ara.util.TestUtil;

public class TypeFactory {

    private TypeFactory() {
    }

    public static Type get(String code) {
        return get(null, 0, code, null, false, false, null);
    }

    public static Type get(Long id, long projectId, String code) {
        return get(id, projectId, code, null, false, false, null);
    }

    public static Type get(Long id, long projectId, String code, String name, boolean isBrowser, boolean isMobile, Source source) {
        Type type = new Type();
        type.setId(id);
        type.setProjectId(projectId);
        TestUtil.setField(type, "code", code);
        TestUtil.setField(type, "name", name);
        TestUtil.setField(type, "isBrowser", isBrowser);
        TestUtil.setField(type, "isMobile", isMobile);
        type.setSource(source);
        return type;
    }

}
