package com.decathlon.ara.lib.embed.producer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Base class for structured embeddings to attach to Cucumber scenarios.<br>
 * Implementations barely only need to implement {@link #toHtml()} and a constructor that passes a hard-coded type to
 * its parent constructor.
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class StructuredEmbedding {

    /**
     * A machine-readable identifier for the content to embed. It is not needed to be unique.<br>
     * Eg. "accessibilityReports" (there could be several embeddings with kind "accessibilityReports").
     */
    private final String kind;

    /**
     * A human-readable name for the content to embed. Eg. "Accessibility Reports".
     */
    private final String name;

    /**
     * The type of data supported by StructuredEmbedding sub-classes.<br>
     * A machine that does not recognize the {@link #kind} can still display it in a generic manner.<br>
     * Each implementation of this class must provide a unique type.
     */
    private final String type;

    /**
     * Data of this embedding: a text or URL most of the times, but can be JSON-serializable objects for other types.
     */
    private final Object data;

    /**
     * The priority to use to sort all embeddings of a given scenario.
     *
     * @see EmbeddingPriority EmbeddingPriority for possible values
     */
    private final EmbeddingPriority priority;

    /**
     * @return the HTML representation of the embedding, for human display (any user data must be HTML escaped)
     */
    public abstract String toHtml();

}
