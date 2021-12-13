package com.decathlon.ara.scenario.common.upload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.domain.Severity;
import com.decathlon.ara.domain.Source;
import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.repository.CountryRepository;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.repository.ScenarioRepository;
import com.decathlon.ara.repository.SeverityRepository;
import com.decathlon.ara.repository.SourceRepository;
import com.decathlon.ara.scenario.cucumber.bean.Tag;
import com.decathlon.ara.scenario.cucumber.util.ScenarioExtractorUtil;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;

@Component
@Transactional
public class ScenarioUploader {

    private static final Logger LOG = LoggerFactory.getLogger(ScenarioUploader.class);
    private static final String TOTAL = "*";

    private final ScenarioRepository scenarioRepository;

    private final FunctionalityRepository functionalityRepository;

    private final SourceRepository sourceRepository;

    private final EntityManager entityManager;

    private final SeverityRepository severityRepository;

    private final CountryRepository countryRepository;

    public ScenarioUploader(ScenarioRepository scenarioRepository, FunctionalityRepository functionalityRepository,
            SourceRepository sourceRepository, EntityManager entityManager, SeverityRepository severityRepository,
            CountryRepository countryRepository) {
        this.scenarioRepository = scenarioRepository;
        this.functionalityRepository = functionalityRepository;
        this.sourceRepository = sourceRepository;
        this.entityManager = entityManager;
        this.severityRepository = severityRepository;
        this.countryRepository = countryRepository;
    }

    public void processUploadedContent(long projectId, String sourceCode, Technology expectedTechnology, ScenarioListSupplier scenarioExtractor) throws BadRequestException {
        LOG.info("SCENARIO|Coverage: Preparing to match scenarios and features (source: {})", sourceCode);
        Source source = sourceRepository.findByProjectIdAndCode(projectId, sourceCode);
        if (source == null) {
            LOG.error("SCENARIO|Cannot match scenarios with features because the source {} was not found", sourceCode);
            throw new NotFoundException(Messages.NOT_FOUND_SOURCE, Entities.SOURCE);
        }
        if (source.getTechnology() != expectedTechnology) {
            final String message = String.format(Messages.RULE_SCENARIO_UPLOAD_TO_WRONG_TECHNOLOGY, expectedTechnology, source.getTechnology());
            LOG.error("SCENARIO|Cannot match scenarios with features because the technologies don't match (source {})", sourceCode);
            LOG.error("SCENARIO|{}", message);
            throw new BadRequestException(message, Entities.SCENARIO, "wrong_technology");
        }

        // Remove the previous scenarios from the same source => the database will remove the associations between functionalities and these scenarios
        var allScenarios = scenarioRepository.findAllBySourceId(source.getId());
        LOG.debug("SCENARIO|{} scenarios found for source {}", allScenarios.size(), sourceCode);
        allScenarios.forEach(this::removeScenarioAssociationSafely);
        scenarioRepository.deleteAll(allScenarios);

        // Let Hibernate make the DELETE SQL statements now, because we will add the same scenarios below: don't mess with old and new instances
        entityManager.flush();
        entityManager.clear();

        List<Scenario> newScenarios = scenarioExtractor.get(source);

        // Check functionality IDs
        // (first get all functionalities with their remaining scenarios eagerly-fetched)
        Set<Functionality> functionalities = functionalityRepository.findAllByProjectIdAndType(projectId, FunctionalityType.FUNCTIONALITY);
        assignWrongFunctionalityIds(functionalities, newScenarios);
        assignWrongSeverityCode(getSeverityCodes(projectId), newScenarios);
        assignWrongCountryCodes(getCountryCodes(projectId), newScenarios);
        // Save the new scenarios
        newScenarios = scenarioRepository.saveAll(newScenarios);
        LOG.info("SCENARIO|{} scenarios updated for source {}", newScenarios.size(), sourceCode);
        entityManager.flush();

        // Re-assign new scenarios to functionalities
        assignCoverage(functionalities, newScenarios);
        computeAggregates(functionalities);
        functionalityRepository.saveAll(functionalities);
        LOG.info("SCENARIO|{} features updated for source {}", functionalities.size(), sourceCode);
        LOG.info("SCENARIO|Coverage complete!");
    }

