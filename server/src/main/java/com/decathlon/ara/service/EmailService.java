package com.decathlon.ara.service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EmailService {

    @NonNull
    private final TemplateEngine templateEngine;

    @NonNull
    private final JavaMailSender emailSender;

    /**
     * Send an HTML mail message.
     *
     * @param from            the email address of the sender of the mail
     * @param to              the email address of the recipient(s) to which to send the mail
     * @param subject         the subject line of the email
     * @param templateName    the name of the HTML template to use to generate the email body
     * @param variables       the optional variables to be used by the template to generate the email body
     * @param inlineResources the optional (can be null and can be empty) map of inline resources (images...) to be used by the template (the key is the name to use in the template: eg. key="signature.png" to use it as "&lt;img src="cid:signature.png"/&gt;" in the template)
     */
    public void sendHtmlMessage(String from, String to, String subject, String templateName, Map<String, Object> variables, Map<String, Resource> inlineResources) {
        try {
            MimeMessage message = emailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to.split(","));
            helper.setSubject(subject);
            helper.setText(buildHtmlContent(templateName, variables), true);
            addInlineResources(inlineResources, helper);

            emailSender.send(message);
        } catch (MessagingException e) {
            log.error("Cannot send email", e);
        }
    }

    private String buildHtmlContent(String templateName, Map<String, Object> variables) {
        final Context context = new Context();
        context.setVariables(variables);
        String html = templateEngine.process("mail/html/" + templateName + ".html", context);
        html = compressHtmlSpaces(html);
        return html;
    }

    String compressHtmlSpaces(String html) {
        // Very basic, but we go from 100 kB down to 60 kB:
        // enough to go below the 102 kB GMail clipping:
        // https://emailmonks.com/blog/email-design/avoid-gmail-clipping/
        html = html.replace("\r\n", "\n");
        html = replaceAll(html, "  ", " ");
        html = replaceAll(html, "\n ", "\n");
        html = replaceAll(html, "\n\n", "\n");
        // IMPORTANT: keep order!
        return html;
    }

    private String replaceAll(String html, String target, String replacement) {
        int previousLength;
        do {
            previousLength = html.length();
            html = html.replace(target, replacement);
        } while (previousLength != html.length());
        return html;
    }

    private void addInlineResources(Map<String, Resource> inlineResources, MimeMessageHelper helper) throws MessagingException {
        if (inlineResources != null) {
            for (Map.Entry<String, Resource> entry : inlineResources.entrySet()) {
                String name = entry.getKey();
                Resource resource = entry.getValue();
                String mimeType = MimeMappings.DEFAULT.get(FilenameUtils.getExtension(name));
                helper.addInline(name, resource, mimeType);
            }
        }
    }

}
