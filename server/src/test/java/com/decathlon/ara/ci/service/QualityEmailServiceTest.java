package com.decathlon.ara.ci.service;

import com.decathlon.ara.ci.bean.QualityThreshold;
import com.decathlon.ara.configuration.AraConfiguration;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.Team;
import com.decathlon.ara.domain.enumeration.QualityStatus;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.TeamRepository;
import com.decathlon.ara.service.EmailService;
import com.decathlon.ara.service.ExecutionHistoryService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.dto.execution.ExecutionDTO;
import com.decathlon.ara.service.dto.execution.ExecutionHistoryPointDTO;
import com.decathlon.ara.service.dto.quality.QualitySeverityDTO;
import com.decathlon.ara.service.dto.run.ExecutedScenarioHandlingCountsDTO;
import com.decathlon.ara.service.dto.run.RunWithQualitiesDTO;
import com.decathlon.ara.service.dto.severity.SeverityDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.support.Settings;
import com.google.common.collect.ImmutableMap;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import static com.decathlon.ara.util.TestUtil.timestamp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QualityEmailServiceTest {

    private static final Timestamp THE_APRIL_25TH_2017_AT_12_H_57_MIN_56_S = timestamp(2017, Calendar.APRIL, 25, 12, 57, 56);

    @Mock
    private AraConfiguration araConfiguration;

    @Mock
    private ExecutionHistoryService executionHistoryService;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SettingService settingService;

    @Spy
    @InjectMocks
    private QualityEmailService cut;

    @Test
    public void sendQualityEmail_should_not_call_sendHtmlMessage_when_no_recipient_address_configured() throws NotFoundException {
        // GIVEN
        long anyProjectId = -42;
        long executionId = 1;
        ExecutionHistoryPointDTO execution = new ExecutionHistoryPointDTO();
        when(executionHistoryService.getExecution(anyProjectId, executionId)).thenReturn(execution);

        // WHEN
        cut.sendQualityEmail(anyProjectId, executionId);

        // THEN
        verify(emailService, never()).sendHtmlMessage(anyString(), anyString(), anyString(), anyString(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void sendQualityEmail_should_call_sendHtmlMessage_with_the_expected_parameters() throws NotFoundException {
        // GIVEN
        long projectId = 42;
        long executionId = 1;
        Date buildDateTime = new Date();
        Date testDateTime = new Date();
        ExecutionHistoryPointDTO execution = new ExecutionHistoryPointDTO()
                .withRuns(Collections.emptyList());
        execution.setId(Long.valueOf(1));
        execution.setBuildDateTime(buildDateTime);
        execution.setTestDateTime(testDateTime);
        when(executionHistoryService.getExecution(projectId, executionId)).thenReturn(execution);
        when(settingService.get(projectId, Settings.EMAIL_FROM)).thenReturn("from");
        when(settingService.get(projectId, Settings.EMAIL_TO_EXECUTION_CRASHED)).thenReturn("to");
        List<Team> teams = Arrays.asList(
                new Team().withId(Long.valueOf(1)).withAssignableToProblems(true),
                new Team().withId(Long.valueOf(2)).withAssignableToProblems(false));
        when(teamRepository.findAllByProjectIdOrderByName(projectId)).thenReturn(teams);
        when(projectRepository.findById(Long.valueOf(projectId))).thenReturn(Optional.of(new Project()
                .withCode("projectCode")
                .withName("theProjectName")));
        Map<String, Map<String, ExecutedScenarioHandlingCountsDTO>> quality = new HashMap<>();
        doReturn(quality).when(cut).aggregateQualitiesPerTeamAndSeverity(execution);
        doReturn("subject").when(cut).getSubject("theProjectName", execution);
        doReturn("build").when(cut).formatDate(same(buildDateTime));
        doReturn("test").when(cut).formatDate(same(testDateTime));
        doReturn("eligibility").when(cut).getEligibilityMessage(same(execution));
        when(araConfiguration.getClientBaseUrl()).thenReturn("url");
        ArgumentCaptor<String> argumentFrom = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argumentTo = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argumentSubject = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argumentTemplateName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> argumentVariables = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map> argumentInlineResources = ArgumentCaptor.forClass(Map.class);
        doNothing().when(emailService).sendHtmlMessage(
                argumentFrom.capture(),
                argumentTo.capture(),
                argumentSubject.capture(),
                argumentTemplateName.capture(),
                argumentVariables.capture(),
                argumentInlineResources.capture());

        // WHEN
        cut.sendQualityEmail(projectId, executionId);

        // THEN
        assertThat(argumentFrom.getValue()).isEqualTo("from");
        assertThat(argumentTo.getValue()).isEqualTo("to");
        assertThat(argumentSubject.getValue()).contains("subject");
        assertThat(argumentTemplateName.getValue()).isEqualTo("execution-quality-status");
        Map<String, Object> variables = argumentVariables.getValue();
        assertThat(variables.get("execution")).isSameAs(execution);
        final List<Team> usedTeams = (List<Team>) variables.get("teamsAssignableToProblems");
        assertThat(usedTeams).hasSize(1);
        assertThat(usedTeams.get(0).getId()).isEqualTo(1);
        assertThat(variables.get("qualitiesPerTeamAndSeverity")).isSameAs(quality);
        assertThat(((Team) variables.get("NO_TEAM")).getId()).isEqualTo(-404);
        assertThat(((Team) variables.get("NO_TEAM")).getName()).isEqualTo("(No team)");
        assertThat(variables.get("buildDate")).isEqualTo("build");
        assertThat(variables.get("testDate")).isEqualTo("test");
        assertThat(variables.get("eligibilityMessage")).isEqualTo("eligibility");
        assertThat(variables.get("executionUrl")).isEqualTo("url#/projects/projectCode/executions/1");
        assertThat(variables.get("projectName")).isEqualTo("theProjectName");
        Map<String, Resource> inlineResources = argumentInlineResources.getValue();
        assertThat(inlineResources.get("favicon.png")).isNotNull();
    }

    @Test
    public void addInlineResource_should_do_nothing_and_not_crash_when_resource_not_found() {
        // GIVEN
        Map<String, Resource> inlineResources = ImmutableMap.of();

        // WHEN
        cut.addInlineResource(inlineResources, "any", "unexisting");

        // THEN
        // inlineResources is an immutable map: if it didn't crash, it didn't attempt to put any value in the map
    }

    @Test
    public void addInlineResource_should_add_the_found_resource() {
        // GIVEN
        Map<String, Resource> inlineResources = new HashMap<>();

        // WHEN
        cut.addInlineResource(inlineResources, "name.txt", "templates/some.txt");

        // THEN
        assertThat(inlineResources).containsOnlyKeys("name.txt");
        assertThat(((ByteArrayResource) inlineResources.get("name.txt")).getByteArray()).isNotEmpty();
    }

    @Test
    public void formatDate_should_return_empty_string_when_date_is_null() {
        // WHEN
        final String formattedDate = cut.formatDate(null);

        // THEN
        assertThat(formattedDate).isEmpty();
    }

    @Test
    public void formatDate_should_return_formatted_date() {
        // GIVEN
        final Locale initialDefaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ENGLISH);

            // WHEN
            final String formattedDate = cut.formatDate(THE_APRIL_25TH_2017_AT_12_H_57_MIN_56_S);

            // THEN
            assertThat(formattedDate).isEqualTo("Apr 25, 2017 - 12:57");
        } finally {
            Locale.setDefault(initialDefaultLocale);
        }
    }

    @Test
    public void formatDate_should_return_formatted_date_in_english_locale() {
        // GIVEN
        final Locale initialDefaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.FRENCH);

            // WHEN
            final String formattedDate = cut.formatDate(THE_APRIL_25TH_2017_AT_12_H_57_MIN_56_S);

            // THEN
            assertThat(formattedDate).isEqualTo("Apr 25, 2017 - 12:57");
        } finally {
            Locale.setDefault(initialDefaultLocale);
        }
    }

    @Test
    public void getSubject_should_work_when_incomplete_and_not_blocking() {
        // GIVEN
        ExecutionDTO execution = validExecution()
                .withBranch("branch")
                .withName("name")
                .withRelease("release")
                .withQualityStatus(QualityStatus.INCOMPLETE);

        // WHEN
        final String subject = cut.getSubject("project", execution);

        // THEN
        assertThat(subject).isEqualTo("PROJECT BRANCH/NAME NRT FOR RELEASE: RAN (INCOMPLETE) TESTED ON APR 25, 2017 - 12:57");
    }

    private ExecutionDTO validExecution() {
        return new ExecutionDTO()
                .withQualitySeverities(Collections.singletonList(new QualitySeverityDTO().withSeverity(new SeverityDTO())))
                .withQualityThresholds(Collections.singletonMap("any", new QualityThreshold()))
                .withTestDateTime(THE_APRIL_25TH_2017_AT_12_H_57_MIN_56_S);
    }

    @Test
    public void getRecipient_should_return_crashed_email_address_for_crashed_execution() {
        // GIVEN
        long projectId = 42;
        ExecutionDTO execution = new ExecutionDTO();
        when(settingService.get(projectId, Settings.EMAIL_TO_EXECUTION_CRASHED)).thenReturn("to");

        // WHEN
        final Optional<String> to = cut.getRecipient(projectId, execution);

        // THEN
        assertThat(to.orElse("absent")).isEqualTo("to");
    }

    @Test
    public void getRecipient_should_return_ran_email_address_for_valid_and_not_blocking_execution() {
        // GIVEN
        long projectId = 42;
        ExecutionDTO execution = validExecution();
        when(settingService.get(projectId, Settings.EMAIL_TO_EXECUTION_RAN)).thenReturn("to");

        // WHEN
        final Optional<String> to = cut.getRecipient(projectId, execution);

        // THEN
        assertThat(to.orElse("absent")).isEqualTo("to");
    }

    @Test
    public void getRecipient_should_return_eligible_passed_email_address_for_blocking_and_passed_execution() {
        // GIVEN
        long projectId = 42;
        ExecutionDTO execution = validExecution()
                .withBlockingValidation(true)
                .withQualityStatus(QualityStatus.PASSED);
        when(settingService.get(projectId, Settings.EMAIL_TO_EXECUTION_ELIGIBLE_PASSED)).thenReturn("to");

        // WHEN
        final Optional<String> to = cut.getRecipient(projectId, execution);

        // THEN
        assertThat(to.orElse("absent")).isEqualTo("to");
    }

    @Test
    public void getRecipient_should_return_eligible_warning_email_address_for_blocking_and_warning_execution() {
        // GIVEN
        long projectId = 42;
        ExecutionDTO execution = validExecution()
                .withBlockingValidation(true)
                .withQualityStatus(QualityStatus.WARNING);
        when(settingService.get(projectId, Settings.EMAIL_TO_EXECUTION_ELIGIBLE_WARNING)).thenReturn("to");

        // WHEN
        final Optional<String> to = cut.getRecipient(projectId, execution);

        // THEN
        assertThat(to.orElse("absent")).isEqualTo("to");
    }

    @Test
    public void getRecipient_should_return_not_eligible_email_address_for_blocking_and_not_acceptable_execution() {
        // GIVEN
        long projectId = 42;
        ExecutionDTO execution = validExecution()
                .withBlockingValidation(true)
                .withQualityStatus(QualityStatus.FAILED);
        when(settingService.get(projectId, Settings.EMAIL_TO_EXECUTION_NOT_ELIGIBLE)).thenReturn("to");

        // WHEN
        final Optional<String> to = cut.getRecipient(projectId, execution);

        // THEN
        assertThat(to.orElse("absent")).isEqualTo("to");
    }

    @Test
    public void getRecipient_should_allow_undefined_properties() {
        // GIVEN
        long projectId = 42;
        ExecutionDTO execution = validExecution()
                .withBlockingValidation(true)
                .withQualityStatus(QualityStatus.FAILED);
        when(settingService.get(projectId, Settings.EMAIL_TO_EXECUTION_NOT_ELIGIBLE)).thenReturn("");

        // WHEN
        final Optional<String> to = cut.getRecipient(projectId, execution);

        // THEN
        assertThat(to.isPresent()).isFalse();
    }

    @Test
    public void getSubject_should_show_RAN_when_not_blocking() {
        // GIVEN
        ExecutionDTO execution = validExecution()
                .withQualityStatus(QualityStatus.WARNING);

        // WHEN
        final String subject = cut.getSubject("anyProjectName", execution);

        // THEN
        assertThat(subject).contains(": RAN (");
    }

    @Test
    public void getSubject_should_show_ELIGIBLE_when_blocking_and_WARNING() {
        // GIVEN
        ExecutionDTO execution = validExecution()
                .withBlockingValidation(true)
                .withQualityStatus(QualityStatus.WARNING);

        // WHEN
        final String subject = cut.getSubject("anyProjectName", execution);

        // THEN
        assertThat(subject).contains(": \u26A0\uFE0F ELIGIBLE (");
    }

    @Test
    public void getSubject_should_show_ELIGIBLE_when_blocking_and_PASSED() {
        // GIVEN
        ExecutionDTO execution = validExecution()
                .withBlockingValidation(true)
                .withQualityStatus(QualityStatus.PASSED);

        // WHEN
        final String subject = cut.getSubject("anyProjectName", execution);

        // THEN
        assertThat(subject).contains(": \u2705 ELIGIBLE (");
    }

    @Test
    public void getSubject_should_show_NOT_ELIGIBLE_when_blocking_and_FAILED() {
        // GIVEN
        ExecutionDTO execution = validExecution()
                .withBlockingValidation(true)
                .withQualityStatus(QualityStatus.FAILED);

        // WHEN
        final String subject = cut.getSubject("anyProjectName", execution);

        // THEN
        assertThat(subject).contains(": \u274C NOT ELIGIBLE (");
    }

    @Test
    public void getSubject_should_show_NOT_ELIGIBLE_when_blocking_and_INCOMPLETE() {
        // GIVEN
        ExecutionDTO execution = validExecution()
                .withBlockingValidation(true)
                .withQualityStatus(QualityStatus.INCOMPLETE);

        // WHEN
        final String subject = cut.getSubject("anyProjectName", execution);

        // THEN
        assertThat(subject).contains(": \u274C NOT ELIGIBLE (");
    }

    @Test
    public void getSubject_should_include_all_severity_percentages() {
        // GIVEN
        ExecutionDTO execution = validExecution()
                .withBranch("branch")
                .withName("name")
                .withRelease("release")
                .withQualityStatus(QualityStatus.PASSED)
                .withQualitySeverities(Arrays.asList(
                        new QualitySeverityDTO()
                                .withSeverity(new SeverityDTO().withInitials("S1"))
                                .withPercent(21),
                        new QualitySeverityDTO()
                                .withSeverity(new SeverityDTO().withInitials("S2"))
                                .withPercent(42),
                        new QualitySeverityDTO()
                                .withSeverity(new SeverityDTO().withInitials("S3"))
                                .withPercent(84)));

        // WHEN
        final String subject = cut.getSubject("project", execution);

        // THEN
        assertThat(subject).isEqualTo("PROJECT BRANCH/NAME NRT FOR RELEASE: RAN (S1 21 % | S2 42 % | S3 84 %) TESTED ON APR 25, 2017 - 12:57");
    }

    @Test
    public void getSubject_should_show_CRASHED_when_null_qualitySeverities() {
        // GIVEN
        ExecutionDTO execution = validExecution().withQualitySeverities(null);

        // WHEN
        final String subject = cut.getSubject("anyProjectName", execution);

        // THEN
        assertThat(subject).contains(": \uD83D\uDCA5 CRASHED, TESTED ON ");
    }

    @Test
    public void getSubject_should_show_CRASHED_when_empty_qualitySeverities() {
        // GIVEN
        ExecutionDTO execution = validExecution().withQualitySeverities(Collections.emptyList());

        // WHEN
        final String subject = cut.getSubject("anyProjectName", execution);

        // THEN
        assertThat(subject).contains(": \uD83D\uDCA5 CRASHED, TESTED ON ");
    }

    @Test
    public void getSubject_should_show_CRASHED_when_null_qualityThresholds() {
        // GIVEN
        ExecutionDTO execution = validExecution().withQualityThresholds(null);

        // WHEN
        final String subject = cut.getSubject("anyProjectName", execution);

        // THEN
        assertThat(subject).contains(": \uD83D\uDCA5 CRASHED, TESTED ON ");
    }

    @Test
    public void getSubject_should_show_CRASHED_when_empty_qualityThresholds() {
        // GIVEN
        ExecutionDTO execution = validExecution().withQualityThresholds(Collections.emptyMap());

        // WHEN
        final String subject = cut.getSubject("anyProjectName", execution);

        // THEN
        assertThat(subject).contains(": \uD83D\uDCA5 CRASHED, TESTED ON ");
    }

    @Test
    public void getEligibilityMessage_should_not_include_eligibility_status_when_not_isBlockingValidation() {
        // GIVEN
        ExecutionDTO execution = new ExecutionDTO().withBlockingValidation(false);

        // WHEN
        final String message = cut.getEligibilityMessage(execution);

        // THEN
        assertThat(message).isEqualTo("Quality for this build:");
    }

    @Test
    public void getEligibilityMessage_should_be_eligible_when_isBlockingValidation_and_passed() {
        // GIVEN
        ExecutionDTO execution = new ExecutionDTO().withBlockingValidation(true).withQualityStatus(QualityStatus.PASSED);

        // WHEN
        final String message = cut.getEligibilityMessage(execution);

        // THEN
        assertThat(message).contains(">eligible to deploy<");
        assertThat(message).doesNotContain(">without much margin regarding the thresholds<");
    }

    @Test
    public void getEligibilityMessage_should_be_eligible_with_warning_message_when_isBlockingValidation_and_warning() {
        // GIVEN
        ExecutionDTO execution = new ExecutionDTO().withBlockingValidation(true).withQualityStatus(QualityStatus.WARNING);

        // WHEN
        final String message = cut.getEligibilityMessage(execution);

        // THEN
        assertThat(message).contains(">eligible to deploy<");
        assertThat(message).contains(">without much margin regarding the thresholds<");
    }

    @Test
    public void getEligibilityMessage_should_not_be_eligible_when_isBlockingValidation_and_failed() {
        // GIVEN
        ExecutionDTO execution = new ExecutionDTO().withBlockingValidation(true).withQualityStatus(QualityStatus.FAILED);

        // WHEN
        final String message = cut.getEligibilityMessage(execution);

        // THEN
        assertThat(message).contains(">not eligible to deploy<");
    }

    @Test
    public void getEligibilityMessage_should_not_be_eligible_when_isBlockingValidation_and_incomplete() {
        // GIVEN
        ExecutionDTO execution = new ExecutionDTO().withBlockingValidation(true).withQualityStatus(QualityStatus.INCOMPLETE);

        // WHEN
        final String message = cut.getEligibilityMessage(execution);

        // THEN
        assertThat(message).contains(">not eligible to deploy<");
    }

    @Test
    public void aggregateQualitiesPerTeamAndSeverity_should_aggregate_team_qualities() {
        // GIVEN
        final String SEVERITY_1 = "severity1";
        final String SEVERITY_2 = "severity2";
        final String SEVERITY_3 = "severity3";
        final String SEVERITY_ALL = "*";

        Map<String, Map<String, ExecutedScenarioHandlingCountsDTO>> quality1 = new HashMap<>();
        {
            Map<String, ExecutedScenarioHandlingCountsDTO> quality1team1 = new HashMap<>();
            quality1team1.put(SEVERITY_1, new ExecutedScenarioHandlingCountsDTO(1, 2, 3));
            quality1team1.put(SEVERITY_2, new ExecutedScenarioHandlingCountsDTO(0, 0, 1));
            quality1team1.put(SEVERITY_ALL, new ExecutedScenarioHandlingCountsDTO(1, 2, 4));
            quality1.put("1", quality1team1);
        }
        {
            Map<String, ExecutedScenarioHandlingCountsDTO> quality1team2 = new HashMap<>();
            quality1team2.put(SEVERITY_1, new ExecutedScenarioHandlingCountsDTO(3, 2, 0));
            quality1team2.put(SEVERITY_ALL, new ExecutedScenarioHandlingCountsDTO(3, 2, 0));
            quality1.put("2", quality1team2);
        }

        Map<String, Map<String, ExecutedScenarioHandlingCountsDTO>> quality2 = new HashMap<>();
        {
            Map<String, ExecutedScenarioHandlingCountsDTO> quality2team1 = new HashMap<>();
            quality2team1.put(SEVERITY_2, new ExecutedScenarioHandlingCountsDTO(0, 0, 1));
            quality2team1.put(SEVERITY_ALL, new ExecutedScenarioHandlingCountsDTO(0, 0, 1));
            quality2.put("1", quality2team1);
        }
        {
            quality2.put("2", new HashMap<>());
        }

        Map<String, Map<String, ExecutedScenarioHandlingCountsDTO>> quality3 = new HashMap<>();
        {
            Map<String, ExecutedScenarioHandlingCountsDTO> quality3team1 = new HashMap<>();
            quality3team1.put(SEVERITY_3, new ExecutedScenarioHandlingCountsDTO(0, 1, 0));
            quality3team1.put(SEVERITY_ALL, new ExecutedScenarioHandlingCountsDTO(0, 1, 0));
            quality3.put("2", quality3team1);
        }

        ExecutionHistoryPointDTO execution = new ExecutionHistoryPointDTO()
                .withRuns(Arrays.asList(
                        new RunWithQualitiesDTO().withQualitiesPerTeamAndSeverity(quality1),
                        new RunWithQualitiesDTO().withQualitiesPerTeamAndSeverity(quality2),
                        new RunWithQualitiesDTO().withQualitiesPerTeamAndSeverity(quality3)));

        // WHEN
        final Map<String, Map<String, ExecutedScenarioHandlingCountsDTO>> aggregatedQualities = cut.aggregateQualitiesPerTeamAndSeverity(execution);

        // THEN
        assertThat(aggregatedQualities).containsOnlyKeys("1", "2"); // Team Ids

        assertThat(aggregatedQualities.get("1")).containsOnlyKeys(SEVERITY_1, SEVERITY_2, SEVERITY_ALL);
        assertThat(aggregatedQualities.get("1").get(SEVERITY_1)).isEqualTo(new ExecutedScenarioHandlingCountsDTO(1, 2, 3));
        assertThat(aggregatedQualities.get("1").get(SEVERITY_2)).isEqualTo(new ExecutedScenarioHandlingCountsDTO(0, 0, 2));
        assertThat(aggregatedQualities.get("1").get(SEVERITY_ALL)).isEqualTo(new ExecutedScenarioHandlingCountsDTO(1, 2, 5));

        assertThat(aggregatedQualities.get("2")).containsOnlyKeys(SEVERITY_1, SEVERITY_3, SEVERITY_ALL);
        assertThat(aggregatedQualities.get("2").get(SEVERITY_1)).isEqualTo(new ExecutedScenarioHandlingCountsDTO(3, 2, 0));
        assertThat(aggregatedQualities.get("2").get(SEVERITY_3)).isEqualTo(new ExecutedScenarioHandlingCountsDTO(0, 1, 0));
        assertThat(aggregatedQualities.get("2").get(SEVERITY_ALL)).isEqualTo(new ExecutedScenarioHandlingCountsDTO(3, 3, 0));
    }

}
