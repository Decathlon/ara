package com.decathlon.ara.lib.embed.producer.type;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LinkEmbeddingTest {

    @Test
    public void toHtml_ShouldReturnEmpty_WhenUrlIsNull() {
        // GIVEN
        final String nullUrl = null;
        final LinkEmbedding cut = new LinkEmbedding(null, null, nullUrl, null);

        // WHEN
        final String html = cut.toHtml();

        // THEN
        assertThat(html).isEmpty();
    }

    @Test
    public void toHtml_ShouldEscapeHtmlCharacters_WhenUrlContainsHtmlCharacters() {
        // GIVEN
        final String urlWithHtml = "Escaping<test> &amp; \"checked'";
        final LinkEmbedding cut = new LinkEmbedding(null, null, urlWithHtml, null);

        // WHEN
        final String html = cut.toHtml();

        // THEN
        assertThat(html).isEqualTo("" +
                "<a href=\"Escaping&lt;test&gt; &amp;amp; &quot;checked&apos;\">SHOW</a> " +
                "(Open in a new tab if it is not opening)");
    }

}
