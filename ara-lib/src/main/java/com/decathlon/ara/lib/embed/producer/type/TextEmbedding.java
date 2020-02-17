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

import com.decathlon.ara.lib.embed.producer.EmbeddingPriority;
import com.decathlon.ara.lib.embed.producer.StructuredEmbedding;
import org.apache.commons.text.StringEscapeUtils;

/**
 * Embed a named piece of text.
 */
public class TextEmbedding extends StructuredEmbedding {

    public TextEmbedding(String kind, String name, String text, EmbeddingPriority priority) {
        super(kind, name, "text", text, priority);
    }

    /**
     * @return the text escaped to be included in an HTML content (can be empty, but never null)
     */
    @Override
    public String toHtml() {
        Object text = getData();
        return (text == null ? "" : StringEscapeUtils.escapeXml10(text.toString()));
    }

}
