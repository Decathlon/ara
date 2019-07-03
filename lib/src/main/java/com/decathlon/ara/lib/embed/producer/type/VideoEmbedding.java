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
 * Embed a link to a video file (served through an HTTP URL).<br>
 * The URL is presented as a video tag in HTML, allowing users to play the video in place.
 */
public class VideoEmbedding extends StructuredEmbedding {

    public VideoEmbedding(String kind, String name, String url, EmbeddingPriority priority) {
        super(kind, name, "video", url, priority);
    }

    /**
     * @return a video tag (if width is reduced if the video is too wide; aspect ratio is kept) with a box shadow (to
     * distinguish the video even if it is blank because of erroneous execution); returns an empty string if the URL is
     * null
     */
    @Override
    public String toHtml() {
        Object url = getData();
        if (url == null) {
            return "";
        }
        final String escapedUrl = StringEscapeUtils.escapeXml10(url.toString());
        return "" +
                "<video src=\"" + escapedUrl + "\" width=\"864\" autobuffer controls " +
                "style=\"max-width: 100%; box-shadow: 0 0 8px lightgray;\">" +
                "<a href=\"" + escapedUrl + "\">SHOW</a>" +
                "</video>";
    }

}
