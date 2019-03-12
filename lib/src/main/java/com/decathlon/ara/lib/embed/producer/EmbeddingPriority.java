package com.decathlon.ara.lib.embed.producer;

/**
 * <p>The priority is used to order embeddings in a sensitive way:</p>
 * <ul>
 *     <li>ERROR_* are presented first because something fatal forbids to exploit the result of the scenario</li>
 *     <li>OUTPUT_* are results produced by the Cucumber scenario and should therefore be presented to the user first
 *     (if no fatal error appeared)</li>
 *     <li>FUNCTIONAL_DEBUG_* are non-technical information to help functional people figure out what happened wrong
 *     during a failing scenario (like screenshots or videos of what the Selenium robot did on the website)</li>
 *     <li>TECHNICAL_DEBUG_* are presented last, for the few technical people to debug the scenario</li>
 *     <li>HIDDEN is special: it is intended for automated tools only and do not generate any HTML report.</li>
 * </ul>
 * <p>Each category (except for the special HIDDEN) have *_SMALL, *_MEDIUM and *_LARGE suffixes:
 * for each category, small items are presented first (summaries, short links...) and bigger one are presented last,
 * so a better big-picture overview can be glanced by users.</p>
 */
public enum EmbeddingPriority {

    /**
     * Something went wrong during the scenario execution: this embedding helps finding what and why.<br>
     * The error category is presented first, because other categories are useless or less important if the scenario
     * encountered an error.<br>
     * A very small information (usually a single line of text): presented first in its category.
     */
    ERROR_SMALL,

    /**
     * Something went wrong during the scenario execution: this embedding helps finding what and why.<br>
     * The error category is presented first, because other categories are useless or less important if the scenario
     * encountered an error.<br>
     * An information that takes a reasonable height to display: presented second in its category.
     */
    ERROR_MEDIUM,

    /**
     * Something went wrong during the scenario execution: this embedding helps finding what and why.<br>
     * The error category is presented first, because other categories are useless or less important if the scenario
     * encountered an error.<br>
     * A large information that takes some height to display: presented last in its category.
     */
    ERROR_LARGE,

    /**
     * The scenario produced some output (a diff result, a graphical report, etc.).<br>
     * The output category is presented after errors.<br>
     * A very small information (usually a single line of text): presented first in its category.
     */
    OUTPUT_SMALL,

    /**
     * The scenario produced some output (a diff result, a graphical report, etc.).<br>
     * The output category is presented after errors.<br>
     * An information that takes a reasonable height to display: presented second in its category.
     */
    OUTPUT_MEDIUM,

    /**
     * The scenario produced some output (a diff result, a graphical report, etc.).<br>
     * The output category is presented after errors.<br>
     * A large information that takes some height to display: presented last in its category.
     */
    OUTPUT_LARGE,

    /**
     * The scenario has some data attached to help debug or trace its execution, and understand what and how the test
     * did: the debug information is not technical at all (like a screenshot or a list of buttons clicked), and can de
     * understood by business people in the team.<br>
     * The functional category is presented after the error and output ones.<br>
     * A very small information (usually a single line of text): presented first in its category.
     */
    FUNCTIONAL_DEBUG_SMALL,

    /**
     * The scenario has some data attached to help debug or trace its execution, and understand what and how the test
     * did: the debug information is not technical at all (like a screenshot or a list of buttons clicked), and can de
     * understood by business people in the team.<br>
     * The functional category is presented after the error and output ones.<br>
     * An information that takes a reasonable height to display: presented second in its category.
     */
    FUNCTIONAL_DEBUG_MEDIUM,

    /**
     * The scenario has some data attached to help debug or trace its execution, and understand what and how the test
     * did: the debug information is not technical at all (like a screenshot or a list of buttons clicked), and can de
     * understood by business people in the team.<br>
     * The functional category is presented after the error and output ones.<br>
     * A large information that takes some height to display: presented last in its category.
     */
    FUNCTIONAL_DEBUG_LARGE,

    /**
     * Technical information used by developers to debug or trace the scenario execution: technical logs, used backend
     * server, some generated UUIDs, IPs, scenario start date and time to correlate with logs, JavaScript errors, HTTP
     * requests, etc.).<br>
     * The technical category is presented after the functional one.<br>
     * A very small information (usually a single line of text): presented first in its category.
     */
    TECHNICAL_DEBUG_SMALL,

    /**
     * Technical information used by developers to debug or trace the scenario execution: technical logs, used backend
     * server, some generated UUIDs, IPs, scenario start date and time to correlate with logs, JavaScript errors, HTTP
     * requests, etc.).<br>
     * The technical category is presented after the functional one.<br>
     * An information that takes a reasonable height to display: presented second in its category.
     */
    TECHNICAL_DEBUG_MEDIUM,

    /**
     * Technical information used by developers to debug or trace the scenario execution: technical logs, used backend
     * server, some generated UUIDs, IPs, scenario start date and time to correlate with logs, JavaScript errors, HTTP
     * requests, etc.).<br>
     * The technical category is presented after the functional one.<br>
     * A large information that takes some height to display: presented last in its category.
     */
    TECHNICAL_DEBUG_LARGE,

    /**
     * The embedding is present in the JSON data for machines, but hidden from the HTML report because it is useless to
     * users.
     */
    HIDDEN

}
