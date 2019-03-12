package com.decathlon.ara.defect;

import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.defect.bean.Defect;
import com.decathlon.ara.service.dto.setting.SettingDTO;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * A test-scope-only defect adapter for integration tests.
 */
@Service
public class TestDefectAdapter implements DefectAdapter {

    @Override
    public List<Defect> getStatuses(long projectId, List<String> ids)
            throws FetchException /* KEEP IT: this adapter will sometimes be mocked throwing a FetchException */ {
        return null;
    }

    @Override
    public List<Defect> getChangedDefects(long projectId, Date since) {
        return null;
    }

    @Override
    public boolean isValidId(long projectId, String id) {
        return false;
    }

    @Override
    public String getIdFormatHint(long projectId) {
        return null;
    }

    @Override
    public String getCode() {
        return "testAdapter";
    }

    @Override
    public String getName() {
        return "Test of defect adapter";
    }

    @Override
    public List<SettingDTO> getSettingDefinitions() {
        return Collections.emptyList();
    }

}
