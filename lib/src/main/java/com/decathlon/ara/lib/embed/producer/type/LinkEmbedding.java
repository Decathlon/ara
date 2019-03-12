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
