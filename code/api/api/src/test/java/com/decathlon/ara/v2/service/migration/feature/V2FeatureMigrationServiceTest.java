package com.decathlon.ara.v2.service.migration.feature;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.enumeration.FunctionalitySeverity;
import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.v2.domain.Feature;
import com.decathlon.ara.v2.domain.Project;
import com.decathlon.ara.v2.domain.Tag;
import com.decathlon.ara.v2.domain.enumeration.FeatureStatus;
import com.decathlon.ara.v2.exception.BusinessException;
import com.decathlon.ara.v2.exception.project.IncompleteProjectException;
import com.decathlon.ara.v2.exception.project.ProjectRequiredException;
import com.decathlon.ara.v2.repository.V2FeatureRepository;
import com.decathlon.ara.v2.service.migration.V2ProjectMigrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class V2FeatureMigrationServiceTest {

    @Mock
    private FunctionalityRepository legacyFunctionalitiesRepository;

    @Mock
    private V2FeatureRepository migrationFeatureRepository;

    @InjectMocks
    private V2FeatureMigrationService featureMigrationService;

    @Test
    void migrateFeatures_throwProjectRequiredException_whenLegacyProjectIsNull() {
        // Given

        // When

        // Then
        assertThatThrownBy(() -> featureMigrationService.migrateFeatures(null, mock(Project.class)))
                .isExactlyInstanceOf(ProjectRequiredException.class);
    }

    @Test
    void migrateFeatures_throwIncompleteProjectException_whenLegacyProjectHasNoId() {
        // Given
        com.decathlon.ara.domain.Project legacyProject = mock(com.decathlon.ara.domain.Project.class);

        // When
        when(legacyProject.getId()).thenReturn(null);

        // Then
        assertThatThrownBy(() -> featureMigrationService.migrateFeatures(legacyProject, mock(Project.class)))
                .isExactlyInstanceOf(IncompleteProjectException.class);
    }

    @Test
    void migrateFeatures_throwProjectRequiredException_whenDestinationProjectIsNull() {
        // Given
        Long projectId = 1L;
        com.decathlon.ara.domain.Project legacyProject = mock(com.decathlon.ara.domain.Project.class);

        // When
        when(legacyProject.getId()).thenReturn(projectId);

        // Then
        assertThatThrownBy(() -> featureMigrationService.migrateFeatures(legacyProject, null))
                .isExactlyInstanceOf(ProjectRequiredException.class);
    }

    @Test
    void migrateFeatures_returnEmptyList_whenNoLegacyFunctionalityFound() throws BusinessException {
        // Given
        Long projectId = 1L;
        com.decathlon.ara.domain.Project legacyProject = mock(com.decathlon.ara.domain.Project.class);

        // When
        when(legacyProject.getId()).thenReturn(projectId);
        when(legacyFunctionalitiesRepository.findAllByProjectIdAndType(projectId, FunctionalityType.FUNCTIONALITY)).thenReturn(null);

        // Then
        List<Feature> features = featureMigrationService.migrateFeatures(legacyProject, mock(Project.class));
        assertThat(features).isNotNull().isEmpty();
    }

    @Test
    void migrateFeatures_saveMigratedFeatures_whenLegacyFunctionalitiesFound() throws BusinessException, ParseException {
        // Given
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        Long projectId = 1L;
        com.decathlon.ara.domain.Project legacyProject = mock(com.decathlon.ara.domain.Project.class);

        Project migrationProject = mock(Project.class);

        List<Feature> savedFeatures = mock(List.class);

        Functionality root1 = mock(Functionality.class);
        Functionality directory11 = mock(Functionality.class);
        Functionality file12 = mock(Functionality.class);
        Functionality file111 = mock(Functionality.class);
        Functionality file112 = mock(Functionality.class);
        Functionality directory113 = mock(Functionality.class);
        Functionality file114 = mock(Functionality.class);
        Functionality file1131 = mock(Functionality.class);

        Functionality root2 = mock(Functionality.class);
        Functionality file21 = mock(Functionality.class);
        Functionality directory22 = mock(Functionality.class);
        Functionality file221 = mock(Functionality.class);

        var legacyFunctionalities = new TreeSet<>(List.of(file12, file111, file112, file114, file1131, file21, file221));

        // When
        when(legacyProject.getId()).thenReturn(projectId);
        when(migrationFeatureRepository.saveAll(anyList())).thenReturn(savedFeatures);
        when(legacyFunctionalitiesRepository.findAllByProjectIdAndType(projectId, FunctionalityType.FUNCTIONALITY)).thenReturn(legacyFunctionalities);
        // Functionalities
        // file12
        when(file12.getId()).thenReturn(12L);
        when(file12.getName()).thenReturn("file12");
        when(file12.getComment()).thenReturn("Comment for feature 12");
        when(file12.getSeverity()).thenReturn(null);
        when(file12.getCreated()).thenReturn("12.1");
        when(file12.getProjectId()).thenReturn(projectId);
        when(file12.getParentId()).thenReturn(1L);
        when(file12.getCreationDateTime()).thenReturn(null);
        when(file12.getUpdateDateTime()).thenReturn(simpleDateFormat.parse("01/01/2021 01:01:21"));
        when(file12.getCountryCodes()).thenReturn("fr");
        // file111
        when(file111.getId()).thenReturn(111L);
        when(file111.getName()).thenReturn("file111");
        when(file111.getNotAutomatable()).thenReturn(true);
        when(file111.getComment()).thenReturn("Comment for feature 111");
        when(file111.getSeverity()).thenReturn(FunctionalitySeverity.LOW);
        when(file111.getCreated()).thenReturn("111.1");
        when(file111.getProjectId()).thenReturn(projectId);
        when(file111.getParentId()).thenReturn(11L);
        when(file111.getCreationDateTime()).thenReturn(simpleDateFormat.parse("01/11/2020 01:11:10"));
        when(file111.getUpdateDateTime()).thenReturn(simpleDateFormat.parse("01/11/2021 01:11:10"));
        when(file111.getCountryCodes()).thenReturn(null);
        // file112
        when(file112.getId()).thenReturn(112L);
        when(file112.getName()).thenReturn("file112");
        when(file112.getStarted()).thenReturn(true);
        when(file112.getComment()).thenReturn("Comment for feature 112");
        when(file112.getSeverity()).thenReturn(FunctionalitySeverity.HIGH);
        when(file112.getCreated()).thenReturn("112.1");
        when(file112.getProjectId()).thenReturn(projectId);
        when(file112.getParentId()).thenReturn(11L);
        when(file112.getCreationDateTime()).thenReturn(simpleDateFormat.parse("11/02/2020 11:02:15"));
        when(file112.getUpdateDateTime()).thenReturn(simpleDateFormat.parse("11/02/2021 11:02:15"));
        when(file112.getCountryCodes()).thenReturn("      ");
        // file114
        when(file114.getId()).thenReturn(114L);
        when(file114.getName()).thenReturn("file114");
        when(file114.getComment()).thenReturn("Comment for feature 114");
        when(file114.getSeverity()).thenReturn(FunctionalitySeverity.MEDIUM);
        when(file114.getCreated()).thenReturn("114.1");
        when(file114.getProjectId()).thenReturn(projectId);
        when(file114.getParentId()).thenReturn(11L);
        when(file114.getCreationDateTime()).thenReturn(simpleDateFormat.parse("11/04/2020 11:04:20"));
        when(file114.getUpdateDateTime()).thenReturn(simpleDateFormat.parse("11/04/2021 11:04:20"));
        when(file114.getCountryCodes()).thenReturn("");
        // file1131
        when(file1131.getId()).thenReturn(1131L);
        when(file1131.getName()).thenReturn("file1131");
        when(file1131.getComment()).thenReturn("Comment for feature 1131");
        when(file1131.getSeverity()).thenReturn(FunctionalitySeverity.MEDIUM);
        when(file1131.getCreated()).thenReturn("1131.1");
        when(file1131.getProjectId()).thenReturn(projectId);
        when(file1131.getParentId()).thenReturn(113L);
        when(file1131.getCreationDateTime()).thenReturn(simpleDateFormat.parse("01/03/2020 11:31:25"));
        when(file1131.getUpdateDateTime()).thenReturn(simpleDateFormat.parse("01/03/2021 11:31:25"));
        when(file1131.getCountryCodes()).thenReturn("Es,fr");
        // file21
        when(file21.getId()).thenReturn(21L);
        when(file21.getName()).thenReturn("file21");
        when(file21.getStarted()).thenReturn(true);
        when(file21.getComment()).thenReturn("Comment for feature 21");
        when(file21.getSeverity()).thenReturn(FunctionalitySeverity.HIGH);
        when(file21.getCreated()).thenReturn("21.1");
        when(file21.getProjectId()).thenReturn(projectId);
        when(file21.getParentId()).thenReturn(2L);
        when(file21.getCreationDateTime()).thenReturn(simpleDateFormat.parse("21/10/2020 21:10:30"));
        when(file21.getUpdateDateTime()).thenReturn(simpleDateFormat.parse("21/10/2021 21:10:30"));
        when(file21.getCountryCodes()).thenReturn("es,fr,de,nl,be,it");
        // file221
        when(file221.getId()).thenReturn(221L);
        when(file221.getName()).thenReturn("file221");
        when(file221.getNotAutomatable()).thenReturn(true);
        when(file221.getComment()).thenReturn("Comment for feature 221");
        when(file221.getSeverity()).thenReturn(FunctionalitySeverity.MEDIUM);
        when(file221.getCreated()).thenReturn("221.1");
        when(file221.getProjectId()).thenReturn(projectId);
        when(file221.getParentId()).thenReturn(22L);
        when(file221.getCreationDateTime()).thenReturn(simpleDateFormat.parse("22/10/2020 22:01:45"));
        when(file221.getUpdateDateTime()).thenReturn(null);
        when(file221.getCountryCodes()).thenReturn("Es, fr,  DE,   nL    ");

        // Folders
        // root1
        when(legacyFunctionalitiesRepository.findByProjectIdAndId(projectId, 1L)).thenReturn(Optional.of(root1));
        when(root1.getName()).thenReturn("root1");
        when(root1.getParentId()).thenReturn(null);
        // directory11
        when(legacyFunctionalitiesRepository.findByProjectIdAndId(projectId, 11L)).thenReturn(Optional.of(directory11));
        when(directory11.getName()).thenReturn("directory11");
        when(directory11.getParentId()).thenReturn(1L);
        when(directory11.getProjectId()).thenReturn(projectId);
        when(directory11.getType()).thenReturn(FunctionalityType.FOLDER);
        // directory113
        when(legacyFunctionalitiesRepository.findByProjectIdAndId(projectId, 113L)).thenReturn(Optional.of(directory113));
        when(directory113.getName()).thenReturn("directory113");
        when(directory113.getParentId()).thenReturn(11L);
        when(directory113.getProjectId()).thenReturn(projectId);
        when(directory113.getType()).thenReturn(FunctionalityType.FOLDER);
        // root2
        when(legacyFunctionalitiesRepository.findByProjectIdAndId(projectId, 2L)).thenReturn(Optional.of(root2));
        when(root2.getName()).thenReturn("root2");
        when(root2.getParentId()).thenReturn(null);
        // directory22
        when(legacyFunctionalitiesRepository.findByProjectIdAndId(projectId, 22L)).thenReturn(Optional.of(directory22));
        when(directory22.getName()).thenReturn("directory22");
        when(directory22.getParentId()).thenReturn(2L);
        when(directory22.getProjectId()).thenReturn(projectId);
        when(directory22.getType()).thenReturn(FunctionalityType.FOLDER);

        // Then
        List<Feature> features = featureMigrationService.migrateFeatures(legacyProject, migrationProject);
        assertThat(features).isEqualTo(savedFeatures);
        ArgumentCaptor<List<Feature>> featuresArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(migrationFeatureRepository).saveAll(featuresArgumentCaptor.capture());
        List<Feature> capturedFeatures = featuresArgumentCaptor.getValue();
        assertThat(capturedFeatures)
                .hasSize(7)
                .extracting(
                        "code",
                        "name",
                        "description",
                        "path",
                        "position",
                        "status",
                        "priority.id.project",
                        "priority.id.code",
                        "priority.name",
                        "priority.level",
                        "versionWhenCreated",
                        "creationDateTime",
                        "updateDateTime",
                        "comment"
                )
                .containsExactly(
                        tuple(
                                "12",
                                "file12",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                "/root1",
                                1,
                                FeatureStatus.READY,
                                null,
                                null,
                                null,
                                null,
                                "12.1",
                                null,
                                LocalDateTime.of(2021, 1, 1, 1, 1, 21),
                                "Comment for feature 12"
                        ),
                        tuple(
                                "111",
                                "file111",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                "/root1/directory11",
                                1,
                                FeatureStatus.NOT_AUTOMATABLE,
                                migrationProject,
                                "low",
                                "Low",
                                3,
                                "111.1",
                                LocalDateTime.of(2020, 11, 1, 1, 11, 10),
                                LocalDateTime.of(2021, 11, 1, 1, 11, 10),
                                "Comment for feature 111"
                        ),
                        tuple(
                                "112",
                                "file112",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                "/root1/directory11",
                                2,
                                FeatureStatus.STARTED,
                                migrationProject,
                                "high",
                                "High",
                                1,
                                "112.1",
                                LocalDateTime.of(2020, 2, 11, 11, 2, 15),
                                LocalDateTime.of(2021, 2, 11, 11, 2, 15),
                                "Comment for feature 112"
                        ),
                        tuple(
                                "114",
                                "file114",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                "/root1/directory11",
                                3,
                                FeatureStatus.READY,
                                migrationProject,
                                "medium",
                                "Medium",
                                2,
                                "114.1",
                                LocalDateTime.of(2020, 4, 11, 11, 4, 20),
                                LocalDateTime.of(2021, 4, 11, 11, 4, 20),
                                "Comment for feature 114"
                        ),
                        tuple(
                                "1131",
                                "file1131",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                "/root1/directory11/directory113",
                                1,
                                FeatureStatus.READY,
                                migrationProject,
                                "medium",
                                "Medium",
                                2,
                                "1131.1",
                                LocalDateTime.of(2020, 3, 1, 11, 31, 25),
                                LocalDateTime.of(2021, 3, 1, 11, 31, 25),
                                "Comment for feature 1131"
                        ),
                        tuple(
                                "21",
                                "file21",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                "/root2",
                                1,
                                FeatureStatus.STARTED,
                                migrationProject,
                                "high",
                                "High",
                                1,
                                "21.1",
                                LocalDateTime.of(2020, 10, 21, 21, 10, 30),
                                LocalDateTime.of(2021, 10, 21, 21, 10, 30),
                                "Comment for feature 21"
                        ),
                        tuple(
                                "221",
                                "file221",
                                V2ProjectMigrationService.NEW_FIELD_GENERATION,
                                "/root2/directory22",
                                1,
                                FeatureStatus.NOT_AUTOMATABLE,
                                migrationProject,
                                "medium",
                                "Medium",
                                2,
                                "221.1",
                                LocalDateTime.of(2020, 10, 22, 22, 1, 45),
                                null,
                                "Comment for feature 221"
                        )
                );

        List<Tag> tagsFromCapturedFeature12 = capturedFeatures.stream()
                .filter(feature -> "12".equals(feature.getCode()))
                .map(Feature::getTags)
                .findFirst()
                .orElse(new ArrayList<>());
        assertThat(tagsFromCapturedFeature12)
                .hasSize(1)
                .extracting(
                        "id.project",
                        "id.code",
                        "name",
                        "description"
                )
                .containsExactly(
                        tuple(
                                migrationProject,
                                "fr",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        )
                );

        List<Tag> tagsFromCapturedFeature111 = capturedFeatures.stream()
                .filter(feature -> "111".equals(feature.getCode()))
                .map(Feature::getTags)
                .findFirst()
                .orElse(new ArrayList<>());
        assertThat(tagsFromCapturedFeature111).isNotNull().isEmpty();

        List<Tag> tagsFromCapturedFeature112 = capturedFeatures.stream()
                .filter(feature -> "112".equals(feature.getCode()))
                .map(Feature::getTags)
                .findFirst()
                .orElse(new ArrayList<>());
        assertThat(tagsFromCapturedFeature112).isNotNull().isEmpty();

        List<Tag> tagsFromCapturedFeature114 = capturedFeatures.stream()
                .filter(feature -> "114".equals(feature.getCode()))
                .map(Feature::getTags)
                .findFirst()
                .orElse(new ArrayList<>());
        assertThat(tagsFromCapturedFeature114).isNotNull().isEmpty();

        List<Tag> tagsFromCapturedFeature1131 = capturedFeatures.stream()
                .filter(feature -> "1131".equals(feature.getCode()))
                .map(Feature::getTags)
                .findFirst()
                .orElse(new ArrayList<>());
        assertThat(tagsFromCapturedFeature1131)
                .hasSize(2)
                .extracting(
                        "id.project",
                        "id.code",
                        "name",
                        "description"
                )
                .containsExactly(
                        tuple(
                                migrationProject,
                                "es",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        ),
                        tuple(
                                migrationProject,
                                "fr",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        )
                );

        List<Tag> tagsFromCapturedFeature21 = capturedFeatures.stream()
                .filter(feature -> "21".equals(feature.getCode()))
                .map(Feature::getTags)
                .findFirst()
                .orElse(new ArrayList<>());
        assertThat(tagsFromCapturedFeature21)
                .hasSize(6)
                .extracting(
                        "id.project",
                        "id.code",
                        "name",
                        "description"
                )
                .containsExactly(
                        tuple(
                                migrationProject,
                                "es",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        ),
                        tuple(
                                migrationProject,
                                "fr",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        ),
                        tuple(
                                migrationProject,
                                "de",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        ),
                        tuple(
                                migrationProject,
                                "nl",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        ),
                        tuple(
                                migrationProject,
                                "be",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        ),
                        tuple(
                                migrationProject,
                                "it",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        )
                );

        List<Tag> tagsFromCapturedFeature221 = capturedFeatures.stream()
                .filter(feature -> "221".equals(feature.getCode()))
                .map(Feature::getTags)
                .findFirst()
                .orElse(new ArrayList<>());
        assertThat(tagsFromCapturedFeature221)
                .hasSize(4)
                .extracting(
                        "id.project",
                        "id.code",
                        "name",
                        "description"
                )
                .containsExactly(
                        tuple(
                                migrationProject,
                                "es",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        ),
                        tuple(
                                migrationProject,
                                "fr",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        ),
                        tuple(
                                migrationProject,
                                "de",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        ),
                        tuple(
                                migrationProject,
                                "nl",
                                V2ProjectMigrationService.FIELD_TO_RENAME,
                                V2ProjectMigrationService.NEW_FIELD_GENERATION
                        )
                );
    }

}
