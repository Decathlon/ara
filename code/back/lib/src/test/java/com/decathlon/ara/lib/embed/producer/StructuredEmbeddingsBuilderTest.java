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

package com.decathlon.ara.lib.embed.producer;

import com.decathlon.ara.lib.embed.producer.type.TextEmbedding;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StructuredEmbeddingsBuilderTest {

    @Test
    public void build_ShouldReturnJsonAndHtmlEquivalents_WhenBuilderFilledWithStructuredEmbeddings() {
        // GIVEN
        StructuredEmbeddingsBuilder builder = new StructuredEmbeddingsBuilder();
        // Added out of priority order to test prioritization too
        builder.add(new TextEmbedding("someKind", "Some name", "some value", EmbeddingPriority.TECHNICAL_DEBUG_SMALL));
        builder.add(new TextEmbedding("someHiddenKind", "Some other name", "some other value", EmbeddingPriority.HIDDEN));

        // WHEN
        final String html = builder.build();

        // THEN
        assertThat(html).isEqualTo("" +
                "<!--StructuredEmbeddings_v1.0.0=[" +
                "{" +
                "\"data\":\"some value\"," +
                "\"kind\":\"someKind\"," +
                "\"name\":\"Some name\"," +
                "\"type\":\"text\"," +
                "\"priority\":\"TECHNICAL_DEBUG_SMALL\"," +
                "\"priorityOrder\":9" +
                "}," +
                "{" +
                "\"data\":\"some other value\"," +
                "\"kind\":\"someHiddenKind\"," +
                "\"name\":\"Some other name\"," +
                "\"type\":\"text\"," +
                "\"priority\":\"HIDDEN\"," +
                "\"priorityOrder\":12" +
                "}" +
                "]-->" +
                "<h3 style=\"margin: 16px 0 4px 0;\">Some name</h3>" +
                "<div>" +
                "<!--START:index=0-->" +
                "some value" +
                "<!--STOP:index=0-->" +
                "</div>");
    }

    @Test
    public void build_ShouldReturnTrue_WhenNoStructuredEmbeddingsHaveBeenAdded() {
        // GIVEN
        StructuredEmbeddingsBuilder builder = new StructuredEmbeddingsBuilder();

        // WHEN
        final boolean empty = builder.isEmpty();

        // THEN
        assertThat(empty).isTrue();
    }

    @Test
    public void build_ShouldReturnFalse_WhenAStructuredEmbeddingHaveBeenAdded() {
        // GIVEN
        StructuredEmbeddingsBuilder builder = new StructuredEmbeddingsBuilder();
        builder.add(new TextEmbedding("any", null, "any", EmbeddingPriority.HIDDEN));

        // WHEN
        final boolean empty = builder.isEmpty();

        // THEN
        assertThat(empty).isFalse();
    }

}
