package com.decathlon.ara.v2.service.migration.feature;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.enumeration.FunctionalitySeverity;
import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.v2.domain.Feature;
import com.decathlon.ara.v2.domain.FeaturePriority;
import com.decathlon.ara.v2.domain.Tag;
import com.decathlon.ara.v2.domain.enumeration.FeatureStatus;
import com.decathlon.ara.v2.domain.id.CodeWithProjectId;
import com.decathlon.ara.v2.exception.BusinessException;
import com.decathlon.ara.v2.exception.project.IncompleteProjectException;
import com.decathlon.ara.v2.exception.project.ProjectRequiredException;
import com.decathlon.ara.v2.repository.V2FeatureRepository;
import com.decathlon.ara.v2.service.migration.V2ProjectMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(Transactional.TxType.REQUIRED)
public class V2FeatureMigrationService {

    private final FunctionalityRepository legacyFunctionalitiesRepository;

    private final V2FeatureRepository migrationFeatureRepository;

    /**
     * Migrate features from a legacy project to a destination project
     * @param legacyProject the legacy project
     * @param destinationProject the destination project
     * @return the persisted features
     * @throws BusinessException thrown when features not migrated
     */
    public List<Feature> migrateFeatures(Project legacyProject, com.decathlon.ara.v2.domain.Project destinationProject) throws BusinessException {
        if (legacyProject == null) {
            log.error("Could not migrate features because no legacy project was given");
            throw new ProjectRequiredException();
        }

        Long projectId = legacyProject.getId();
        if (projectId == null) {
            log.error("Could not migrate features because no id found in the given legacy project");
            throw new IncompleteProjectException();
        }

        if (destinationProject == null) {
            log.error("Could not migrate features because no destination project was given");
            throw new ProjectRequiredException();
        }

        var legacyFunctionalities = legacyFunctionalitiesRepository.findAllByProjectIdAndType(projectId, FunctionalityType.FUNCTIONALITY);
        if (CollectionUtils.isEmpty(legacyFunctionalities)) {
            return new ArrayList<>();
        }

        List<Feature> featuresToMigrate = legacyFunctionalities.stream()
                .filter(legacyFunctionality -> legacyFunctionality.getId() != null)
                .map(getPositionByLegacyFunctionalityPairFunction(legacyFunctionalities))
                .map(getMigrationFeatureFromLegacyFunctionalityAndPositionPairFunction(destinationProject))
                .collect(Collectors.toList());
        return migrationFeatureRepository.saveAll(featuresToMigrate);
    }

    /**
     * Return a function that transform a legacy functionalities into a pair that contains:
     * - the same functionality
     * - its position relative to its siblings (i.e. sharing the same parent)
     * @param allLegacyFunctionalities all available legacy functionalities
     * @return the function that transform the functionality into a pair
     */
    private Function<Functionality, Pair<Functionality, Integer>> getPositionByLegacyFunctionalityPairFunction(Set<Functionality> allLegacyFunctionalities) {
        return legacyFunctionality -> Pair.of(
                legacyFunctionality,
                allLegacyFunctionalities
                        .stream()
                        .filter(functionality -> functionality.getParentId() != null)
                        .filter(functionality -> functionality.getParentId().equals(legacyFunctionality.getParentId()))
                        .map(functionality -> functionality.getId())
                        .filter(Objects::nonNull)
                        .sorted()
                        .collect(Collectors.toList())
                        .indexOf(legacyFunctionality.getId()) + 1
        );
    }

    /**
     * Return a function that transform a pair (containing a legacy functionality and its position) into a migration feature
     * @param migrationProject the migration project
     * @return the function that transform a pair into a migration feature
     */
    private Function<Pair<Functionality, Integer>, Feature> getMigrationFeatureFromLegacyFunctionalityAndPositionPairFunction(
            com.decathlon.ara.v2.domain.Project migrationProject
    ) {
        return legacyFunctionalityAndPositionPair -> {
            var legacyFunctionality = legacyFunctionalityAndPositionPair.getFirst();
            var feature = createMigrationFeatureFromLegacyFunctionality(legacyFunctionalityAndPositionPair);
            var priority = getMigrationFeaturePriorityFromLegacyFunctionality(legacyFunctionality, migrationProject);
            var tags = getMigrationTagsFromLegacyFunctionality(legacyFunctionality, migrationProject);
            return feature.withPriority(priority).withTags(tags);
        };
    }

