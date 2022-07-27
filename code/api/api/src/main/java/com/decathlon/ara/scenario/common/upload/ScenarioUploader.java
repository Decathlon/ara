package com.decathlon.ara.scenario.common.upload;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.*;
import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.repository.*;
import com.decathlon.ara.scenario.cucumber.bean.Tag;
import com.decathlon.ara.scenario.cucumber.util.ScenarioExtractorUtil;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        List<Scenario> newScenarios = scenarioExtractor.get(source);

        // Check functionality IDs
        // (first get all functionalities with their remaining scenarios eagerly-fetched)
        Set<Functionality> functionalities = functionalityRepository.findAllByProjectIdAndType(projectId, FunctionalityType.FUNCTIONALITY);

        functionalities = deleteScenariosFromSameSource(source, functionalities);

        assignWrongFunctionalityIds(functionalities, newScenarios);
        assignWrongSeverityCode(getSeverityCodes(projectId), newScenarios);
        assignWrongCountryCodes(getCountryCodes(projectId), newScenarios);
        // Save the new scenarios
        newScenarios = scenarioRepository.saveAll(newScenarios);
        LOG.info("SCENARIO|{} scenarios updated for source {}", newScenarios.size(), sourceCode);

        // Re-assign new scenarios to functionalities
        assignCoverage(functionalities, newScenarios);
        computeAggregates(functionalities);
        functionalityRepository.saveAll(functionalities);
        LOG.info("SCENARIO|{} features updated for source {}", functionalities.size(), sourceCode);
        LOG.info("SCENARIO|Coverage complete!");
    }

    private Set<Functionality> deleteScenariosFromSameSource(Source source, Set<Functionality> functionalities) {
        var functionalitiesWithoutSourceScenarios = functionalities.stream()
                .map(f -> Pair.of(f, f.getScenarios().stream().filter(s -> !s.getSource().equals(source)).toList()))
                .map(p -> Pair.of(p.getFirst(), new TreeSet<>(p.getSecond())))
                .map(p -> {
                    var f = p.getFirst();
                    var s = p.getSecond();
                    f.setScenarios(s);
                    return f;
                })
                .toList();
        functionalitiesWithoutSourceScenarios = functionalityRepository.saveAll(functionalitiesWithoutSourceScenarios);
        scenarioRepository.deleteAllBySource(source);
        return new HashSet<>(functionalitiesWithoutSourceScenarios);
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
                .toList();
    }

    /**
     * @param countryCodes he countryCodes in which to assign the wrongCountryCodes
     * @param scenarios the new scenarios to append to matching functionalities
     */
    public void assignWrongCountryCodes(List<String> countryCodes, List<Scenario> scenarios) {
        for (Scenario scenario : scenarios) {
            StringBuilder builder = new StringBuilder();
            var scenarioCountryCodes = StringUtils.isNotBlank(scenario.getCountryCodes()) ? scenario.getCountryCodes() : "";
            for (String scenarioCountryCode : scenarioCountryCodes.split(Scenario.COUNTRY_CODES_SEPARATOR)) {
                if (!countryCodes.contains(scenarioCountryCode)) {
                    builder.append(builder.length() == 0 ? "" : ",").append(scenarioCountryCode);
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
     * @param functionalities for each of them, if it is not a folder, update the coverage counts (covered & ignored) and coverage per source and ignore state
     */
    private static void computeAggregates(Collection<Functionality> functionalities) {
        functionalities.forEach(ScenarioUploader::computeAggregates);
    }

    /**
     * @param functionality update the coverage counts (covered & ignored) and coverage per source and ignore state
     */
    private static void computeAggregates(Functionality functionality) {
        var coverageAggregates = getCoverageAggregatesFromFunctionality(functionality);
        var coverAggregate = coverageAggregates.get(false);
        var ignoreAggregate = coverageAggregates.get(true);
        var coverageNumbers = getCoverageNumbersFromFunctionality(functionality);
        var coverNumber = coverageNumbers.containsKey(false) ? coverageNumbers.get(false).intValue() : 0;
        var ignoreNumber = coverageNumbers.containsKey(true) ? coverageNumbers.get(true).intValue() : 0;

        functionality.setCoveredScenarios(coverNumber);
        functionality.setIgnoredScenarios(ignoreNumber);

        functionality.setCoveredCountryScenarios(coverAggregate);
        functionality.setIgnoredCountryScenarios(ignoreAggregate);
    }

    /**
     * Get aggregates displaying the scenarios distribution:
     * - by state (ignored or covered), then
     * - by source code, then
     * - by country codes
     * e.g. if map.get(true) returns "source_1:*=1,xx=1,yy=1|source_2:*=2,xx=1,yy=2", it means that:
     * - it is an ignored state (map.get(true))
     * - the functionality has 1 source_1 ignored scenario total (*=1) in which
     *   - 1 concerns the country xx (xx=1)
     *   - 1 concerns the country yy (yy=1)
     * - it has 2 source_2 ignored scenarios total (*=2) in which
     *   - 1 concerns the country xx (xx=1)
     *   - 2 concerns the country yy (yy=2)
     * If null, then there is no scenarios covered or ignored
     * @param functionality the functionality to get the coverage aggregates from
     * @return ignored and covered aggregates
     */
    static Map<Boolean, String> getCoverageAggregatesFromFunctionality(Functionality functionality) {
        Function<List<Scenario>, String> partialAggregateFromScenarios = scenarios -> scenarios.stream()
                .map(Scenario::getCountryCodes)
                .filter(StringUtils::isNotBlank)
                .map(s -> s.split(Scenario.COUNTRY_CODES_SEPARATOR))
                .flatMap(Stream::of)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet()
                .stream()
                .map(e -> String.format("%s=%d", e.getKey(), e.getValue()))
                .sorted()
                .collect(Collectors.joining(","));
        Function<Map.Entry<String, List<Scenario>>, String> aggregateForSource = entry -> {
            var sourceCode = entry.getKey();
            var scenarios = entry.getValue();
            var partialScenarioAggregate = partialAggregateFromScenarios.apply(scenarios);
            var partialAggregate = StringUtils.isNotBlank(partialScenarioAggregate) ? String.format(",%s", partialScenarioAggregate) : "";
            return String.format("%s:%s=%d%s", sourceCode, TOTAL, scenarios.size(), partialAggregate);
        };
        return functionality.getScenarios()
                .stream()
                .collect(Collectors.groupingBy(Scenario::isIgnored))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e1 -> e1.getValue()
                        .stream()
                        .collect(Collectors.groupingBy(sc -> sc.getSource().getCode()))
                        .entrySet()
                        .stream()
                        .map(aggregateForSource::apply)
                        .collect(Collectors.joining("|"))
                ));
    }

    /**
     * Get coverage state distribution
     * e.g. map.get(true) returns the total ignored scenarios number whereas map.get(false) is about covered scenarios number
     * @param functionality the functionality
     * @return the coverage state distribution
     */
    static Map<Boolean, Long> getCoverageNumbersFromFunctionality(Functionality functionality) {
        return functionality.getScenarios()
                .stream()
                .collect(Collectors.groupingBy(Scenario::isIgnored, Collectors.counting()));
    }
}
