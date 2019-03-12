package com.decathlon.ara.lib.embed.producer.type;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VideoEmbeddingTest {

    @Test
    public void toHtml_ShouldReturnEmpty_WhenUrlIsNull() {
        // GIVEN
        final String nullUrl = null;
        final VideoEmbedding cut = new VideoEmbedding(null, null, nullUrl, null);

        // WHEN
        final String html = cut.toHtml();

        // THEN
        assertThat(html).isEmpty();
    }

    @Test
    public void toHtml_ShouldEscapeHtmlCharacters_WhenUrlContainsHtmlCharacters() {
        // GIVEN
        final String urlWithHtml = "Escaping<test> &amp; \"checked'";
        final VideoEmbedding cut = new VideoEmbedding(null, null, urlWithHtml, null);

        // WHEN
        final String html = cut.toHtml();

        // THEN
        assertThat(html).isEqualTo("" +
                "<video src=\"Escaping&lt;test&gt; &amp;amp; &quot;checked&apos;\" " +
                "width=\"864\" autobuffer controls style=\"max-width: 100%; box-shadow: 0 0 8px lightgray;\">" +
                "<a href=\"Escaping&lt;test&gt; &amp;amp; &quot;checked&apos;\">SHOW</a>" +
                "</video>");
    }

}
