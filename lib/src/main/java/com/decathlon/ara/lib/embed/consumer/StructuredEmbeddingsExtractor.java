package com.decathlon.ara.lib.embed.consumer;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

/**
 * Read an HTML String, parse it and extract all structured embeddings it contains.
 */
@Slf4j
public class StructuredEmbeddingsExtractor {

    // We only recognize version 1, and its non-breaking sub-versions (using https://semver.org/ to ensure that)
    private static final Pattern MAGIC_HEADER_PATTERN = Pattern.compile("^(<!--StructuredEmbeddings_v1(.[0-9]+)?.([0-9]+)?=).*$", Pattern.DOTALL);
    private static final String JSON_STOP = "-->";

    /**
     * Read an HTML String, parse it and extract all structured embeddings it contains.
     *
     * @param html the HTML String to parse
     * @return the read embeddings, or empty if the HTML is not of the supported format
     */
    public Optional<StructuredEmbeddingsHolder> extract(String html) {
        int jsonStartIndex = magicHeaderLength(html);
        if (jsonStartIndex <= 0) {
            return Optional.empty();
        }

        int jsonStopIndex = html.indexOf(JSON_STOP, jsonStartIndex);
        if (jsonStopIndex <= 0) {
            return Optional.empty();
        }

        String json = html.substring(jsonStartIndex, jsonStopIndex).replace("&#x2d;", "-").replace("&amp;", "&");
        String htmlParts = html.substring(jsonStopIndex + JSON_STOP.length());

        JSONArray array;
        try {
            array = (JSONArray) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(json);
        } catch (ParseException e) {
            log.error("Cannot parse StructuredEmbeddings JSON", e);
            return Optional.empty();
        }

        StructuredEmbeddingsHolder embeddings = new StructuredEmbeddingsHolder();
        for (int i = 0; i < array.size(); i++) {
            JSONObject object = (JSONObject) array.get(i);
            final Integer priorityOrder = (Integer) object.get("priorityOrder");
            final ParsedStructuredEmbedding embedding = new ParsedStructuredEmbedding(
                    (String) object.get("kind"),
                    (String) object.get("name"),
                    (String) object.get("type"),
                    object.get("data"),
                    (String) object.get("priority"),
                    (priorityOrder == null ? 0 : priorityOrder.intValue()),
                    extractHtml(htmlParts, i));
            embeddings.getEmbeddings().add(embedding);
        }
        return Optional.of(embeddings);
    }

    /**
     * @param html an HTML that can begin with a special comment containing machine-readable JSON
     * @return the length of the recognized magic header, or <= 0 if not found/recognized
     */
    private int magicHeaderLength(String html) {
        final Matcher matcher = MAGIC_HEADER_PATTERN.matcher(html);
        if (matcher.matches()) {
            return matcher.group(1).length();
        }
        return -1;
    }

    /**
     * Extract the HTML representation of an embedding, by its index.
     *
     * @param htmlParts the whole HTML content
     * @param index the index of the embedding to search for its HTML representation
     * @return the HTML representation, or null if not found (wrong index, or hidden content)
     */
    private String extractHtml(String htmlParts, int index) {
        final String startToken = "<!--START:index=" + index + "-->";
        final String stopToken = "<!--STOP:index=" + index + "-->";

        int startIndex = htmlParts.indexOf(startToken);
        if (startIndex == -1) {
            return null;
        }
        int stopIndex = htmlParts.indexOf(stopToken, startIndex + startToken.length());
        if (stopIndex == -1) {
            return null;
        }

        return htmlParts.substring(startIndex + startToken.length(), stopIndex);
    }

}
