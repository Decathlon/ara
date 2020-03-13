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
 * Embed an image.<br>
 * The HTML representation is cropped with scrollbars if too high, to not cast shadow to other embeddings.
 */
public class ImageEmbedding extends StructuredEmbedding {

    public ImageEmbedding(String kind, String name, String imageUrl, EmbeddingPriority priority) {
        super(kind, name, "image", imageUrl, priority);
    }

    /**
     * @return an image tag (scrollable it it is too height) with a box shadow (to distinguish the image even if it is
     * blank because of erroneous execution); returns an empty string if the URL is null
     */
    @Override
    public String toHtml() {
        Object url = getData();
        if (url == null) {
            return "";
        }
        return "<div style=\"max-width: 100%; max-height: 400px; overflow: auto; box-shadow: 0 0 8px lightgray;\">" +
                "<img style=\"max-width: 100%;\" src=\"" + StringEscapeUtils.escapeXml10(url.toString()) + "\">" +
                "</div>";
    }

}
