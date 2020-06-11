package com.decathlon.ara.scenario.common.indexer;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Run;

import java.io.File;
import java.util.List;

public interface ScenariosIndexer {

    /**
     * Create the executed tests scenarios
     * @param parentFolder the run folder containing all the files needed to create the {@link List} of {@link ExecutedScenario}
     *                     For instance the folder api for POSTMAN or the folder firefox-desktop for CUCUMBER
     * @param run the {@link Run} for which the {@link List} of {@link ExecutedScenario} are requested.
     * @return the executed tests scenarios
     */
    List<ExecutedScenario> getExecutedScenarios(File parentFolder, Run run);
}
