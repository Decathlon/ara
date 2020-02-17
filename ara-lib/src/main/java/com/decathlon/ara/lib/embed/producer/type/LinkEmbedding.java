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
 * Embed a named and clickable link to a URL.
 */
public class LinkEmbedding extends StructuredEmbedding {

    public LinkEmbedding(String kind, String name, String url, EmbeddingPriority priority) {
        super(kind, name, "link", url, priority);
    }

    /**
     * @return a link "SHOW" pointing to the given URL; returns an empty string if the URL is null
     */
    @Override
    public String toHtml() {
        Object url = getData();
        if (url == null) {
            return "";
        }
        final String escapedUrl = StringEscapeUtils.escapeXml10(url.toString());
        return "<a href=\"" + escapedUrl + "\">SHOW</a>" +
                // If link and viewer are not both HTTPS or HTTP, browser blocks link opening
                " (Open in a new tab if it is not opening)";
    }

}
