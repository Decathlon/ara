package com.decathlon.ara.service;

import com.decathlon.ara.Entities;
import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.Country;
import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.domain.Source;
import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.domain.enumeration.Technology;
import com.decathlon.ara.domain.projection.IgnoredScenario;
import com.decathlon.ara.domain.projection.ScenarioIgnoreCount;
import com.decathlon.ara.postman.service.PostmanScenarioIndexerService;
import com.decathlon.ara.report.bean.Feature;
import com.decathlon.ara.report.bean.Tag;
import com.decathlon.ara.report.util.CucumberReportUtil;
import com.decathlon.ara.report.util.ScenarioExtractorUtil;
import com.decathlon.ara.repository.CountryRepository;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.repository.ScenarioRepository;
import com.decathlon.ara.repository.SourceRepository;
import com.decathlon.ara.service.dto.ignore.ScenarioIgnoreCountDTO;
import com.decathlon.ara.service.dto.ignore.ScenarioIgnoreFeatureDTO;
import com.decathlon.ara.service.dto.ignore.ScenarioIgnoreSeverityDTO;
import com.decathlon.ara.service.dto.ignore.ScenarioIgnoreSourceDTO;
import com.decathlon.ara.service.dto.scenario.ScenarioSummaryDTO;
import com.decathlon.ara.service.dto.severity.SeverityDTO;
import com.decathlon.ara.service.dto.source.SourceDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.mapper.ScenarioSummaryMapper;
import com.decathlon.ara.service.mapper.SourceMapper;
import com.decathlon.ara.domain.Severity;
import com.decathlon.ara.repository.SeverityRepository;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

