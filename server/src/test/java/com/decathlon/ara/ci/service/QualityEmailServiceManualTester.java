package com.decathlon.ara.ci.service;

import com.decathlon.ara.configuration.AraConfiguration;
import com.decathlon.ara.domain.Project;
import com.decathlon.ara.domain.Team;
import com.decathlon.ara.repository.ProjectRepository;
import com.decathlon.ara.repository.TeamRepository;
import com.decathlon.ara.service.EmailService;
import com.decathlon.ara.service.ExecutionHistoryService;
import com.decathlon.ara.service.SettingService;
import com.decathlon.ara.service.dto.execution.ExecutionHistoryPointDTO;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.service.support.Settings;
import com.decathlon.ara.util.TestUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * This class is not a unit-test: it is meant to run manually, to debug the writing of the mail template (this is why
 * the class-name ends with "Tester" and not "Test" nor "IT").
 */
@RunWith(MockitoJUnitRunner.class)
public class QualityEmailServiceManualTester {

    @Mock
    private AraConfiguration araConfiguration;

    @Mock
    private ExecutionHistoryService executionHistoryService;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SettingService settingService;

    @Spy
    private TemplateEngine templateEngine = new SpringTemplateEngine();

    @Spy
    private JavaMailSender emailSender = new JavaMailSenderImpl();

    private QualityEmailService cut;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() throws IOException {
        String from = System.getProperty("from");
        if (StringUtils.isEmpty(from)) {
            throw new AssertionError("You need to set your email address (or a technical one) " +
                    "in the 'from' system property, eg. with the VM argument -D" + "from=me@company.com");
        }

        String to = System.getProperty("to");
        if (StringUtils.isEmpty(to)) {
            throw new AssertionError("You need to set your email address " +
                    "in the 'to' system property, eg. with the VM argument -D" + "to=me@company.com");
        }

        EmailService emailService = new EmailService(templateEngine, emailSender);

        cut = new QualityEmailService(araConfiguration, executionHistoryService, teamRepository, emailService, projectRepository, settingService);

        when(araConfiguration.getClientBaseUrl()).thenReturn("http://localhost:8081/");
        when(projectRepository.findById(Long.valueOf(1))).thenReturn(Optional.of(new Project()
                .withCode("projectCode")
                .withName("projectName")));
        when(settingService.get(1, Settings.EMAIL_FROM)).thenReturn("ARA Manual Tester <" + from + ">");
        when(settingService.get(1, Settings.EMAIL_TO_EXECUTION_RAN)).thenReturn(to);
        when(settingService.get(1, Settings.EMAIL_TO_EXECUTION_ELIGIBLE_WARNING)).thenReturn(to);
        when(settingService.get(1, Settings.EMAIL_TO_EXECUTION_NOT_ELIGIBLE)).thenReturn(to);

        InputStream inputStream = TestUtil.openResourceStream("mailing-test/teams.json");
        List<Team> teams = objectMapper.readValue(inputStream, new TypeReference<List<Team>>() {
        });

        when(teamRepository.findAllByProjectIdOrderByName(anyLong())).thenReturn(teams);

        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setOrder(Integer.valueOf(1));
        templateResolver.setResolvablePatterns(Collections.singleton("mail/html/*"));
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        templateResolver.setCacheable(false);
        templateEngine.setTemplateResolver(templateResolver);
    }

    @Test
    public void testSendMessageWithSucceedBlocking() throws IOException, NotFoundException {
        send("execution-succeed-blocking.json");
    }

    @Test
    public void testSendMessageWithFailedNonBlocking() throws IOException, NotFoundException {
        send("execution-failed-not-blocking.json");
    }

    @Test
    public void testSendBiggestMessage() throws IOException, NotFoundException {
        send("execution-biggest.json");
    }

    private void send(String fileName) throws IOException, NotFoundException {
        InputStream inputStream = TestUtil.openResourceStream("mailing-test/" + fileName);
        ExecutionHistoryPointDTO execution = objectMapper.readValue(inputStream, ExecutionHistoryPointDTO.class);
        when(executionHistoryService.getExecution(1, 1)).thenReturn(execution);

        cut.sendQualityEmail(1, 1);
    }

}
