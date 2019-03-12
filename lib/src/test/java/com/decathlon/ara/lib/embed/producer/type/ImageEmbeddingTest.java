package com.decathlon.ara.lib.embed.producer.type;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageEmbeddingTest {

    @Test
    public void toHtml_ShouldReturnEmpty_WhenUrlIsNull() {
        // GIVEN
        final String nullUrl = null;
        final ImageEmbedding cut = new ImageEmbedding(null, null, nullUrl, null);

        // WHEN
        final String html = cut.toHtml();

        // THEN
        assertThat(html).isEmpty();
    }

    @Test
    public void toHtml_ShouldEscapeHtmlCharacters_WhenUrlContainsHtmlCharacters() {
        // GIVEN
        final String urlWithHtml = "Escaping<test> &amp; \"checked'";
        final ImageEmbedding cut = new ImageEmbedding(null, null, urlWithHtml, null);

        // WHEN
        final String html = cut.toHtml();

        // THEN
        assertThat(html).isEqualTo("" +
                "<div style=\"max-width: 100%; max-height: 400px; overflow: auto; box-shadow: 0 0 8px lightgray;\">" +
                "<img style=\"max-width: 100%;\" src=\"Escaping&lt;test&gt; &amp;amp; &quot;checked&apos;\">" +
                "</div>");
    }

}
