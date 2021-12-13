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

package com.decathlon.ara.lib.embed.consumer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class StructuredEmbeddingsExtractorTest {

    private StructuredEmbeddingsExtractor cut = new StructuredEmbeddingsExtractor();

    @Test
    public void extractStringData_ShouldReturnTheFirstMatchingEmbeddingValue() {
        // GIVEN
        final String html = "" +
                "<!--StructuredEmbeddings_v1.0.0=[" +
                "  {" +
                "  }," +
                "  {" +
                "    \"data\":\"someStringValue\"," +
                "    \"kind\":\"someKind\"" +
                "  }," +
                "  {" +
                "    \"kind\":\"someKind\"" +
                "  }" +
                "]-->";

        // WHEN
        final Optional<String> embedding = extract(html).extractStringData("someKind");

        // THEN
        assertThat(embedding).isEqualTo(Optional.of("someStringValue"));
    }

    @Test
    public void extract_ShouldParseAllMainFields_WhenAStructuredEmbeddingIsPresent() {
        // GIVEN
        final String html = "" +
                "<!--StructuredEmbeddings_v1.0.0=[" +
                "  {" +
                "    \"kind\":\"someKind\"," +
                "    \"name\":\"someName\"," +
                "    \"type\":\"someType\"," +
                "    \"priority\":\"somePriority\"," +
                "    \"priorityOrder\":4" +
                "  }" +
                "]-->" +
                "anyTitle<!--START:index=0-->someHtml<!--STOP:index=0-->anyTailHtml";

        // WHEN
        ParsedStructuredEmbedding embedding = extractFirstEmbedding(html);

        // THEN
        assertThat(embedding.getKind()).isEqualTo("someKind");
        assertThat(embedding.getName()).isEqualTo("someName");
        assertThat(embedding.getType()).isEqualTo("someType");
        assertThat(embedding.getPriority()).isEqualTo("somePriority");
        assertThat(embedding.getPriorityOrder()).isEqualTo(4);
        assertThat(embedding.getHtml()).isEqualTo("someHtml");
    }

    @Test
    public void extract_ShouldReturnDataAsString_WhenATextDataStructuredEmbeddingIsPresent() {
        // GIVEN
        final String html = "" +
                "<!--StructuredEmbeddings_v1.0.0=[" +
                "  {" +
                "    \"data\":\"someText\"" +
                "  }" +
                "]-->";

        // WHEN
        ParsedStructuredEmbedding embedding = extractFirstEmbedding(html);

        // THEN
        assertThat(embedding.getData()).isEqualTo("someText");
    }

    @Test
    public void extract_ShouldReturnDataAsJsonObject_WhenAnObjectDataStructuredEmbeddingIsPresent() {
        // GIVEN
        final String html = "" +
                "<!--StructuredEmbeddings_v1.0.0=[" +
                "  {" +
                "    \"data\":{" +
                "      \"key\":\"value\"," +
                "      \"array\":[" +
                "        {" +
                "          \"sub-key\":\"sub-value\"" +
                "        }" +
                "      ]" +
                "    }" +
                "  }" +
                "]-->";

        // WHEN
        ParsedStructuredEmbedding embedding = extractFirstEmbedding(html);

        // THEN
        JSONObject jsonData = (JSONObject) embedding.getData();
        assertThat(jsonData.get("key")).isEqualTo("value");
        JSONArray array = (JSONArray) jsonData.get("array");
        JSONObject subObject = (JSONObject) array.get(0);
        assertThat(subObject.get("sub-key")).isEqualTo("sub-value");
        assertThat(subObject.get("undefined-key")).isNull();
    }

    @Test
    public void extract_ShouldUnescapeStringData_WhenAStructuredEmbeddingHasHtmlCharacters() {
        // GIVEN
        final String html = "" +
                "<!--StructuredEmbeddings_v1.0.0=[" +
                "  {" +
                "    \"data\":\"&amp;amp;&#x2d;&#x2d; <test> &amp; \\\"checked'\"" +
                "  }" +
                "]-->";

        // WHEN
        ParsedStructuredEmbedding embedding = extractFirstEmbedding(html);

        // THEN
        assertThat(embedding.getData()).isEqualTo("&amp;-- <test> & \"checked'");
    }

    private StructuredEmbeddingsHolder extract(String html) {
        return cut.extract(html)
                .orElseThrow(() -> new AssertionError("There is no embedding!"));
    }

    private ParsedStructuredEmbedding extractFirstEmbedding(String html) {
        return extract(html).getEmbeddings().get(0);
    }

}
