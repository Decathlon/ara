package com.decathlon.ara.v2.service.migration.scenario;

import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Source;
import com.decathlon.ara.repository.ScenarioRepository;
import com.decathlon.ara.service.support.Settings;
import com.decathlon.ara.v2.domain.*;
import com.decathlon.ara.v2.domain.id.CodeWithProjectId;
import com.decathlon.ara.v2.domain.id.ScenarioVersionId;
import com.decathlon.ara.v2.exception.BusinessException;
import com.decathlon.ara.v2.exception.project.IncompleteProjectException;
import com.decathlon.ara.v2.exception.project.ProjectRequiredException;
import com.decathlon.ara.v2.repository.V2ScenarioRepository;
import com.decathlon.ara.v2.repository.V2ScenarioVersionRepository;
import com.decathlon.ara.v2.service.migration.V2ProjectMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(Transactional.TxType.REQUIRED)
public class V2ScenarioMigrationService {

    private final ScenarioRepository legacyScenarioRepository;

    private final V2ScenarioRepository migrationScenarioRepository;
    private final V2ScenarioVersionRepository migrationScenarioVersionRepository;

    /**
     * Migrate scenarios from a legacy project to a destination project
     * @param legacyProject the legacy project
     * @param migrationProject the migration project
     * @param allAvailableFeatures the features previously migrated
     * @param allAvailableTags the tags previously migrated
     * @param legacyExecutions the legacy executions
     * @return the migrated scenarios
     * @throws BusinessException thrown when scenarios not migrated
     */
    public List<Scenario> migrateScenarios(
            com.decathlon.ara.domain.Project legacyProject,
            Project migrationProject,
            List<Feature> allAvailableFeatures,
            List<Tag> allAvailableTags,
            List<com.decathlon.ara.domain.Execution> legacyExecutions
    ) throws BusinessException {
        if (legacyProject == null) {
            log.error("Could not migrate scenarios because no legacy project was given");
            throw new ProjectRequiredException();
        }

        Long projectId = legacyProject.getId();
        if (projectId == null) {
            log.error("Could not migrate scenarios because no id found in the given legacy project");
            throw new IncompleteProjectException();
        }

        if (migrationProject == null) {
            log.error("Could not migrate scenarios because no destination project was given");
            throw new ProjectRequiredException();
        }

        var legacyScenarios = legacyScenarioRepository.findAllBySourceProjectId(projectId);
        if (CollectionUtils.isEmpty(legacyScenarios)) {
            return new ArrayList<>();
        }

        return migrateScenariosFromLegacyScenariosAndExecutedScenarios(
                legacyScenarios,
                legacyExecutions,
                migrationProject,
                allAvailableFeatures,
                allAvailableTags
        );
    }

