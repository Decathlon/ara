package com.decathlon.ara.lib.embed.producer.type;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TextEmbeddingTest {

    @Test
    public void toHtml_ShouldReturnEmpty_WhenDataIsNull() {
        // GIVEN
        final String nullText = null;
        final TextEmbedding cut = new TextEmbedding(null, null, nullText, null);

        // WHEN
        final String html = cut.toHtml();

        // THEN
        assertThat(html).isEmpty();
    }

    @Test
    public void toHtml_ShouldEscapeHtmlCharacters_WhenDataContainsHtmlCharacters() {
        // GIVEN
        final String textWithHtml = "Escaping<test> &amp; \"checked'";
        final TextEmbedding cut = new TextEmbedding(null, null, textWithHtml, null);

        // WHEN
        final String html = cut.toHtml();

        // THEN
        assertThat(html).isEqualTo("Escaping&lt;test&gt; &amp;amp; &quot;checked&apos;");
    }

}
