package com.decathlon.ara.report.bean;

import com.decathlon.ara.lib.embed.consumer.StructuredEmbeddingsHolder;
import com.decathlon.ara.report.support.ResultsWithMatch;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.Data;

@Data
public class Step implements ResultsWithMatch {

    private String name;
    private String keyword;
    private Integer line;
    private Result result;
    private Row[] rows = new Row[0];
    private Match match;
    private Embedded[] embeddings = new Embedded[0];
    private String[] output = new String[0];
    private Comment[] comments = new Comment[0];
    private int[] matchedColumns = new int[0];

    @JsonProperty("doc_string")
    private DocString docString;

    /**
     * @return the extracted screenshot, if the step contains an embedding that is a video URL
     */
    public Optional<byte[]> extractScreenshot() {
        if (embeddings != null) {
            for (Embedded embedding : embeddings) {
                final Optional<byte[]> screenshot = embedding.extractScreenshot();
                if (screenshot.isPresent()) {
                    return screenshot;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * @return the extracted video link ("http...mp4"), if the step contains an embedding that is a video URL
     */
    public Optional<String> extractVideoUrl() {
        if (embeddings != null) {
            // Video link is the last embedding: don't loose time decoding other text embeddings
            for (int i = embeddings.length - 1; i >= 0; i--) {
                final Embedded embedding = embeddings[i];
                Optional<String> videoUrl = embedding.extractVideoUrl();
                if (videoUrl.isPresent()) {
                    return videoUrl;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * @return the extracted structured-embedding, if the step contains such embedding
     */
    public Optional<StructuredEmbeddingsHolder> extractStructuredEmbeddings() {
        if (embeddings != null) {
            // Structured embeddings are the last embedding: don't loose time decoding other text embeddings
            for (int i = embeddings.length - 1; i >= 0; i--) {
                final Embedded embedding = embeddings[i];
                Optional<StructuredEmbeddingsHolder> structuredEmbeddings = embedding.extractStructuredEmbeddings();
                if (structuredEmbeddings.isPresent()) {
                    return structuredEmbeddings;
                }
            }
        }
        return Optional.empty();
    }

}
