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

import com.decathlon.ara.lib.embed.producer.EmbeddingPriority;
import lombok.Data;

/**
 * Represents one Structured Embedding parsed from an HTML string containing one or more structured embeddings.
 *
 * @see StructuredEmbeddingsHolder StructuredEmbeddingsHolder is holding all embeddings from the same HTML string
 */
@Data
public class ParsedStructuredEmbedding {

    /**
     * A machine-readable identifier for the content to embed. It is not needed to be unique.<br>
     * Eg. "accessibilityReports" (there could be several embeddings with kind "accessibilityReports").
     */
    private final String kind;

    /**
     * A human-readable name for the content to embed. Eg. "Accessibility Reports".
     */
    private final String name;

    /**
     * The type of data supported by StructuredEmbedding sub-classes. A machine that does not recognize the kind can still display it in a generic manner.
     */
    private final String type;

    /**
     * The data of this embedding: a text or URL most of the times, but can be JSON-serializable objects for other types.
     */
    private final Object data;

    /**
     * @see EmbeddingPriority
     */
    private final String priority;

    /**
     * @see EmbeddingPriority#ordinal()
     */
    private final int priorityOrder;

    /**
     * The HTML representation of the embedding, for human display
     */
    private final String html;

}
