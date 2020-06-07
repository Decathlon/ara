package com.decathlon.ara.test;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Run;

import java.io.File;
import java.util.List;

public interface ScenariosIndexer {

    List<ExecutedScenario> getExecutedScenarios(File parentFolder, Run run);
}
