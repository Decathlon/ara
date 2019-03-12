package com.decathlon.ara.lib.embed.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Data;

/**
 * Holds the result of all structured embeddings parsed by {@link StructuredEmbeddingsExtractor#extract(String)}.
 */
@Data
public class StructuredEmbeddingsHolder {

    /**
     * All parsed structured embeddings.
     */
    private final List<ParsedStructuredEmbedding> embeddings = new ArrayList<>();

    /**
     * Try to extract an embedding of a particular kind.
     *
     * @param kind the kind of embedding to be searched for
     * @return the found embedding, if any
     */
    public Optional<String> extractStringData(String kind) {
        return embeddings.stream()
                .filter(e -> kind.equals(e.getKind()))
                .findFirst()
                .map(e -> (String) e.getData());
    }

}