/**
 * Service for managing Scenario.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ScenarioService {

    private static final String TOTAL = "*";

    @NonNull
    private final ScenarioRepository scenarioRepository;

    @NonNull
    private final FunctionalityRepository functionalityRepository;

    @NonNull
    private final SourceRepository sourceRepository;

    @NonNull
    private final SourceMapper sourceMapper;

    @NonNull
    private final ScenarioSummaryMapper scenarioSummaryMapper;

    @NonNull
    private final EntityManager entityManager;

    @NonNull
    private final SeverityService severityService;

    @NonNull
    private final PostmanScenarioIndexerService postmanScenarioIndexerService;

    @NonNull
    private final SeverityRepository severityRepository;

    @NonNull
    private final CountryRepository countryRepository;

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

    /**
     * Upload the Cucumber scenario set of a test type.
     *
     * @param projectId  the ID of the project in which to work
     * @param sourceCode the source-code determining the location of the files that are uploaded
     * @param json       the report.json file as generated by a cucumber --dry-run
     * @throws BadRequestException if the source cannot be found, the source code is not using CUCUMBER technology, or something goes wrong while parsing the report content
     */
    public void uploadCucumber(long projectId, String sourceCode, String json) throws BadRequestException {
        processUploadedContent(projectId, sourceCode, Technology.CUCUMBER, source -> {
            // Extract and save scenarios of the source from the report.json
            List<Feature> features;
            try {
                features = CucumberReportUtil.parseReportJson(json);
            } catch (IOException e) {
                log.error("Cannot parse uploaded Cucumber report.json", e);
                throw new BadRequestException("Cannot parse uploaded Cucumber report.json", Entities.SCENARIO, "cannot_parse_report_json");
            }
            return ScenarioExtractorUtil.extractScenarios(source, features);
        });
    }

    /**
     * Upload the Postman collection set of a test type.
     *
     * @param projectId  the ID of the project in which to work
     * @param sourceCode the source-code determining the location of the files that are uploaded
     * @param zipFile    the ZIP file containing .json files representing Postman collection files
     * @throws BadRequestException if the source cannot be found, the source code is not using POSTMAN technology, or something goes wrong while parsing the collection contents
     */
    public void uploadPostman(long projectId, String sourceCode, File zipFile) throws BadRequestException {
        processUploadedContent(projectId, sourceCode, Technology.POSTMAN, source -> {
            try {
                return postmanScenarioIndexerService.extractScenarios(source, zipFile);
            } catch (IOException e) {
                log.error("Cannot parse uploaded Postman collections ZIP", e);
                throw new BadRequestException("Cannot parse uploaded Postman collections ZIP", Entities.SCENARIO, "cannot_parse_zip");
            }
        });
    }

    private void processUploadedContent(long projectId, String sourceCode, Technology expectedTechnology, ScenarioListSupplier scenarioExtractor) throws BadRequestException {
        Source source = sourceRepository.findByProjectIdAndCode(projectId, sourceCode);
        if (source == null) {
            throw new NotFoundException(Messages.NOT_FOUND_SOURCE, Entities.SOURCE);
        }
        if (source.getTechnology() != expectedTechnology) {
            final String message = String.format(Messages.RULE_SCENARIO_UPLOAD_TO_WRONG_TECHNOLOGY, expectedTechnology, source.getTechnology());
            throw new BadRequestException(message, Entities.SCENARIO, "wrong_technology");
        }

        // Remove the previous scenarios from the same source => the database will remove the associations between functionalities and these scenarios
        scenarioRepository.deleteAll(scenarioRepository.findAllBySourceId(source.getId()));
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
        entityManager.flush();

        // Re-assign new scenarios to functionalities
        assignCoverage(functionalities, newScenarios);
        computeAggregates(functionalities);
        functionalityRepository.saveAll(functionalities);
    }

    /**
     * @param projectId the ID of the project in which to work
     * @return all scenarios that have no associated functionalities or have wrong or nonexistent functionality identifier
     */
    public List<ScenarioSummaryDTO> findAllWithFunctionalityErrors(long projectId) {
        return scenarioSummaryMapper.toDto(scenarioRepository.findAllWithFunctionalityErrors(projectId));
    }

    /**
     * @param projectId the ID of the project in which to work
     * @return for each source (API, Web...) and severity couples, a count of ignored&amp;total scenarios and a list of ignored scenarios by feature file
     */
    public List<ScenarioIgnoreSourceDTO> getIgnoredScenarioCounts(long projectId) {
        // All database severities + special Severity.ALL
        List<SeverityDTO> severitiesWithAll = severityService.getSeveritiesWithAll(projectId);
        String defaultSeverityCode = severityService.getDefaultSeverityCode(severitiesWithAll);

        List<ScenarioIgnoreSourceDTO> resultList = new ArrayList<>();
        fillCountsInto(projectId, resultList, severitiesWithAll, defaultSeverityCode);
        fillScenariosInto(projectId, resultList, severitiesWithAll, defaultSeverityCode);
        addGlobalSource(resultList);
        sort(resultList);
        return resultList;
    }

    private void addGlobalSource(List<ScenarioIgnoreSourceDTO> resultList) {
        ScenarioIgnoreSourceDTO global = new ScenarioIgnoreSourceDTO();
        global.setSource(SourceDTO.ALL);

        // Add all counts
        for (ScenarioIgnoreSourceDTO sourceDTO : resultList) {
            for (ScenarioIgnoreSeverityDTO severityDTO : sourceDTO.getSeverities()) {
                final ScenarioIgnoreCountDTO globalCounts = getOrCreate(global, severityDTO.getSeverity()).getCounts();
                globalCounts.setIgnored(globalCounts.getIgnored() + severityDTO.getCounts().getIgnored());
                globalCounts.setTotal(globalCounts.getTotal() + severityDTO.getCounts().getTotal());
            }
        }

        // Global special source has no feature list (null will not export it in JSON)
        for (ScenarioIgnoreSeverityDTO severityDTO : global.getSeverities()) {
            severityDTO.setFeatures(null);
        }

        resultList.add(global);
    }

    /**
     * Sort a source[].severity[] tree by source code first, and then for each source, all of their severities by severity position.
     *
     * @param resultList the tree to sort
     */
    private void sort(List<ScenarioIgnoreSourceDTO> resultList) {
        // Order all sources
        resultList.sort((result1, result2) -> {
            Comparator<ScenarioIgnoreSourceDTO> comparator = comparing(ScenarioIgnoreSourceDTO::getSource, nullsFirst(naturalOrder()));
            return nullsFirst(comparator).compare(result1, result2);
        });

        // For each source, sort all severities
        for (ScenarioIgnoreSourceDTO result : resultList) {
            result.getSeverities().sort((severity1, severity2) -> {
                Comparator<ScenarioIgnoreSeverityDTO> comparator = comparing(s -> s.getSeverity().getPosition(), nullsFirst(naturalOrder()));
                return nullsFirst(comparator).compare(severity1, severity2);
            });
        }
    }

    /**
     * Compute a source[].severity[] tree, counting total and ignored scenarios per severity.
     *
     * @param projectId           the ID of the project in which to work
     * @param resultList          the list in which to populate counts: for each source, a list of severities (with ignored and
     *                            total scenario counts)
     * @param severitiesWithAll   all actual severities + the ALL special-severity
     * @param defaultSeverityCode the code of the severity to use for scenarios with no assigned severity
     */
    private void fillCountsInto(long projectId, List<ScenarioIgnoreSourceDTO> resultList, List<SeverityDTO> severitiesWithAll, String defaultSeverityCode) {
        // For each [ source, severityCode, ignoredOrNot ] triple, we have the count of scenarios that match these criteria
        for (ScenarioIgnoreCount ignoreCount : scenarioRepository.findIgnoreCounts(projectId)) {
            // Scenarios without severity will be counted as having the default severity
            final String effectiveSeverityCode = (StringUtils.isEmpty(ignoreCount.getSeverityCode()) ? defaultSeverityCode : ignoreCount.getSeverityCode());
            // Add the counts of scenarios to the triple' severity, as well as the the ALL-SEVERITIES count
            final List<SeverityDTO> severitiesToIncrement = severitiesWithAll.stream()
                    .filter(severity -> Severity.ALL.getCode().equals(severity.getCode()) || severity.getCode().equals(effectiveSeverityCode))
                    .collect(Collectors.toList());
            for (SeverityDTO severity : severitiesToIncrement) {
                ScenarioIgnoreSeverityDTO result = getOrCreate(resultList, ignoreCount.getSource(), severity);
                // Increment the count of TOTAL scenarios and the count of IGNORED scenarios (if the count is counting ignored scenarios)
                result.getCounts().setTotal(result.getCounts().getTotal() + ignoreCount.getCount());
                if (ignoreCount.isIgnored()) {
                    result.getCounts().setIgnored(result.getCounts().getIgnored() + ignoreCount.getCount());
                }
            }
        }
    }

    /**
     * Compute a source[].severity[].feature[].scenario[] tree, appending all ignored scenario names in this tree.
     *
     * @param projectId           the ID of the project in which to work
     * @param resultList          the list in which to populate scenario names: for each source, a list of severities,
     *                            and for each severity, a list of features (with name and file name)... and for each feature, a
     *                            list of scenario names (only ignored scenarios are appended)
     * @param severities          all actual severities (with or without the ALL special-severity: this one will not be used)
     * @param defaultSeverityCode the code of the severity to use for scenarios with no assigned severity
     */
    private void fillScenariosInto(long projectId, List<ScenarioIgnoreSourceDTO> resultList, List<SeverityDTO> severities, String defaultSeverityCode) {
        for (IgnoredScenario ignoredScenario : scenarioRepository.findIgnoredScenarios(projectId)) {
            // Scenarios without severity will be counted as having the default severity
            final String effectiveSeverityCode = (StringUtils.isEmpty(ignoredScenario.getSeverity()) ? defaultSeverityCode : ignoredScenario.getSeverity());
            // Append the ignored scenario to the feature&scenario list of its source&severity
            severities.stream()
                    .filter(severity -> severity.getCode().equals(effectiveSeverityCode))
                    .findFirst()
                    .ifPresent(severity -> {
                        ScenarioIgnoreSeverityDTO result = getOrCreate(resultList, ignoredScenario.getSource(), severity);
                        getOrCreateFeature(result.getFeatures(), ignoredScenario).getScenarios().add(ignoredScenario.getName());
                    });
        }
    }

    /**
     * @param features        the list of features in which to find or create the featureFile of the ignoredScenario;
     *                        if created, it is appended to the list
     * @param ignoredScenario search its featureFile in the list, or create it with its featureFile and featureName
     * @return the found or created DTO from/into the list
     */
    private ScenarioIgnoreFeatureDTO getOrCreateFeature(List<ScenarioIgnoreFeatureDTO> features, IgnoredScenario ignoredScenario) {
        return features.stream()
                .filter(f -> f.getFile().equals(ignoredScenario.getFeatureFile()))
                .findFirst()
                .orElseGet(() -> {
                    ScenarioIgnoreFeatureDTO feature = new ScenarioIgnoreFeatureDTO();
                    feature.setFile(ignoredScenario.getFeatureFile());
                    feature.setName(ignoredScenario.getFeatureName());
                    features.add(feature);
                    return feature;
                });
    }

    /**
     * @param list     the list of DTOs in which to find or create the requested one (matching both source and severity);
     *                 if created, it is appended to the list
     * @param source   the requested source of the DTO to find or create
     * @param severity the requested severity of the DTO to find or create
     * @return the found or created DTO from/into the list
     */
    private ScenarioIgnoreSeverityDTO getOrCreate(List<ScenarioIgnoreSourceDTO> list, Source source, SeverityDTO severity) {
        final ScenarioIgnoreSourceDTO ignoredSourceDTO = list.stream()
                .filter(r -> r.getSource().getCode().equals(source.getCode()))
                .findFirst()
                .orElseGet(() -> {
                    ScenarioIgnoreSourceDTO result = new ScenarioIgnoreSourceDTO();
                    result.setSource(sourceMapper.toDto(source));
                    list.add(result);
                    return result;
                });
        return getOrCreate(ignoredSourceDTO, severity);
    }

    private ScenarioIgnoreSeverityDTO getOrCreate(ScenarioIgnoreSourceDTO ignoredSourceDTO, SeverityDTO severity) {
        return ignoredSourceDTO.getSeverities().stream()
                .filter(r -> r.getSeverity().getCode().equals(severity.getCode()))
                .findFirst()
                .orElseGet(() -> {
                    ScenarioIgnoreSeverityDTO result = new ScenarioIgnoreSeverityDTO();
                    result.setSeverity(severity);
                    result.setFeatures(severity.getCode().equals(Severity.ALL.getCode()) ? null : new ArrayList<>());
                    ignoredSourceDTO.getSeverities().add(result);
                    return result;
                });
    }

    @FunctionalInterface
    private interface ScenarioListSupplier {

        List<Scenario> get(Source source) throws BadRequestException;

    }

    private List<String> getSeverityCodes(long projectId) {
        return severityRepository.findAllByProjectIdOrderByPosition(projectId).stream()
                .map(Severity::getCode)
                .distinct()
                .collect(Collectors.toList());
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

}
