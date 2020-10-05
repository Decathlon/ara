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

package com.decathlon.ara.defect;

import com.decathlon.ara.ci.util.FetchException;
import com.decathlon.ara.defect.bean.Defect;
import com.decathlon.ara.service.dto.setting.SettingDTO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

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
    public boolean isValidId(String id) {
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
