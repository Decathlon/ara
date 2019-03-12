package com.decathlon.ara.report.util;

import com.decathlon.ara.report.support.ResultsWithMatch;
import com.decathlon.ara.report.bean.Argument;
import com.decathlon.ara.report.bean.Step;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * A set of static functions with no dependency nor side-effect (no download, upload, database access...) that parse a stepDefinitions.json or manipulate the parsed result.
 */
@Slf4j
@UtilityClass
public class StepDefinitionUtil {

    /**
     * @param stepDefinitionsJson the JSON containing the step definitions to parse and return
     * @return the list of step regular expressions; can be empty if cannot download or parse the stepDefinitions.json file, but never null
     */
    public static List<String> parseStepDefinitionsJson(String stepDefinitionsJson) {
        try {
            return new ObjectMapper().readValue(stepDefinitionsJson, new TypeReference<List<String>>() {
                // Nothing to override from this abstract class
            });
        } catch (IOException e) {
            log.error("Cannot load step definitions", e);
            return new ArrayList<>();
        }
    }

    /**
     * @param stepOrHook      the step or hook for which to compute the step definition
     * @param hookName        optional hook name to use as step definition if stepOrHook is a hook and therefore has no step definition
     * @param stepDefinitions the parsed list of step definitions extracted from Cucumber
     * @return the step definition of the step or the hook class+method of the hook
     */
    public static String extractStepDefinition(ResultsWithMatch stepOrHook, String hookName, List<String> stepDefinitions) {
        String stepDefinition;
        if (StringUtils.isEmpty(hookName)) {
            stepDefinition = getMatchingStepDefinition(stepDefinitions, ((Step) stepOrHook).getName(), stepOrHook.getMatch().getArguments());
        } else {
            stepDefinition = stepOrHook.getMatch().getLocation(); // eg. "Hooks.beforeScenario(Scenario)"
        }
        return stepDefinition;
    }

    /**
     * @param stepDefinitions the list of step definitions extracted from Cucumber
     * @param stepName        eg. "User goes to the product details page of product \"NrtP01\" model \"NrtP01M3\""
     * @param arguments       eg. [ { "val": "NrtP01", "offset": 50 }, { "val": "NrtP01M3", "offset": 65 } ]
     * @return the match in stepDefinitions, or a simulated one (eg. "User goes to the product details page of product \"*\" model \"*\"")
     */
    private static String getMatchingStepDefinition(List<String> stepDefinitions, String stepName, Argument[] arguments) {
        List<String> matchingStepDefinitions = new ArrayList<>();
        for (String stepDefinition : stepDefinitions) {
            if (Pattern.compile(stepDefinition).matcher(stepName).matches()) {
                matchingStepDefinitions.add(stepDefinition);
            }
        }

        if (matchingStepDefinitions.isEmpty()) {
            log.error("Cannot find any matching step definition for \"{}\"", stepName);
            return simulateMatchingStepDefinition(stepName, arguments);
        }

        if (matchingStepDefinitions.size() > 1) {
            log.error("Found multiple matching step definition for \"{}\": taking the first one in {}", stepName, matchingStepDefinitions);
        }

        return matchingStepDefinitions.get(0);
    }

    public static String simulateMatchingStepDefinition(String stepName, Argument[] arguments) {
        StringBuilder builder = new StringBuilder();
        builder.append("^");

        int lastIndex = 0;
        for (Argument argument : arguments) {
            if (argument.getOffset() > lastIndex) {
                builder.append(escapeRegularExpression(stepName.substring(lastIndex, argument.getOffset())));
            }

            boolean isNumeric = StringUtils.isNotEmpty(argument.getVal()) &&
                    StringUtils.isNumeric(argument.getVal()); // isNumeric() really means areDigits()
            if (isNumeric && argument.getOffset() > 0 && stepName.charAt(argument.getOffset() - 1) == '"') {
                // If an argument is numeric but preceded by a quote, it was probably meant to be a generic string
                isNumeric = false;
            }

            if (isNumeric) {
                builder.append("(\\d+)");
            } else {
                builder.append("([^\"]*)");
            }
            lastIndex = argument.getOffset() + argument.getVal().length();
        }

        if (lastIndex < stepName.length()) {
            builder.append(escapeRegularExpression(stepName.substring(lastIndex)));
        }

        builder.append("$");
        return builder.toString();
    }

    private static String escapeRegularExpression(String text) {
        // See http://docs.oracle.com/javase/tutorial/essential/regex/literals.html
        String characters = "" +
                "\\<" +
                "\\(" +
                "\\[" +
                "\\{" +
                "\\\\" +
                "\\^" +
                "\\-" +
                "\\=" +
                "\\$" +
                "\\!" +
                "\\|" +
                "\\]" +
                "\\}" +
                "\\)" +
                "\\?" +
                "\\*" +
                "\\+" +
                "\\." +
                "\\>";
        return text.replaceAll("([" + characters + "])", "\\\\$1");
    }

}
