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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private JavaMailSender emailSender;

    @InjectMocks
    private EmailService cut;

    @Test
    void testCompressHtmlSpaces() {
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
