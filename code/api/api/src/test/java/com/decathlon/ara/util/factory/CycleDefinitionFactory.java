package com.decathlon.ara.util.factory;

import com.decathlon.ara.domain.CycleDefinition;
import com.decathlon.ara.util.TestUtil;

public class CycleDefinitionFactory {

    public static CycleDefinition get(long projectId) {
        return get(null, projectId, null, null, 0);
    }

    public static CycleDefinition get(Long id, long projectId) {
        return get(id, projectId, null, null, 0);
    }

    public static CycleDefinition get(Long id, long projectId, String branch, String name, int branchPosition) {
        CycleDefinition cycleDefinition = new CycleDefinition();
        TestUtil.setField(cycleDefinition, "id", id);
        cycleDefinition.setProjectId(projectId);
        TestUtil.setField(cycleDefinition, "branch", branch);
        TestUtil.setField(cycleDefinition, "name", name);
        cycleDefinition.setBranchPosition(branchPosition);
        return cycleDefinition;
    }

}
