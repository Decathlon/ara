package com.decathlon.ara.lib.embed.producer.type;

import lombok.Data;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectEmbeddingTest {

    @Test
    public void toHtml_ShouldReturnEmpty_WhenDataIsNull() {
        // GIVEN
        final Object nullData = null;
        final ObjectEmbedding cut = new ObjectEmbedding(null, null, nullData, null);

        // WHEN
        final String html = cut.toHtml();

        // THEN
        assertThat(html).isEmpty();
    }

    @Test
    public void toHtml_ShouldEncodeDataAsJsonAndEscapeHtmlCharacters_WhenDataIsPresent() {
        // GIVEN
        final SomeClass data = new SomeClass();
        data.child = new SomeClass();
        data.child.text = "sub-text";
        data.text = "Escaping<test> &amp; \"checked'";
        data.number = 42;
        final ObjectEmbedding cut = new ObjectEmbedding(null, null, data, null);

        // WHEN
        final String html = cut.toHtml();

        // THEN
        assertThat(html).isEqualTo("" +
                "{&quot;number&quot;:42," +
                "&quot;text&quot;:&quot;Escaping&lt;test&gt; &amp;amp; \\&quot;checked&apos;&quot;," +
                "&quot;child&quot;:{&quot;number&quot;:0,&quot;text&quot;:&quot;sub-text&quot;,&quot;child&quot;:null}}");
    }

    @Data
    public static class SomeClass {

        SomeClass child;
        String text;
        int number;

    }

}