    public void removeScenarioAssociationSafely(Scenario scenario) {
        var scenarioFunctionalities = scenario.getFunctionalities();
        while (!scenarioFunctionalities.isEmpty()) {
            var initialSize = scenarioFunctionalities.size();
            scenarioFunctionalities.stream()
                    .findFirst()
                    .ifPresent(scenario::removeFunctionality);
            if (scenarioFunctionalities.size() >= initialSize) {
                // Check implementation of removeFunctionality decrease the size of functionalities set
                throw new IllegalStateException(
                        "Error during scenario-functionality link deletion: prevent infinite loop");
            }
        }
    }

    @FunctionalInterface
    public interface ScenarioListSupplier {
        List<Scenario> get(Source source) throws BadRequestException;
    }

    /**
     * @param functionalities the functionalities in which to assign the wrongFunctionalityIds, if any
     * @param scenarios       the new scenarios to append to matching functionalities (excluding folders)
     */
    private static void assignWrongFunctionalityIds(Collection<Functionality> functionalities, List<Scenario> scenarios) {
        for (Scenario scenario : scenarios) {
            List<String> wrongFunctionalityIds = ScenarioExtractorUtil.extractWrongFunctionalityIds(scenario.getName(), functionalities);
            if (wrongFunctionalityIds.isEmpty()) {
                scenario.setWrongFunctionalityIds(null);
            } else {
                scenario.setWrongFunctionalityIds(String.join("\n", wrongFunctionalityIds));
            }
        }
    }

    /**
     * @param severityCodes the severityCodes in which to assign the wrongSeverityCode
     * @param scenarios  the new scenarios to append to matching functionalities
     */
    public void assignWrongSeverityCode(List<String> severityCodes, List<Scenario> scenarios) {
        for (Scenario scenario : scenarios) {
            if (severityCodes.contains(scenario.getSeverity())) {
                scenario.setWrongSeverityCode(null);
            } else {
                scenario.setWrongSeverityCode(scenario.getSeverity());
            }
        }
    }

