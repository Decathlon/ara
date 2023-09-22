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

package com.decathlon.ara.lib.embed.producer.type;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LinkEmbeddingTest {

    @Test
    void toHtml_ShouldReturnEmpty_WhenUrlIsNull() {
        // GIVEN
        final String nullUrl = null;
        final LinkEmbedding cut = new LinkEmbedding(null, null, nullUrl, null);

        // WHEN
        final String html = cut.toHtml();

        // THEN
        assertThat(html).isEmpty();
    }

    @Test
    void toHtml_ShouldEscapeHtmlCharacters_WhenUrlContainsHtmlCharacters() {
        // GIVEN
        final String urlWithHtml = "Escaping<test> &amp; \"checked'";
        final LinkEmbedding cut = new LinkEmbedding(null, null, urlWithHtml, null);

        // WHEN
        final String html = cut.toHtml();

        // THEN
        assertThat(html).isEqualTo("" +
                "<a href=\"Escaping&lt;test&gt; &amp;amp; &quot;checked&apos;\">SHOW</a> " +
                "(Open in a new tab if it is not opening)");
    }

}
