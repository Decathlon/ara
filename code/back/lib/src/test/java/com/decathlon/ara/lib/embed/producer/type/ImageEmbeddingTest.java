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


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ImageEmbeddingTest {

    @Test
    public void toHtml_ShouldReturnEmpty_WhenUrlIsNull() {
        // GIVEN
        final String nullUrl = null;
        final ImageEmbedding cut = new ImageEmbedding(null, null, nullUrl, null);

        // WHEN
        final String html = cut.toHtml();

        // THEN
        assertThat(html).isEmpty();
    }

    @Test
    public void toHtml_ShouldEscapeHtmlCharacters_WhenUrlContainsHtmlCharacters() {
        // GIVEN
        final String urlWithHtml = "Escaping<test> &amp; \"checked'";
        final ImageEmbedding cut = new ImageEmbedding(null, null, urlWithHtml, null);

        // WHEN
        final String html = cut.toHtml();

        // THEN
        assertThat(html).isEqualTo("" +
                "<div style=\"max-width: 100%; max-height: 400px; overflow: auto; box-shadow: 0 0 8px lightgray;\">" +
                "<img style=\"max-width: 100%;\" src=\"Escaping&lt;test&gt; &amp;amp; &quot;checked&apos;\">" +
                "</div>");
    }

}
