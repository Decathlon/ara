package com.decathlon.ara.report.bean;

import com.decathlon.ara.lib.embed.consumer.StructuredEmbeddingsHolder;
import com.decathlon.ara.report.support.ResultsWithMatch;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;

@Data
public class Element {

    private static final String[] SCENARIO_KEYWORDS = { "Scenario", "Scenario Outline" };
    private static final Pattern SCENARIO_OUTLINE_PATTERN = Pattern.compile("^.*;;([0-9]+)$");
    private static final String BACKGROUND_KEYWORD = "Background";

    private String id;
    private String name;
    private String type;
    private String description;
    private String keyword;
    private Step[] steps = new Step[0];
    private Hook[] before = new Hook[0];
    private Hook[] after = new Hook[0];
    private Tag[] tags = new Tag[0];
    private Integer line;
    private Comment[] comments = new Comment[0];

    /**
     * @param stepsOrHooks steps and/or before/after hooks of a scenario
     * @return true if all of them have exactly the status PASSED
     */
    private static boolean isPassed(ResultsWithMatch[] stepsOrHooks) {
        if (stepsOrHooks != null) {
            for (final ResultsWithMatch stepOrHook : stepsOrHooks) {
                if (stepOrHook.getResult().getStatus() != Status.PASSED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @return true if the current element is a scenario or scenario outline
     */
    public boolean isScenario() {
        for (String reference : SCENARIO_KEYWORDS) {
            if (reference.equals(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if the current element is a background
     */
    public boolean isBackground() {
        return BACKGROUND_KEYWORD.equals(keyword);
    }

    /**
     * With cucumber JSON reports, if a @Before hook fails, the scenario's status is PASSED! So we check that all @Before and @After hooks are
     * "passed" and that all steps are also "passed".
     *
     * @return true if the scenario has all steps and all @Before/@After "passed"
     */
    public boolean isPassed() {
        return isPassed(getBefore()) &&
                isPassed(getSteps()) &&
                isPassed(getAfter());
    }

    /**
     * @return the extracted screenshot found in one of the steps of the scenario (usually the last one), if any
     */
    public Optional<byte[]> extractScreenshot() {
        if (steps != null) {
            for (final Step step : steps) {
                Optional<byte[]> screenshot = step.extractScreenshot();
                if (screenshot.isPresent()) {
                    return screenshot;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * @return the extracted video link ("http...mp4") found in one of the steps of the scenario (usually the last one), if any
     */
    public Optional<String> extractVideoUrl() {
        if (steps != null) {
            for (final Step step : steps) {
                Optional<String> videoUrl = step.extractVideoUrl();
                if (videoUrl.isPresent()) {
                    return videoUrl;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * @return the extracted structured-embedding, if one of the steps of the scenario contains such embedding
     */
    public Optional<StructuredEmbeddingsHolder> extractStructuredEmbeddings() {
        if (steps != null) {
            for (final Step step : steps) {
                Optional<StructuredEmbeddingsHolder> structuredEmbeddings = step.extractStructuredEmbeddings();
                if (structuredEmbeddings.isPresent()) {
                    return structuredEmbeddings;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * @return true if the current element is a scenario or the first instance of a group of instances of one single scenario outline
     */
    public boolean isSingleScenarioOrFirstOfOutline() {
        // "Scenario Outline"s are expanded to a list of scenarios without reference to the outline
        // Thankfully, their id ends with ";;2", ";;3", etc.
        // The id starts with 2, so we can detect if the occurrence is the first one (or if it is not from an outline)
        final Matcher matcher = SCENARIO_OUTLINE_PATTERN.matcher(id);
        return !matcher.matches() || Integer.parseInt(matcher.group(1)) <= 2;
    }

}
