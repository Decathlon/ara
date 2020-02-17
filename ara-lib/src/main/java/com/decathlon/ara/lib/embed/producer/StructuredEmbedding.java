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

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Base class for structured embeddings to attach to Cucumber scenarios.<br>
 * Implementations barely only need to implement {@link #toHtml()} and a constructor that passes a hard-coded type to
 * its parent constructor.
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class StructuredEmbedding {

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
     * The type of data supported by StructuredEmbedding sub-classes.<br>
     * A machine that does not recognize the {@link #kind} can still display it in a generic manner.<br>
     * Each implementation of this class must provide a unique type.
     */
    private final String type;

    /**
     * Data of this embedding: a text or URL most of the times, but can be JSON-serializable objects for other types.
     */
    private final Object data;

    /**
     * The priority to use to sort all embeddings of a given scenario.
     *
     * @see EmbeddingPriority EmbeddingPriority for possible values
     */
    private final EmbeddingPriority priority;

    /**
     * @return the HTML representation of the embedding, for human display (any user data must be HTML escaped)
     */
    public abstract String toHtml();

}
