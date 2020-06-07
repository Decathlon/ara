package com.decathlon.ara.test.strategy;

import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.test.CucumberScenariosIndexer;
import com.decathlon.ara.test.PostmanScenariosIndexer;
import com.decathlon.ara.test.ScenariosIndexer;
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
            default:
                log.info("The technology {} is not handled yet. It may be a great feature request ;)", technology);
                return Optional.empty();
        }
    }
}
