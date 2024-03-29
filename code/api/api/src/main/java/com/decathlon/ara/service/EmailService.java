/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

package com.decathlon.ara.service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

    private final TemplateEngine templateEngine;

    private final Optional<JavaMailSender> emailSender;

    public EmailService(TemplateEngine templateEngine, Optional<JavaMailSender> emailSender) {
        this.templateEngine = templateEngine;
        this.emailSender = emailSender;
    }

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
        var emailSenderInit = emailSender.orElseThrow(() -> new RuntimeException("No Spring mail been configured"));
        try {
            MimeMessage message = emailSenderInit.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to.split(","));
            helper.setSubject(subject);
            helper.setText(buildHtmlContent(templateName, variables), true);
            addInlineResources(inlineResources, helper);

            emailSenderInit.send(message);
        } catch (MessagingException e) {
            LOG.error("EMAIL|Cannot send email", e);
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