    /**
     * Get a migration feature priority from a legacy functionality
     * @param legacyFunctionality the legacy functionality
     * @param migrationProject the migration project
     * @return the feature priority
     */
    private FeaturePriority getMigrationFeaturePriorityFromLegacyFunctionality(
            Functionality legacyFunctionality,
            com.decathlon.ara.v2.domain.Project migrationProject
    ) {
        var legacyFunctionalitySeverity = legacyFunctionality.getSeverity();
        if (legacyFunctionalitySeverity == null) {
            return null;
        }
        var legacyFunctionalitySeverityToLevel = Map.of(
                FunctionalitySeverity.HIGH, 1,
                FunctionalitySeverity.MEDIUM, 2,
                FunctionalitySeverity.LOW, 3
        );
        var priorityNameInLowerCase = legacyFunctionalitySeverity.toString().toLowerCase();
        var priorityLevel = legacyFunctionalitySeverityToLevel.get(legacyFunctionalitySeverity);
        return new FeaturePriority()
                .withId(new CodeWithProjectId().withProject(migrationProject).withCode(priorityNameInLowerCase))
                .withName(StringUtils.capitalize(priorityNameInLowerCase))
                .withLevel(priorityLevel);
    }

    /**
     * Get migration tags from legacy functionality
     * @param legacyFunctionality the legacy functionality
     * @param migrationProject the migration project
     * @return the migration tags
     */
    private List<Tag> getMigrationTagsFromLegacyFunctionality(
            Functionality legacyFunctionality,
            com.decathlon.ara.v2.domain.Project migrationProject
    ) {
        var countryCodes = legacyFunctionality.getCountryCodes();
        if (StringUtils.isBlank(countryCodes)) {
            return new ArrayList<>();
        }

        var tagName = V2ProjectMigrationService.FIELD_TO_RENAME;
        var tagDescription = V2ProjectMigrationService.NEW_FIELD_GENERATION;
        Function<Tag, Tag> addNameAndDescriptionIfNotFoundFunction =
                migrationTag -> migrationTag.withName(tagName).withDescription(tagDescription);

        return Arrays.stream(countryCodes.split(Functionality.COUNTRY_CODES_SEPARATOR))
                .map(String::strip)
                .map(String::toLowerCase)
                .map(countryCode -> new Tag().withId(
                        new CodeWithProjectId()
                                .withProject(migrationProject)
                                .withCode(countryCode)
                        )
                )
                .map(addNameAndDescriptionIfNotFoundFunction)
                .collect(Collectors.toList());
    }

    /**
     * Create migration feature from legacy functionality
     * @param legacyFunctionalityAndPositionPair a pair containing a legacy functionality and its position
     * @return the created migration feature
     */
    private Feature createMigrationFeatureFromLegacyFunctionality(Pair<Functionality, Integer> legacyFunctionalityAndPositionPair) {
        var legacyFunctionality = legacyFunctionalityAndPositionPair.getFirst();
        var featurePosition = legacyFunctionalityAndPositionPair.getSecond();
        var featurePath = getMigrationFeaturePathFromLegacyFunctionality(legacyFunctionality);
        var featureStatus = getMigrationFeatureStatusFromLegacyFunctionality(legacyFunctionality);

        return new Feature()
                .withName(legacyFunctionality.getName())
                .withCode(String.valueOf(legacyFunctionality.getId()))
                .withDescription(V2ProjectMigrationService.NEW_FIELD_GENERATION)
                .withPosition(featurePosition)
                .withPath(featurePath)
                .withVersionWhenCreated(legacyFunctionality.getCreated())
                .withStatus(featureStatus)
                .withCreationDateTime(V2ProjectMigrationService.getDateFromLocalDateTime(legacyFunctionality.getCreationDateTime()))
                .withUpdateDateTime(V2ProjectMigrationService.getDateFromLocalDateTime(legacyFunctionality.getUpdateDateTime()))
                .withComment(legacyFunctionality.getComment());
    }

    /**
     * Get migration feature path from legacy functionality
     * @param legacyFunctionality the legacy functionality
     * @return the feature path
     */
    private String getMigrationFeaturePathFromLegacyFunctionality(Functionality legacyFunctionality) {
        var featureName = legacyFunctionality.getName();
        if (legacyFunctionality.getParentId() == null) {
            return "/" + featureName;
        }
        var legacyParentFunctionality = legacyFunctionalitiesRepository.findByProjectIdAndId(legacyFunctionality.getProjectId(), legacyFunctionality.getParentId());
        return getMigrationFeaturePathFromLegacyFunctionality(legacyParentFunctionality.get()) + (FunctionalityType.FOLDER.equals(legacyFunctionality.getType()) ? "/" + featureName : "");
    }

    /**
     * Get the migration feature status
     * @param legacyFunctionality the legacy functionality status
     * @return the feature status
     */
    private FeatureStatus getMigrationFeatureStatusFromLegacyFunctionality(Functionality legacyFunctionality) {
        if (Boolean.TRUE.equals(legacyFunctionality.getNotAutomatable())) {
            return FeatureStatus.NOT_AUTOMATABLE;
        }
        if (Boolean.TRUE.equals(legacyFunctionality.getStarted())) {
            return FeatureStatus.STARTED;
        }
        return FeatureStatus.READY;
    }
}
