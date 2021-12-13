package com.decathlon.ara.scenario.common.strategy;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.scenario.common.indexer.ScenariosIndexer;
import com.decathlon.ara.scenario.cucumber.indexer.CucumberScenariosIndexer;
import com.decathlon.ara.scenario.cypress.indexer.CypressScenariosIndexer;
import com.decathlon.ara.scenario.generic.indexer.GenericScenariosIndexer;
import com.decathlon.ara.scenario.postman.indexer.PostmanScenariosIndexer;

@Component
public class ScenariosIndexerStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(ScenariosIndexerStrategy.class);

    private final GenericScenariosIndexer genericScenariosIndexer;

    private final PostmanScenariosIndexer postmanScenariosIndexerService;

    private final CucumberScenariosIndexer cucumberScenariosIndexerService;

    private final CypressScenariosIndexer cypressScenariosIndexer;

    public ScenariosIndexerStrategy(GenericScenariosIndexer genericScenariosIndexer,
            PostmanScenariosIndexer postmanScenariosIndexerService,
            CucumberScenariosIndexer cucumberScenariosIndexerService, CypressScenariosIndexer cypressScenariosIndexer) {
        this.genericScenariosIndexer = genericScenariosIndexer;
        this.postmanScenariosIndexerService = postmanScenariosIndexerService;
        this.cucumberScenariosIndexerService = cucumberScenariosIndexerService;
        this.cypressScenariosIndexer = cypressScenariosIndexer;
    }

    /**
     * Get the scenarios indexer from the technology
     * @param technology the technology
     * @return the matching scenarios indexer, if found
     */
    public Optional<ScenariosIndexer> getScenariosIndexer(Technology technology) {
        if (technology == null) {
            LOG.info("The technology can not be null");
            return Optional.empty();
        }
        switch (technology) {
            case GENERIC:
                return Optional.of(genericScenariosIndexer);
            case CUCUMBER:
                return Optional.of(cucumberScenariosIndexerService);
            case POSTMAN:
                return Optional.of(postmanScenariosIndexerService);
            case CYPRESS:
                return Optional.of(cypressScenariosIndexer);
            default:
                LOG.info("The technology {} is not handled yet. It may be a great feature request ;)", technology);
                return Optional.empty();
        }
    }
}