    private List<String> getSeverityCodes(long projectId) {
        return severityRepository.findAllByProjectIdOrderByPosition(projectId).stream()
                .map(Severity::getCode)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * @param countryCodes he countryCodes in which to assign the wrongCountryCodes
     * @param scenarios the new scenarios to append to matching functionalities
     */
    public void assignWrongCountryCodes(List<String> countryCodes, List<Scenario> scenarios) {
        for (Scenario scenario : scenarios) {
            StringBuilder builder = new StringBuilder();
            for (String countryCode : scenario.getCountryCodes().split(Scenario.COUNTRY_CODES_SEPARATOR)) {
                if (!countryCodes.contains(countryCode)) {
                    builder.append(builder.length() == 0 ? "" : ",").append(countryCode);
                }
            }
            scenario.setWrongCountryCodes(builder.length() == 0 ? null : builder.toString());
        }
    }

    private List<String> getCountryCodes(long projectId) {
        List<Country> countries = countryRepository.findAllByProjectIdOrderByCode(projectId);
        List<String> result = new ArrayList<>();

        for (Country country : countries) {
            result.add(country.getCode());
        }
        result.add(Tag.COUNTRY_ALL);

        return result;
    }

    /**
     * @param functionalities the functionalities in which to append matching scenarios for the list of new scenarios
     * @param newScenarios    the new scenarios to append to matching functionalities (excluding folders)
     */
    private static void assignCoverage(Collection<Functionality> functionalities, List<Scenario> newScenarios) {
        for (Functionality functionality : functionalities) {
            for (Scenario scenario : newScenarios) {
                if (ScenarioExtractorUtil.extractFunctionalityIds(scenario.getName()).contains(functionality.getId())) {
                    functionality.addScenario(scenario);
                }
            }
        }
    }

    /**
     * @param functionalities for each of them, if it is not a folder, update the coverage counts (normal & ignored) and coverage per source and ignore state
     */
    private static void computeAggregates(Collection<Functionality> functionalities) {
        for (Functionality functionality : functionalities) {
            final Set<Scenario> scenarios = functionality.getScenarios();

            functionality.setCoveredScenarios(computeCount(scenarios, false));
            functionality.setIgnoredScenarios(computeCount(scenarios, true));

            functionality.setCoveredCountryScenarios(computeGlobalAggregate(scenarios, false));
            functionality.setIgnoredCountryScenarios(computeGlobalAggregate(scenarios, true));
        }
    }

    /**
     * @param scenarios a list of scenarios to count for the ignore state
     * @param ignored   the ignore state for a scenario to be counted
     * @return the count of scenarios with the given ignore state
     */
    private static Integer computeCount(Set<Scenario> scenarios, boolean ignored) {
        return Integer.valueOf((int) scenarios.stream().filter(s -> s.isIgnored() == ignored).count());
    }

    /**
     * For a given ignore state, count matching scenarios per source and country.
     *
     * @param scenarios a list of scenarios to count per source and country for the ignore state
     * @param ignored   the ignore state for a scenario to match
     * @return eg. "API:cn=3,nl=1|WEB:be=2" or null if no coverage was found for the ignore state
     */
    private static String computeGlobalAggregate(Set<Scenario> scenarios, boolean ignored) {
        List<Source> sortedDistinctSources = scenarios.stream()
                .map(Scenario::getSource)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        String coverage = null;
        for (Source source : sortedDistinctSources) {
            String aggregate = computeGlobalAggregate(scenarios, source, ignored);
            if (aggregate != null) {
                coverage = (coverage == null ? "" : coverage + "|") + aggregate;
            }
        }
        return coverage;
    }

    /**
     * For a given source and an ignore state, count matching scenarios per country.
     *
     * @param scenarios a list of scenarios to count per source for the given ignore state
     * @param source    the source for a scenario to match
     * @param ignored   the ignore state for a scenario to match
     * @return eg. "API:cn=3,nl=1" or null if no coverage was found for the source and ignore state
     */
    private static String computeGlobalAggregate(Set<Scenario> scenarios, Source source, boolean ignored) {
        Map<String, Integer> countryCoverage = new TreeMap<>(); // TreeMap: ordered by key alphabetically

        for (Scenario scenario : scenarios) {
            if (scenario.getSource().equals(source) && scenario.isIgnored() == ignored) {
                increment(countryCoverage, TOTAL);
                // Still count scenarios without country codes
                for (String countryCode : scenario.getCountryCodes().split(Scenario.COUNTRY_CODES_SEPARATOR)) {
                    increment(countryCoverage, countryCode);
                }
            }
        }

        if (countryCoverage.isEmpty()) {
            return null;
        }

        StringBuilder aggregate = new StringBuilder(source.getCode());
        boolean first = true;
        for (Map.Entry<String, Integer> entry : countryCoverage.entrySet()) {
            aggregate.append(first ? ':' : ',').append(entry.getKey()).append('=').append(entry.getValue().toString());
            first = false;
        }
        return aggregate.toString();
    }

    /**
     * Increment the <code>key</code> in the map of <code>counts</code> (the key may not exist beforehand).
     *
     * @param counts the map containing counts by a String key
     * @param key    the key to increment, created before increment if not existing yet
     */
    private static void increment(Map<String, Integer> counts, String key) {
        Integer oldValue = counts.get(key);
        Integer newValue = Integer.valueOf(oldValue == null ? 1 : oldValue.intValue() + 1);
        counts.put(key, newValue);
    }
}
