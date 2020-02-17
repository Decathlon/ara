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
import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;
import org.apache.commons.text.StringEscapeUtils;

/**
 * Embed a Java object, serialized as a JSON object, both as JSON and HTML representations.<br>
 * Use {@link EmbeddingPriority#HIDDEN} to not display the serialized object in the HTML representation, if not desired.
 */
public class ObjectEmbedding extends StructuredEmbedding {

    public ObjectEmbedding(String kind, String name, Object object, EmbeddingPriority priority) {
        super(kind, name, "object", object, priority);
    }

    /**
     * @return the object serialized as a JSON object; returns an empty string if the object is null
     */
    @Override
    public String toHtml() {
        Object object = getData();
        if (object == null) {
            return "";
        }
        return StringEscapeUtils.escapeXml10(JSONValue.toJSONString(object, JSONStyle.NO_COMPRESS));
    }

}
