package com.decathlon.ara.lib.embed.producer;

import com.decathlon.ara.lib.embed.producer.type.TextEmbedding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.text.StringEscapeUtils;

/**
 * <p>Produce a structured embeddings HTML representation to be embedded into a Cucumber scenario.</p>
 *
 * <p>Structured embeddings provide both a human-readable and machine-readable embedding for Cucumber scenarios.</p>
 *
 * <p>Embeddings have a "name" for human to sort out the list of embeddings, and the result is compiled to an HTML
 * output, and copied to a JSON hidden object for machines to parse embedded data.</p>
 *
 * <p>Embeddings have an "kind" and some other meta-data for automated-tools to be able to parse the attachments and
 * extract useful information.<br>
 * Tools are able to display raw HTML to humans for other non-supported embedding types.</p>
 *
 * <p>The goal of these structured embeddings is to keep the section small to not overload the reports, and not make
 * Cucumber run out of memory when storing all embeddings of an execution.<br>
 * So any big content (images, videos, long log files...) must be uploaded somewhere and only the URL will be
 * embedded.<br>
 * This will let automated tools parse the report.json without having to download a heavy JSON or to run out of memory
 * trying to load a report full of a lot of big embedding files.</p>
 */
public class StructuredEmbeddingsBuilder {

    /**
     * Date &amp; time pattern, down to the millisecond, humanly readable but machinely parseable too
     * (used for eg. scenario starting date and time).
     */
    public static final String HUMAN_AND_MACHINE_READABLE_TIMESTAMP_PATTERN = "yyyy.MM.dd-HH'h'mm'm'ss.SSS";

    private List<StructuredEmbedding> embeddings = new ArrayList<>();

    /**
     * Add an embedding to the embeddings that will later be attached to a Cucumber scenario by using {@link #build()}.
     *
     * @param embedding the data to embed
     */
    public void add(StructuredEmbedding embedding) {
        embeddings.add(embedding);
    }

    /**
     * Add a text embedding containing the scenario start date and time in a format that is both human- and machine-
     * readable, with the kind that ARA will search for when indexing execution reports.
     *
     * @see #HUMAN_AND_MACHINE_READABLE_TIMESTAMP_PATTERN the pattern to parse it yourself
     */
    public void addStartDateTime() {
        final SimpleDateFormat formatter = new SimpleDateFormat(HUMAN_AND_MACHINE_READABLE_TIMESTAMP_PATTERN);
        String startDateTime = formatter.format(Calendar.getInstance().getTime());
        add(new TextEmbedding(
                "startDateTime",
                "Scenario start date & time",
                startDateTime,
                EmbeddingPriority.TECHNICAL_DEBUG_SMALL));
    }

    /**
     * @return true if the builder contains no embedding, false if at least one embedding is present
     */
    public boolean isEmpty() {
        return embeddings.isEmpty();
    }

    /**
     * @return a string containing an HTML representation of all the added embeddings, as well as the same data in the
     * form of a JSON hidden in a comment at the start of the HTML
     * @see #add(StructuredEmbedding) to add embeddings before building
     */
    public String build() {
        embeddings.sort(Comparator.comparingInt(o -> o.getPriority().ordinal()));
        // Magic code to recognize the embedding as a structured one + versioned using https://semver.org/
        return "<!--StructuredEmbeddings_v1.0.0=" + toJsonArray().toJSONString()
                .replace("&", "&amp;")
                .replace("-", "&#x2d;") + "-->" +
                toHtml();
    }

    /**
     * @return the JSON representation of the embeddings, to put in an HTML comment
     */
    private JSONArray toJsonArray() {
        final JSONArray array = new JSONArray();
        for (StructuredEmbedding embedding : embeddings) {
            JSONObject object = new JSONObject();
            object.put("kind", embedding.getKind());
            object.put("name", embedding.getName());
            object.put("type", embedding.getType());
            object.put("data", embedding.getData());
            object.put("priority", embedding.getPriority());
            object.put("priorityOrder", Integer.valueOf(embedding.getPriority().ordinal()));
            array.add(object);
        }
        return array;
    }

    /**
     * @return the user-visible HTML representation of the embeddings
     */
    private String toHtml() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < embeddings.size(); i++) {
            StructuredEmbedding embedding = embeddings.get(i);
            if (embedding.getPriority() != EmbeddingPriority.HIDDEN) {
                // No new-line characters, because it will be embedded in a <pre/> in default Cucumber reports
                builder
                        .append("<h3 style=\"margin: 16px 0 4px 0;\">")
                        .append(StringEscapeUtils.escapeXml10(embedding.getName()))
                        .append("</h3><div>")
                        .append("<!--START:index=").append(i).append("-->")
                        .append(embedding.toHtml())
                        .append("<!--STOP:index=").append(i).append("--></div>");
            }
        }
        return builder.toString();
    }

}