    /**
     * Migrate scenarios
     * @param legacyScenarios the legacy scenarios
     * @param legacyExecutions the legacy executions. The executed scenarios are used to create the scenario versions
     * @param migrationProject the migration project
     * @param allAvailableFeatures the features previously migrated
     * @param allAvailableTags the tags previously migrated
     * @return the migrated scenarios
     */
    private List<Scenario> migrateScenariosFromLegacyScenariosAndExecutedScenarios(
            List<com.decathlon.ara.domain.Scenario> legacyScenarios,
            List<com.decathlon.ara.domain.Execution> legacyExecutions,
            Project migrationProject,
            List<Feature> allAvailableFeatures,
            List<Tag> allAvailableTags
    ) {
        final var distinctLegacyScenarios = legacyScenarios.stream()
                .map(UniqueLegacyScenarioWrapper::new)
                .distinct()
                .map(UniqueLegacyScenarioWrapper::getLegacyScenario)
                .collect(Collectors.toList());

        final var legacyExtendedExecutedScenarioWithPotentialScenarioMatchPairs = legacyExecutions.stream()
                .map(execution -> Pair.of(execution, execution.getCycleDefinition().getBranch()))
                .map(executionAndBranchPair -> executionAndBranchPair
                        .getFirst()
                        .getRuns()
                        .stream()
                        .map(run -> run.getExecutedScenarios()
                                .stream()
                                .map(executedScenario -> new ExecutedScenario.ExtendedExecutedScenario()
                                        .withLegacyExecutedScenario(executedScenario)
                                        .withBranchName(executionAndBranchPair.getSecond())
                                        .withLegacySource(run.getType().getSource())
                                )
                                .collect(Collectors.toList())
                        )
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList())
                )
                .flatMap(Collection::stream)
                .sorted(Comparator.comparingLong(extendedExecutedScenario -> extendedExecutedScenario.getLegacyExecutedScenario().getId()))
                .collect(Collectors.groupingBy(UniqueLegacyExecutedScenarioBaseWrapper::new))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue()
                                .stream()
                                .map(UniqueLegacyExecutedScenarioVersionWrapper::new)
                                .distinct()
                                .map(UniqueLegacyExecutedScenarioBaseWrapper::getExtendedExecutedScenario)
                                .collect(Collectors.toList())
                ))
                .entrySet()
                .stream()
                .sorted(Comparator.comparingLong(entry -> entry.getKey().getExtendedExecutedScenario().getLegacyExecutedScenario().getId()))
                .map(entry -> Pair.of(
                        entry.getKey()
                                .getExtendedExecutedScenario()
                                .getPotentialMatchingScenario(distinctLegacyScenarios)
                        ,
                        entry.getValue()
                ))
                .collect(Collectors.toList());

        final var uniqueLegacyScenarioWrappersMatchingExecutedScenarios = legacyExtendedExecutedScenarioWithPotentialScenarioMatchPairs.stream()
                .map(Pair::getFirst)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(UniqueLegacyScenarioWrapper::new)
                .collect(Collectors.toList());
        final var legacyScenariosNotMatchingAnyExecutedScenario = distinctLegacyScenarios.stream()
                .map(UniqueLegacyScenarioWrapper::new)
                .filter(wrapper -> !uniqueLegacyScenarioWrappersMatchingExecutedScenarios.contains(wrapper))
                .map(UniqueLegacyScenarioWrapper::getLegacyScenario)
                .map(scenario -> Pair.of(Optional.ofNullable(scenario), Collections.<ExecutedScenario.ExtendedExecutedScenario> emptyList()))
                .collect(Collectors.toList());

        final var migratedScenarios = Stream.of(legacyExtendedExecutedScenarioWithPotentialScenarioMatchPairs, legacyScenariosNotMatchingAnyExecutedScenario)
                .flatMap(Collection::stream)
                .map(pair -> migrateScenario(pair, migrationProject, allAvailableFeatures, allAvailableTags))
                .collect(Collectors.toList());
        return migratedScenarios;
    }

    /**
     * Migrate a single scenario
     * @param potentialLegacyScenarioAndItsExtendedExecutedScenarios a pair containing a potential scenario and its (distinct) matching executed scenarios
     * @param migrationProject the migration project
     * @param allAvailableFeatures the features previously migrated
     * @param allAvailableTags the tags previously migrated
     * @return the migrated scenario
     */
    private Scenario migrateScenario(
            Pair<Optional<com.decathlon.ara.domain.Scenario>, List<ExecutedScenario.ExtendedExecutedScenario>> potentialLegacyScenarioAndItsExtendedExecutedScenarios,
            Project migrationProject,
            List<Feature> allAvailableFeatures,
            List<Tag> allAvailableTags
    ) {
        var potentialLegacyScenario = potentialLegacyScenarioAndItsExtendedExecutedScenarios.getFirst();
        var legacyExtendedExecutedScenarios = potentialLegacyScenarioAndItsExtendedExecutedScenarios.getSecond();

        var baseMigrationScenario = potentialLegacyScenario
                .map(legacyScenario -> getBaseMigrationScenarioFromLegacyScenario(legacyScenario, migrationProject, allAvailableTags))
                .orElseGet(() -> getBaseMigrationScenarioFromLegacyExtendedScenarios(legacyExtendedExecutedScenarios, migrationProject, allAvailableTags));

        final var migratedScenario = migrationScenarioRepository.save(baseMigrationScenario);
        var versions = legacyExtendedExecutedScenarios.stream()
                .map(executedScenario ->
                        getMigrationScenarioVersionFromLegacyExtendedExecutedScenario(
                                executedScenario,
                                potentialLegacyScenario,
                                migrationProject,
                                migratedScenario,
                                allAvailableFeatures
                        )
                )
                .collect(Collectors.toList());
        potentialLegacyScenario.ifPresent(scenario ->
                versions.add(0, getMigrationScenarioVersionFromLegacyScenario(
                        scenario,
                        migrationProject,
                        migratedScenario,
                        allAvailableFeatures
                        )
                )
        );
        final var migratedVersions = migrationScenarioVersionRepository.saveAll(versions);
        return migratedScenario.withVersions(migratedVersions);
    }

    /**
     * Convert a legacy scenario into a base migration scenario (i.e. without versions)
     * @param legacyScenario the legacy scenario
     * @param migrationProject the migration project
     * @param allAvailableTags the tags previously migrated
     * @return the converted scenario
     */
    private Scenario getBaseMigrationScenarioFromLegacyScenario(
            com.decathlon.ara.domain.Scenario legacyScenario,
            Project migrationProject,
            List<Tag> allAvailableTags
    ) {
        var scenarioCode = String.format("s-%d", legacyScenario.getId());
        var scenarioName = legacyScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName().getFirst();
        var scenarioType = getMigrationScenarioTypeFromLegacySource(legacyScenario.getSource(), migrationProject).orElse(null);
        var scenarioTags = getMigrationTagsFromCountryCodes(legacyScenario.getCountryCodes(), allAvailableTags);
        return new Scenario()
                .withId(
                        new CodeWithProjectId()
                                .withCode(scenarioCode)
                                .withProject(migrationProject)
                )
                .withName(scenarioName)
                .withType(scenarioType)
                .withTags(scenarioTags);
    }

    /**
     * Convert a legacy extended (i.e. with branch and source) executed scenarios into a base migration scenario (i.e. without versions)
     * @param legacyExtendedExecutedScenarios the legacy extended executed scenarios
     * @param migrationProject the migration project
     * @param allAvailableTags the tags previously migrated
     * @return the converted scenario
     */
    private Scenario getBaseMigrationScenarioFromLegacyExtendedScenarios(
            List<ExecutedScenario.ExtendedExecutedScenario> legacyExtendedExecutedScenarios,
            Project migrationProject,
            List<Tag> allAvailableTags
    ) {
        var firstLegacyExtendedExecutedScenario = legacyExtendedExecutedScenarios.stream()
                .findFirst()
                .get();
        var firstLegacyExecutedScenario = firstLegacyExtendedExecutedScenario.getLegacyExecutedScenario();
        var scenarioCode = String.format("e-%d", firstLegacyExecutedScenario.getId());
        var scenarioName = firstLegacyExecutedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName().getFirst();
        var scenarioType = getMigrationScenarioTypeFromLegacySource(firstLegacyExtendedExecutedScenario.getLegacySource(), migrationProject).orElse(null);
        return new Scenario()
                .withId(
                        new CodeWithProjectId()
                                .withCode(scenarioCode)
                                .withProject(migrationProject)
                )
                .withName(scenarioName)
                .withType(scenarioType)
                .withTags(Collections.emptyList());
    }

    /**
     * Convert a legacy scenario into a migration scenario version
     * @param legacyScenario the legacy scenario
     * @param migrationProject the migration project
     * @param migrationScenario the migration scenario (for the version id)
     * @param allFeatures the features previously migrated
     * @return the converted scenario version
     */
    private ScenarioVersion getMigrationScenarioVersionFromLegacyScenario(
            com.decathlon.ara.domain.Scenario legacyScenario,
            Project migrationProject,
            Scenario migrationScenario,
            List<Feature> allFeatures
    ) {
        var legacySource = legacyScenario.getSource();
        var branchName = legacySource.getDefaultBranch();
        var featureCodes = legacyScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName().getSecond();

        var generatedSHA = String.format("generated_SHA-s-%s-%d", branchName, legacyScenario.getId());
        var versionId = new ScenarioVersionId()
                .withScenario(migrationScenario)
                .withCommitSHA(generatedSHA);
        var coveredFeatures = getFeaturesFromCodes(allFeatures, featureCodes);
        var steps = getMigrationScenarioSteps(legacyScenario.getScenarioSteps());
        var severity = new ScenarioSeverity()
                .withId(
                        new CodeWithProjectId()
                                .withProject(migrationProject)
                                .withCode(legacyScenario.getSeverity())
                )
                .withName(V2ProjectMigrationService.FIELD_TO_RENAME)
                .withDescription(V2ProjectMigrationService.NEW_FIELD_GENERATION);
        var branch = new Branch()
                .withId(
                        new CodeWithProjectId()
                                .withCode(branchName)
                                .withProject(migrationProject)
                )
                .withName(V2ProjectMigrationService.FIELD_TO_RENAME)
                .withDescription(V2ProjectMigrationService.NEW_FIELD_GENERATION);
        var scenarioFileUrl = getFileUrl(legacySource, Optional.empty(), legacyScenario.getFeatureFile());

        return new ScenarioVersion()
                .withId(versionId)
                .withCoveredFeatures(coveredFeatures)
                .withIgnored(legacyScenario.isIgnored())
                .withSteps(steps)
                .withUpdateDateTime(LocalDateTime.now())
                .withSeverity(severity)
                .withBranch(branch)
                .withFileName(legacyScenario.getFeatureName())
                .withFileUrl(scenarioFileUrl);
    }

    /**
     * Convert a legacy extended (i.e. with branch and source) executed scenario into a migration scenario version
     * @param legacyExtendedExecutedScenario the legacy extended executed scenario
     * @param legacyScenario the legacy scenario
     * @param migrationProject the migration project
     * @param migrationScenario the migration scenario (for the version id)
     * @param allFeatures the features previously migrated
     * @return the converted scenario version
     */
    private ScenarioVersion getMigrationScenarioVersionFromLegacyExtendedExecutedScenario(
            ExecutedScenario.ExtendedExecutedScenario legacyExtendedExecutedScenario,
            Optional<com.decathlon.ara.domain.Scenario> legacyScenario,
            Project migrationProject,
            Scenario migrationScenario,
            List<Feature> allFeatures
    ) {
        var legacyExecutedScenario = legacyExtendedExecutedScenario.getLegacyExecutedScenario();
        var legacySource = legacyExtendedExecutedScenario.getLegacySource();
        var branchName = legacyExtendedExecutedScenario.getBranchName();
        var featureCodes = legacyExecutedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName().getSecond();

        var generatedSHA = String.format("generated_SHA-e-%s-%d", branchName, legacyExecutedScenario.getId());
        var versionId = new ScenarioVersionId()
                .withScenario(migrationScenario)
                .withCommitSHA(generatedSHA);
        var coveredFeatures = getFeaturesFromCodes(allFeatures, featureCodes);
        var steps = getMigrationScenarioSteps(legacyExecutedScenario.getStatelessScenarioSteps());
        var severity = new ScenarioSeverity()
                .withId(
                        new CodeWithProjectId()
                                .withProject(migrationProject)
                                .withCode(legacyExecutedScenario.getSeverity())
                )
                .withName(V2ProjectMigrationService.FIELD_TO_RENAME)
                .withDescription(V2ProjectMigrationService.NEW_FIELD_GENERATION);
        var branch = new Branch()
                .withId(
                        new CodeWithProjectId()
                                .withCode(branchName)
                                .withProject(migrationProject)
                )
                .withName(V2ProjectMigrationService.FIELD_TO_RENAME)
                .withDescription(V2ProjectMigrationService.NEW_FIELD_GENERATION);
        var scenarioFileUrl = getFileUrl(legacySource, Optional.of(branchName), legacyExecutedScenario.getFeatureFile());

        return new ScenarioVersion()
                .withId(versionId)
                .withCoveredFeatures(coveredFeatures)
                .withIgnored(legacyScenario.map(com.decathlon.ara.domain.Scenario::isIgnored).orElse(false))
                .withSteps(steps)
                .withUpdateDateTime(legacyExecutedScenario.getStartDateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .withSeverity(severity)
                .withBranch(branch)
                .withFileName(legacyScenario.map(com.decathlon.ara.domain.Scenario::getFeatureName).orElse(legacyExecutedScenario.getFeatureName()))
                .withFileUrl(scenarioFileUrl);
    }

    /**
     * Get features from feature codes. If a code doesn't match any feature, then it's ignored
     * @param features the features previously migrated
     * @param featureCodes the feature codes
     * @return the features matching the codes
     */
    private List<Feature> getFeaturesFromCodes(List<Feature> features, List<String> featureCodes) {
        if (CollectionUtils.isEmpty(features)) {
            return new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(featureCodes)) {
            return new ArrayList<>();
        }
        return featureCodes.stream()
                .map(featureCode ->
                        features.stream()
                                .filter(feature -> Objects.equals(featureCode, feature.getCode()))
                                .findFirst()
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Convert a legacy source into a migration scenario type
     * @param legacySource the legacy source
     * @param migrationProject the migration project
     * @return the migration scenario type
     */
    public static Optional<ScenarioType> getMigrationScenarioTypeFromLegacySource(
            Source legacySource,
            Project migrationProject
    ) {
        if (legacySource == null || legacySource.getTechnology() == null) {
            return Optional.empty();
        }
        var migrationScenarioType = new ScenarioType()
                .withId(new CodeWithProjectId().withProject(migrationProject).withCode(legacySource.getCode()))
                .withName(legacySource.getName())
                .withTechnology(legacySource.getTechnology().name())
                .withDescription(V2ProjectMigrationService.NEW_FIELD_GENERATION);
        return Optional.of(migrationScenarioType);
    }

    /**
     * Get migration tags from country codes
     * @param countryCodes the country codes
     * @param allAvailableTags the tags previously migrated
     * @return the migration tags
     */
    private List<Tag> getMigrationTagsFromCountryCodes(String countryCodes, List<Tag> allAvailableTags) {
        if (StringUtils.isBlank(countryCodes)) {
            return new ArrayList<>();
        }

        if (CollectionUtils.isEmpty(allAvailableTags)) {
            return new ArrayList<>();
        }

        if ("all".equals(countryCodes)) {
            return allAvailableTags;
        }

        Function<String, Optional<Tag>> getMigrationTagFromCodeFunction = tagCode -> allAvailableTags.stream()
                .filter(tag -> Objects.equals(tagCode, tag.getId().getCode()))
                .findFirst();

        return Arrays.stream(countryCodes.split(com.decathlon.ara.domain.Scenario.COUNTRY_CODES_SEPARATOR))
                .filter(StringUtils::isNotBlank)
                .map(String::strip)
                .map(String::toLowerCase)
                .distinct()
                .map(getMigrationTagFromCodeFunction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Get migration scenario steps from legacy scenario steps
     * @param legacyScenarioSteps the legacy scenario steps
     * @return the migration scenario steps
     */
    private List<ScenarioStep> getMigrationScenarioSteps(
            List<com.decathlon.ara.domain.Scenario.ScenarioStep> legacyScenarioSteps
    ) {
        return CollectionUtils.isEmpty(legacyScenarioSteps) ?
                new ArrayList<>() :
                legacyScenarioSteps
                        .stream()
                        .map(legacyStep -> new ScenarioStep().withLine(legacyStep.getLine()).withContent(legacyStep.getContent()))
                        .collect(Collectors.toList());
    }

    /**
     * Get the complete scenario file url
     * @param legacySource the legacy source
     * @param branchName the branch name
     * @param fileName the scenario file name
     * @return the complete scenario file url
     */
    private String getFileUrl(Source legacySource, Optional<String> branchName, String fileName) {
        var baseUrl = legacySource.getVcsUrl();
        if (StringUtils.isBlank(baseUrl)) {
            return "";
        }
        var baseUrlLastChar = baseUrl.charAt(baseUrl.length() - 1);
        var finalBaseUrl = '/' == baseUrlLastChar ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        var finalBaseUrlWithBranch = finalBaseUrl.replace(
                Settings.BRANCH_VARIABLE,
                branchName.orElse(legacySource.getDefaultBranch())
        );

        var fileNameFirstChar = fileName.charAt(0);
        var finalFileName = '/' == fileNameFirstChar ? fileName.substring(1) : fileName;
        return String.format("%s/%s", finalBaseUrlWithBranch, finalFileName);
    }

    private static class UniqueLegacyExecutedScenarioBaseWrapper {
        protected ExecutedScenario.ExtendedExecutedScenario extendedExecutedScenario;

        public UniqueLegacyExecutedScenarioBaseWrapper(ExecutedScenario.ExtendedExecutedScenario extendedExecutedScenario) {
            this.extendedExecutedScenario = extendedExecutedScenario;
        }

        public ExecutedScenario.ExtendedExecutedScenario getExtendedExecutedScenario() {
            return extendedExecutedScenario;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UniqueLegacyExecutedScenarioBaseWrapper that = (UniqueLegacyExecutedScenarioBaseWrapper) o;
            var thisLegacyExecutedScenario = extendedExecutedScenario.getLegacyExecutedScenario();
            var thatLegacyExecutedScenario = that.extendedExecutedScenario.getLegacyExecutedScenario();
            var thisScenarioNameOnly = thisLegacyExecutedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName().getFirst();
            var thatScenarioNameOnly = thatLegacyExecutedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName().getFirst();
            return Objects.equals(thisLegacyExecutedScenario.getFeatureFile(), thatLegacyExecutedScenario.getFeatureFile()) &&
                    Objects.equals(thisScenarioNameOnly, thatScenarioNameOnly) &&
                    Objects.equals(extendedExecutedScenario.getLegacySource(), that.extendedExecutedScenario.getLegacySource());
        }

        @Override
        public int hashCode() {
            var executedScenario = extendedExecutedScenario.getLegacyExecutedScenario();
            var scenarioFeatureFile =  executedScenario.getFeatureFile();
            var scenarioNameOnly = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName().getFirst();
            var scenarioSource = extendedExecutedScenario.getLegacySource();
            return Objects.hash(scenarioFeatureFile, scenarioNameOnly, scenarioSource);
        }
    }

    private static class UniqueLegacyExecutedScenarioVersionWrapper extends UniqueLegacyExecutedScenarioBaseWrapper {

        public UniqueLegacyExecutedScenarioVersionWrapper(ExecutedScenario.ExtendedExecutedScenario extendedExecutedScenario) {
            super(extendedExecutedScenario);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            UniqueLegacyExecutedScenarioVersionWrapper that = (UniqueLegacyExecutedScenarioVersionWrapper) o;
            var thisLegacyExecutedScenario = extendedExecutedScenario.getLegacyExecutedScenario();
            var thatLegacyExecutedScenario = that.extendedExecutedScenario.getLegacyExecutedScenario();
            return Objects.equals(extendedExecutedScenario.getBranchName(), that.extendedExecutedScenario.getBranchName()) &&
                    Objects.equals(thisLegacyExecutedScenario.getSeverity(), thatLegacyExecutedScenario.getSeverity()) &&
                    thisLegacyExecutedScenario.shareTheSameStepsAs(thatLegacyExecutedScenario) &&
                    thisLegacyExecutedScenario.shareTheSameFunctionalityCodesAs(thatLegacyExecutedScenario);
        }

        @Override
        public int hashCode() {
            var scenarioBranchName = extendedExecutedScenario.getBranchName();
            var executedScenario = extendedExecutedScenario.getLegacyExecutedScenario();
            var scenarioSeverity = executedScenario.getSeverity();
            var featureCodes = executedScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName().getSecond();
            var executedSteps = executedScenario.getExecutedScenarioSteps();
            return Objects.hash(
                    super.hashCode(),
                    scenarioBranchName,
                    scenarioSeverity,
                    featureCodes,
                    executedSteps
            );
        }
    }

    private static class UniqueLegacyScenarioWrapper {
        protected com.decathlon.ara.domain.Scenario legacyScenario;

        public UniqueLegacyScenarioWrapper(com.decathlon.ara.domain.Scenario legacyScenario) {
            this.legacyScenario = legacyScenario;
        }

        public com.decathlon.ara.domain.Scenario getLegacyScenario() {
            return legacyScenario;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UniqueLegacyScenarioWrapper that = (UniqueLegacyScenarioWrapper) o;
            var thisScenarioNameOnly = legacyScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName().getFirst();
            var thatScenarioNameOnly = that.legacyScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName().getFirst();
            return Objects.equals(legacyScenario.getSource(), that.legacyScenario.getSource()) &&
                    Objects.equals(legacyScenario.getFeatureFile(), that.legacyScenario.getFeatureFile()) &&
                    Objects.equals(thisScenarioNameOnly, thatScenarioNameOnly);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    legacyScenario.getSource(),
                    legacyScenario.getFeatureFile(),
                    legacyScenario.getNameWithoutCodesAndFunctionalityCodesFromScenarioName().getFirst()
            );
        }
    }
}
