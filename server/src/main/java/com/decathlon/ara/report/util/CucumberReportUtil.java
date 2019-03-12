package com.decathlon.ara.report.util;

import com.decathlon.ara.report.bean.Element;
import com.decathlon.ara.report.bean.Feature;
import com.decathlon.ara.report.bean.Hook;
import com.decathlon.ara.report.bean.Row;
import com.decathlon.ara.report.bean.Step;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

/**
 * A set of static functions with no dependency nor side-effect (no download, upload, database access...) that parse Cucumber's report.json and manipulate basic data.
 */
@UtilityClass
public class CucumberReportUtil {

    /**
     * @param reportJson the content of a report.json as produced by Cucumber
     * @return the parsed report.json as a list of *.feature files
     * @throws IOException if something goes wrong while parsing the report content
     */
    public static List<Feature> parseReportJson(String reportJson) throws IOException {
        List<Feature> features = new ObjectMapper().readValue(reportJson, new TypeReference<List<Feature>>() {
            // Nothing to override from this abstract class
        });
        if (features == null) {
            throw new IllegalArgumentException("Cannot parse report.json stream");
        }
        return features;
    }

    /**
     * @param scenario          a scenario from a parsed report.json Cucumber report of an execution
     * @param backgroundContent optional background content to append after @Before and before real scenario content
     * @return the scenario content: each line begins with line number + status + step or hook (separated by ':'), eg.<br>
     * <code>261:passed:Given a request<br>
     * 262:passed:When I call the service<br>
     * 263:failed:Then the response has no error<br>
     * 264:skipped:And the response has values:<br>
     * 265:skipped:| "Key 1" | "Value 1" |<br>
     * 266:skipped:| "Key 2" | "Value 2" |</code>
     */
    public static String extractScenarioContent(Element scenario, String backgroundContent) {
        StringBuilder builder = new StringBuilder();
        appendHooks(builder, scenario.getBefore(), "@Before");
        if (StringUtils.isNotEmpty(backgroundContent)) {
            newLine(builder)
                    .append("0:element:Background:\n")
                    .append(backgroundContent)
                    .append("\n0:element:Scenario:");
        }
        appendSteps(builder, scenario.getSteps());
        appendHooks(builder, scenario.getAfter(), "@After");
        return builder.toString();
    }

    static int virtualHookLine(String hookName, int hookIndex) {
        if ("@After".equals(hookName)) {
            return 100000 + hookIndex;
        } else { // Before
            return -100000 + hookIndex;
        }
    }

    private static void appendHooks(StringBuilder builder, Hook[] hooks, String hookName) {
        for (int i = 0; i < hooks.length; i++) {
            Hook hook = hooks[i];
            newLine(builder)
                    .append(virtualHookLine(hookName, i)).append(":")
                    .append(hook.getResult().getStatus().getJsonValue()).append(":")
                    .append(hook.getResult().getDuration()).append(":")
                    .append(hookName).append(' ').append(hook.getMatch().getLocation());
        }
    }

    private static void appendSteps(StringBuilder builder, Step[] steps) {
        for (Step step : steps) {
            newLine(builder)
                    .append(step.getLine()).append(":")
                    .append(step.getResult().getStatus().getJsonValue()).append(":")
                    .append(step.getResult().getDuration()).append(":")
                    .append(step.getKeyword()).append(step.getName());
            appendRows(builder, step);
            appendDocString(builder, step);
        }
    }

    private static void appendDocString(StringBuilder builder, Step step) {
        if (step.getDocString() != null) {
            final Integer lineNumber = step.getLine();
            final String status = step.getResult().getStatus().getJsonValue();
            String docType = StringUtils.defaultString(step.getDocString().getContentType());
            newLine(builder)
                    .append(lineNumber).append(':')
                    .append(status).append(":")
                    .append("\"\"\"").append(docType);
            final String[] lines = step.getDocString().getValue()
                    .replace("\r\n", "\n")
                    .split("\n");
            for (String line : lines) {
                builder
                        .append('\n')
                        .append(lineNumber).append(':')
                        .append(status).append(":")
                        .append(line);
            }
            builder
                    .append('\n')
                    .append(lineNumber).append(':')
                    .append(status).append(":")
                    .append("\"\"\"");
        }
    }

    /**
     * Append the rows of a step in a scenario from a parsed report.json Cucumber report of an execution. eg:
     * <code>265:skipped:| "Key 1" | "Value 1" |<br>
     * 266:skipped:| "Key 2" | "Value 2" |</code>
     *
     * @param builder a String builder in which to append the rows
     * @param step    the step containing (or not) rows (appends nothing to the builder if the step has no row)
     */
    private static void appendRows(StringBuilder builder, Step step) {
        if (step.getRows() != null && step.getRows().length > 0) {
            int[] columnSizes = getColumnSizes(step.getRows());
            for (Row row : step.getRows()) {
                newLine(builder)
                        .append(row.getLine()).append(":")
                        .append(step.getResult().getStatus().getJsonValue()).append(":");
                for (int i = 0; i < row.getCells().length; i++) {
                    String cell = row.getCells()[i];
                    boolean isNumeric = StringUtils.isNotEmpty(cell) && StringUtils.isNumeric(cell); // isNumeric() really means areDigits()
                    cell = (isNumeric ? StringUtils.leftPad(cell, columnSizes[i]) : StringUtils.rightPad(cell, columnSizes[i]));
                    builder.append("| ").append(cell).append(' ');
                }
                builder.append("|");
            }
        }
    }

    /**
     * @param rows a list of rows containing cells
     * @return for each columns, the maximum string length of each cell of that column
     */
    private static int[] getColumnSizes(Row[] rows) {
        int[] sizes = new int[getMaxColumnCount(rows)];
        for (Row row : rows) {
            for (int i = 0; i < row.getCells().length; i++) {
                int cellLength = row.getCells()[i].length();
                if (sizes[i] < cellLength) {
                    sizes[i] = cellLength;
                }
            }
        }
        return sizes;
    }

    /**
     * @param rows a list of rows containing cells
     * @return the maximum number of columns among all rows
     */
    private static int getMaxColumnCount(Row[] rows) {
        int maxColumns = 0;
        for (Row row : rows) {
            if (maxColumns < row.getCells().length) {
                maxColumns = row.getCells().length;
            }
        }
        return maxColumns;
    }

    private static StringBuilder newLine(StringBuilder builder) {
        if (builder.length() > 0) {
            builder.append('\n');
        }
        return builder;
    }

}
