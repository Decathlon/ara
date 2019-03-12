package com.decathlon.ara.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EmailServiceTest {

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private JavaMailSender emailSender;

    @InjectMocks
    private EmailService cut;

    @Test
    public void testCompressHtmlSpaces() {
        // GIVEN
        String html = "" +
                "<b>\n" +
                "    \r\n" +
                " <i>html  </i>\r\n" +
                "  \r\n" +
                "  \n\n\n" +
                "    </b>";

        // WHEN
        html = cut.compressHtmlSpaces(html);

        // THEN
        assertThat(html).isEqualTo("" +
                "<b>\n" +
                "<i>html </i>\n" +
                "</b>");
    }

}
