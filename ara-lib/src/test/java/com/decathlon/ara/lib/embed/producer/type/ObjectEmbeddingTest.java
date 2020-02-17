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

import lombok.Data;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectEmbeddingTest {

    @Test
    public void toHtml_ShouldReturnEmpty_WhenDataIsNull() {
        // GIVEN
        final Object nullData = null;
        final ObjectEmbedding cut = new ObjectEmbedding(null, null, nullData, null);

        // WHEN
        final String html = cut.toHtml();

        // THEN
        assertThat(html).isEmpty();
    }

    @Test
    public void toHtml_ShouldEncodeDataAsJsonAndEscapeHtmlCharacters_WhenDataIsPresent() {
        // GIVEN
        final SomeClass data = new SomeClass();
        data.child = new SomeClass();
        data.child.text = "sub-text";
        data.text = "Escaping<test> &amp; \"checked'";
        data.number = 42;
        final ObjectEmbedding cut = new ObjectEmbedding(null, null, data, null);

        // WHEN
        final String html = cut.toHtml();

        // THEN
        assertThat(html).isEqualTo("" +
                "{&quot;number&quot;:42," +
                "&quot;text&quot;:&quot;Escaping&lt;test&gt; &amp;amp; \\&quot;checked&apos;&quot;," +
                "&quot;child&quot;:{&quot;number&quot;:0,&quot;text&quot;:&quot;sub-text&quot;,&quot;child&quot;:null}}");
    }

    @Data
    public static class SomeClass {

        SomeClass child;
        String text;
        int number;

    }

}
