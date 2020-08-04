package com.decathlon.ara.scenario.common.strategy;

import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.scenario.cucumber.indexer.CucumberScenariosIndexer;
import com.decathlon.ara.scenario.cypress.indexer.CypressScenariosIndexer;
import com.decathlon.ara.scenario.postman.indexer.PostmanScenariosIndexer;
import com.decathlon.ara.scenario.common.indexer.ScenariosIndexer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ScenariosIndexerStrategy {

    @NonNull
    private final PostmanScenariosIndexer postmanScenariosIndexerService;

    @NonNull
    private final CucumberScenariosIndexer cucumberScenariosIndexerService;

    @NonNull
    private final CypressScenariosIndexer cypressScenariosIndexer;

    /**
     * Get the scenarios indexer from the technology
     * @param technology the technology
     * @return the matching scenarios indexer, if found
     */
    public Optional<ScenariosIndexer> getScenariosIndexer(Technology technology) {
        if (technology == null) {
            log.info("The technology can not be null");
            return Optional.empty();
        }
        switch (technology) {
            case CUCUMBER:
                return Optional.of(cucumberScenariosIndexerService);
            case POSTMAN:
                return Optional.of(postmanScenariosIndexerService);
            case CYPRESS:
                return Optional.of(cypressScenariosIndexer);
            default:
                log.info("The technology {} is not handled yet. It may be a great feature request ;)", technology);
                return Optional.empty();
        }
    }
}
