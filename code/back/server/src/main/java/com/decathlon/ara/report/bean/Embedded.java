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

package com.decathlon.ara.report.bean;

import com.decathlon.ara.lib.embed.consumer.StructuredEmbeddingsExtractor;
import com.decathlon.ara.lib.embed.consumer.StructuredEmbeddingsHolder;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class Embedded {

    @JsonProperty("mime_type")
    private String mimeType;

    private String data;

    public Optional<String> getDecodedDataAsString() {
        return getDecodedDataAsBytes().map(bytes -> new String(bytes, StandardCharsets.UTF_8));
    }

    public Optional<byte[]> getDecodedDataAsBytes() {
        return Optional.ofNullable(data).filter(StringUtils::isNotEmpty).map(d -> Base64.getDecoder().decode(d));
    }

    /**
     * @return the extracted screenshot, if the embedding is an image
     */
    public Optional<byte[]> extractScreenshot() {
        if ("image/png".equals(mimeType)) {
            return getDecodedDataAsBytes();
        }
        return Optional.empty();
    }

    /**
     * @return the extracted video link ("http...mp4"), if the embedding is a video URL
     */
    public Optional<String> extractVideoUrl() {
        if ("text/plain".equals(getMimeType())) {
            final Optional<String> textContent = getDecodedDataAsString();
            if (textContent.isPresent() && textContent.get().startsWith("http") && textContent.get().endsWith(".mp4")) {
                return textContent;
            }
        }
        return Optional.empty();
    }

    /**
     * @return the extracted structured-embedding, if the embedding is one
     */
    public Optional<StructuredEmbeddingsHolder> extractStructuredEmbeddings() {
        if ("text/html".equals(getMimeType())) {
            final Optional<String> textContent = getDecodedDataAsString();
            if (textContent.isPresent()) {
                final Optional<StructuredEmbeddingsHolder> maybeEmbeddings =
                        new StructuredEmbeddingsExtractor().extract(textContent.get());
                if (maybeEmbeddings.isPresent()) {
                    return maybeEmbeddings;
                }
            }
        }
        return Optional.empty();
    }

}
