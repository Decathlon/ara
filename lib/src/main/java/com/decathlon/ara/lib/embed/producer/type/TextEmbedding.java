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
