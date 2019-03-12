package com.decathlon.ara.postman.service;

import com.decathlon.ara.postman.bean.Error;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.domain.Source;
import com.decathlon.ara.postman.bean.Assertion;
import com.decathlon.ara.postman.bean.Body;
import com.decathlon.ara.postman.bean.Collection;
import com.decathlon.ara.postman.bean.Execution;
import com.decathlon.ara.postman.bean.Failure;
import com.decathlon.ara.postman.bean.Item;
import com.decathlon.ara.postman.bean.KeyValue;
import com.decathlon.ara.postman.bean.Request;
import com.decathlon.ara.postman.bean.Response;
import com.decathlon.ara.postman.bean.Stream;
import com.decathlon.ara.postman.bean.Url;
import com.decathlon.ara.postman.model.NewmanParsingResult;
import com.decathlon.ara.postman.model.NewmanScenario;
import com.decathlon.ara.report.asset.AssetService;
import com.decathlon.ara.report.bean.Status;
import com.decathlon.ara.report.bean.Tag;
import com.decathlon.ara.report.util.ScenarioExtractorUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PostmanService {

    /**
     * A Postman request is in a folder, sub-folder... To delimit each path segment (folder or request), they are separated with this delimiter.<br>
     * Unicode Character 'BLACK RIGHT-POINTING TRIANGLE' (U+25B6)<br>
     * https://www.fileformat.info/info/unicode/char/25b6/index.htm
     */
    public static final String FOLDER_DELIMITER = " \u25b6 ";

    private static final int LINE_PRE_REQUEST_SCRIPT = -100000;
    private static final int LINE_TEST_SCRIPT = 100000;
    private static final int REQUEST_LINE = -1; // First assertion is line 0, so request must come just before
    private static final String STEP_PRE_REQUEST_SCRIPT = "<Pre-Request Script>";
    private static final String STEP_TEST_SCRIPT = "<Test Script>";
    private static final Pattern SEVERITY_PATTERN = Pattern.compile("^(" + Tag.SEVERITY_PREFIX + "[a-z]+(-[a-z]+)*).*");
    private static final String ERROR_PARAGRAPH = "<p style=\"background-color: red; color: white;\">";
    private static final String PARAGRAPH_AND_LINE_END = "</p>\n";
    private static final String CONTENT_DIV_BEGIN_AND_LINE_END = "<div class=\"content\">\n";
    private static final String DIV_END_AND_LINE_END = "</div>\n";

    @NonNull
    private final ObjectMapper objectMapper;

    @NonNull
    private final AssetService assetService;

    /**
     * <p>Newman reports are very huge.<br>
     * Streaming it in order to avoid OutOfMemoryException.</p>
     * <p>The main parts that we retrieve (here, $ is the root of the JSON document, using JsonPath convention) are:</p>
     * <ul>
     * <li>
     * $.collection: a tree of items: medium size, as it contains potentially large pre-request and test JavaScript entities.<br>
     * Mapped to an object in one go.<br>
     * We will deduce a list of ExecutedScenario from them.<br>
     * And we will upload the scripts as text-files, to reduce in-database size.
     * </li>
     * <li>
     * $.run.executions: an array with all HTTP requests made<br>
     * Each request can be huge, as it contains HTTP response bodies (as an array of integers representing the binary stream)<br>
     * We map one execution object at a time, to reduce OutOfMemory chances<br>
     * We extract the response body and upload it: we keep only an URL to the uploaded place
     * </li>
     * <li>
     * $.run.failures: contains about the same as item in collection, with useless objects like parent<br>
     * Unmarshalled all at once.
     * </li>
     * <li>
     * Other nodes are ignored (not unmarshalled at all):
     * <ul>
     * <li>
     * $.environment: all environment variables after execution<br>
     * Ignored because we do not care, and it can be polluted by a lot of big transient variables
     * </li>
     * <li>
     * $.globals: all global variables after execution<br>
     * Ignored because we do not care, and it can be pulluted by a lot of big transient variables
     * </li>
     * <li>
     * $.run.&lt;others&gt;: very small objects, but we do not care<br>
     * Ignored
     * </li>
     * </ul>
     * </li>
     * </ul>
     * <p>Some size information:</p>
     * <ul>
     * <li>[PARSED ALL AT ONCE] collection: 2.15 MB ===&gt; in memory: 4 MB (3.8 MB used by scripts' exec Strings!
     * Scripts are not parsed anymore when indexing executions, but parsed when indexing requests from VCS)</li>
     * <li>[IGNORED] environment: 55 kB</li>
     * <li>[IGNORED] globals: 1 kB</li>
     * <li>[IGNORED] run.stats + run.timings: 1 kB</li>
     * <li>[PARSED ONE BY ONE] executions: 8,14 MB ===&gt; in memory: 152 kB (not impressive, but users could
     * generate lot of extra data</li>
     * <li>[PARSED ALL AT ONCE, AS WE DO NOT STORE BIG OBJECTS] failures: 1 MB ===&gt; in memory: 8 kB</li>
     * </ul>
     * <p>To measure RAM usage, add depdendency com.carrotsearch:java-sizeof:0.0.5
     * and use RamUsageEstimator.sizeOf(object)</p>
     *
     * @param parser the parser, pointing to an open stream, ready to parse the Newman-generated report.json
     * @param result the object where to return parsing result: at completion or if an exception occurs, it can contains temporary files that needs to be removed (after using them or not)
     * @throws IOException on streaming error or the stream was not well-formed and JSON parsing failed
     */
    public void parse(JsonParser parser, NewmanParsingResult result) throws IOException {
        if (!parser.isClosed() && parser.nextToken() == JsonToken.START_OBJECT) {
            log.debug("[json] JSON stream contains a root object: parsing the Newman report");
            parseRootObject(parser, result);
        } else {
            throw new IOException("JSON stream does not contain a root object: no Newman report to parse");
        }
    }

    /**
     * Parse the root object of a Newman JSON report: read the "collection" and "run" properties, ignoring everything else.
     *
     * @param parser the parser, pointing to the START_OBJECT of the root object of an open stream
     * @param result the object where to put the read collection, run's executions and run's failures
     * @throws IOException if something goes wrong (streaming or parsing error)
     */
    private void parseRootObject(JsonParser parser, NewmanParsingResult result) throws IOException {
        while (!parser.isClosed()) {
            JsonToken jsonToken = parser.nextToken();
            if (jsonToken == JsonToken.END_OBJECT) {
                break;
            }

            boolean startingObject = (jsonToken == JsonToken.START_OBJECT);
            final String fieldName = parser.getCurrentName();

            if (startingObject && "collection".equals(fieldName)) {
                log.debug("[json:$] found collection: parsing it");
                result.setCollection(objectMapper.readValue(parser, Collection.class));

            } else if (startingObject && "run".equals(fieldName)) {
                log.debug("[json:$] found run: parsing it");
                parseRun(parser, result);

            } else if (jsonToken != JsonToken.FIELD_NAME) {
                log.debug("[json:$] unmapped {} (last field name: {})", jsonToken, fieldName);
                parser.skipChildren();
            }
        }
    }

    /**
     * Parse the content of a "run" property of the root object of a Newman JSON report: read "executions" and "failures" properties, ignoring everything else.<br>
     * Execution response body streams are saved to file, to free up some RAM.
     *
     * @param parser the parser, pointing to the START_OBJECT of the "run" object of an open stream
     * @param result the object where to put the read executions and failures
     * @throws IOException if something goes wrong (streaming or parsing error)
     */
    private void parseRun(JsonParser parser, NewmanParsingResult result) throws IOException {
        while (!parser.isClosed()) {
            JsonToken jsonToken = parser.nextToken();
            if (jsonToken == JsonToken.END_OBJECT) {
                break;
            }

            boolean startingArray = (jsonToken == JsonToken.START_ARRAY);
            final String fieldName = parser.getCurrentName();

            if (startingArray && "executions".equals(fieldName)) {
                log.debug("[json:$.run] found executions: parsing it");
                List<Execution> executions = new ArrayList<>();
                result.setExecutions(executions);
                parseExecutions(parser, executions);

            } else if (startingArray && "failures".equals(fieldName)) {
                log.debug("[json:$.run] found failures: parsing it");
                result.setFailures(Arrays.asList(objectMapper.readValue(parser, Failure[].class)));

            } else if (jsonToken != JsonToken.FIELD_NAME) {
                log.debug("[json:$.run] unmapped {} (last field name: {})", jsonToken, fieldName);
                parser.skipChildren();
            }
        }
    }

    /**
     * Parse the "executions" array, one value at a time, saving the response body streams to files on the go (to free up some RAM).
     *
     * @param parser     the parser, pointing to the START_ARRAY of the "executions" array of an open stream
     * @param executions the list where to put the read executions
     * @throws IOException if something goes wrong (streaming or parsing error)
     */
    private void parseExecutions(JsonParser parser, List<Execution> executions) throws IOException {
        while (!parser.isClosed()) {
            JsonToken jsonToken = parser.nextToken();
            if (jsonToken == JsonToken.END_ARRAY) {
                break;
            }

            if (jsonToken == JsonToken.START_OBJECT) {
                log.debug("[json:$.run.executions] found execution: parsing it");
                Execution execution = objectMapper.readValue(parser, Execution.class);
                executions.add(execution); // Add BEFORE saving to file: if write fails (no space left), the "half-"file can be deleted
                saveExecutionStreamToFile(execution);
            }
        }
    }

    /**
     * Save the HTTP response body stream to file, if any, and set it to null (to free up some RAM).<br>
     * While setting stream data to null, the stream tempFile is set.
     *
     * @param execution the execution (may not contain any response not any stream data)
     * @throws IOException if something goes wrong (streaming or parsing error)
     */
    void saveExecutionStreamToFile(Execution execution) throws IOException {
        if (execution.getResponse() == null ||
                execution.getResponse().getStream() == null ||
                execution.getResponse().getStream().getData() == null) {
            return;
        }

        final Stream stream = execution.getResponse().getStream();

        File file = File.createTempFile("ara_execution_response_stream_", ".bin");
        file.deleteOnExit();
        FileUtils.writeByteArrayToFile(file, stream.getData());
        stream.setTempFile(file);
        stream.setData(null);
    }

    /**
     * Walk through the Postman tree of folders and requests in the collection and create a flat list of NewmanScenarios, with created ExecutedScenarios for each one.
     *
     * @param items           the Postman collection tree
     * @param requestPosition incremented for each executed scenario, for them to have a different line number across the entire run
     * @param parentSeverity  the severity of the parent (if any, empty if none) folder: deepest folders&requests have
     *                        higher priority and will replace this parent severity if any
     * @param parentFolders   the parent folders of the current items to process: empty at the root of the collection (the method is called recursively for each folder)
     * @return the list of Newman scenarios to process (an intermediate form to aggregate executions and failures in them, and to produce ExecutedScenarios at the end)
     */
    List<NewmanScenario> toScenarios(Item[] items, AtomicInteger requestPosition, String parentSeverity, String... parentFolders) {
        List<NewmanScenario> newmanScenarios = new ArrayList<>();
        if (items != null) {
            for (Item item : items) {
                // Compute item severity (deepest items' severity override severity of previous ones)
                String severity = getSeverity(ArrayUtils.addAll(parentFolders, item.getName()));
                if (StringUtils.isEmpty(severity)) {
                    severity = parentSeverity;
                }

                // Compute item path (without any tag)
                String nameWithoutTag = removeSeverityTag(item.getName());
                String[] path = ArrayUtils.addAll(parentFolders, nameWithoutTag);

                ExecutedScenario executedScenario = new ExecutedScenario(); // UNIQUE: "runId", "featureFile", "name", "line"
                executedScenario.setName(String.join(FOLDER_DELIMITER, path));
                executedScenario.setLine(requestPosition.incrementAndGet());
                executedScenario.setCucumberId(toCucumberId(path));
                executedScenario.setSeverity(severity);
                executedScenario.setTags(StringUtils.isEmpty(severity) ? null : Tag.SEVERITY_PREFIX + severity);

                NewmanScenario newmanScenario = new NewmanScenario();
                newmanScenario.setScenario(executedScenario);
                newmanScenario.setItem(item);
                newmanScenarios.add(newmanScenario);

                newmanScenarios.addAll(toScenarios(item.getChildren(), requestPosition, severity, path));
            }
        }
        return newmanScenarios;
    }

    /**
     * Return a unique identifier for a scenario/request, allowing to group together several executions of the same requests.<br>
     * This allows to get the history of a scenario in ARA: when did it fail or succeed on the time-line?
     *
     * @param path a list of folder names (if any), ended with the request name (if any); eg. [ "folder", "@severity-high request" ]
     * @return eg. "folder/request" for the path given as example
     */
    String toCucumberId(String[] path) {
        // Remove functionalities prefixes for each folder, sub-folder and request
        String[] withoutFunctionalities = new String[path.length];
        for (int i = 0; i < withoutFunctionalities.length; i++) {
            withoutFunctionalities[i] = ScenarioExtractorUtil.removeFunctionalitiesFromScenarioName(path[i]);
        }

        // Join as one ID
        String id = String.join("/", withoutFunctionalities);

        // If the result is too long, take only the last characters, as they are more likely to be unique
        // (most requests would start with the same folder hierarchy)
        if (id.length() > ExecutedScenario.CUCUMBER_ID_MAX_SIZE) {
            id = id.substring(id.length() - ExecutedScenario.CUCUMBER_ID_MAX_SIZE);
        }
        return id;
    }

    /**
     * A folder or request name can begin with a severity tag.<br>
     * If any, remove it from the name and return the modified name.
     *
     * @param name eg. "@severity-high Title", "@severity-high : Title", "@severity-high - Title" or just "Title"
     * @return "Title" for the four given name examples; or "Untitled" if the name would be empty after tag removal
     */
    String removeSeverityTag(String name) {
        final Matcher matcher = SEVERITY_PATTERN.matcher(name);
        if (matcher.matches()) {
            // "@severity-high : real title" => " : real title"
            String strippedDownName = name.substring(matcher.group(1).length());
            // " : real title" or " - real title" => "real title"
            while (strippedDownName.length() > 0 && (Character.isWhitespace(strippedDownName.charAt(0)) || strippedDownName.charAt(0) == ':' || strippedDownName.charAt(0) == '-')) {
                strippedDownName = strippedDownName.substring(1);
            }
            if (strippedDownName.isEmpty()) {
                strippedDownName = "Untitled"; // Eg. "@severity-high : - " is not a name
            }
            return strippedDownName;
        }
        return name;
    }

    /**
     * Postman collections are trees; each folder or request can have a severity.<br>
     * Given the path of a request, get the severity of that request.<br>
     * If several severities are defined in the path, the deepest one wins (request severity wins over parent folders severities).
     *
     * @param path a list of folder names (if any), ended with the request name
     * @return the severity, or an empty string (but never null)
     */
    String getSeverity(String... path) {
        // Deepest severity in the folder/request hierarchy has precedence
        for (int i = path.length - 1; i >= 0; i--) {
            final Matcher matcher = SEVERITY_PATTERN.matcher(path[i]);
            if (matcher.matches()) {
                return matcher.group(1).substring(Tag.SEVERITY_PREFIX.length());
            }
        }
        return "";
    }

    /**
     * For each Newman scenario from the list, attach their related execution given as parameter.
     *
     * @param executions      all executed requests from the Newman execution
     * @param newmanScenarios all Newman executed requests: executions will be appended to them
     */
    void mapExecutionsToScenarios(List<Execution> executions, List<NewmanScenario> newmanScenarios) {
        for (Execution execution : executions) {
            final String itemId = execution.getItem().getId(); // execution.id is mostly the same... but not always!
            Optional<NewmanScenario> newmanScenario = findByItemId(newmanScenarios, itemId);
            if (newmanScenario.isPresent()) {
                newmanScenario.get().setExecution(execution);
            } else {
                log.error("Execution {} has no matching item in the Postman collection", itemId);
            }
        }
    }

    /**
     * Find a Newman scenario in the list by its UUID.
     *
     * @param newmanScenarios each one containing an item
     * @param id              the UUID of the item to search for in the list of scenarios
     * @return the found scenario with the given UUID, if any
     */
    private Optional<NewmanScenario> findByItemId(List<NewmanScenario> newmanScenarios, String id) {
        return newmanScenarios.stream()
                .filter(s -> s.getItem().getId().equals(id))
                .findFirst();
    }

    /**
     * For each Newman scenario from the list, append all their related failures given as parameter.
     *
     * @param failures        all failures of all executed requests from the Newman execution
     * @param newmanScenarios all Newman executed requests: failures will be appended to them
     */
    void mapFailuresToScenarios(List<Failure> failures, List<NewmanScenario> newmanScenarios) {
        for (Failure failure : failures) {
            Optional<NewmanScenario> newmanScenario = findByItemId(newmanScenarios, failure.getSource().getId());
            if (newmanScenario.isPresent()) {
                newmanScenario.get().getFailures().add(failure);
            } else {
                log.error("Failure {} has no matching item in the Postman collection", failure.getSource().getId());
            }
        }
    }

    /**
     * Compute and set the ExecutedScenario content for each Newman scenario.
     *
     * @param newmanScenarios the Newman scenarios to extract: the scenario content will be written in each newmanScenario.scenario.content
     */
    void buildScenarioContents(List<NewmanScenario> newmanScenarios) {
        for (NewmanScenario newmanScenario : newmanScenarios) {
            final Request request = newmanScenario.getItem().getRequest();
            final List<Failure> failures = newmanScenario.getFailures();
            final Execution execution = newmanScenario.getExecution();
            final Response response = execution.getResponse();
            final Assertion[] assertions = execution.getAssertions();
            newmanScenario.getScenario().setContent(buildScenarioContent(request, response, assertions, failures, false));
        }
    }

    /**
     * @param request    (not nullable)
     * @param response   (can be null)
     * @param assertions (can be null)
     * @param failures   (can be null)
     * @param skipped    if true, all steps will have the status "skipped" instead of the computed one
     * @return the scenario content, formatted as if the Postman request was a Cucumber scenario
     */
    String buildScenarioContent(Request request, Response response, Assertion[] assertions, List<Failure> failures, boolean skipped) {
        StringBuilder content = new StringBuilder();

        // Pre-request script
        content.append(LINE_PRE_REQUEST_SCRIPT).append(':')
                .append(getStatus(failures, LINE_PRE_REQUEST_SCRIPT, skipped)).append(':')
                .append(STEP_PRE_REQUEST_SCRIPT);

        // The request (if a JSON is applied in a query parameter, it can contain multi-lines)
        final String[] requestStep = buildRequestStep(request)
                .replace("\r\n", "\n")
                .split("\n");
        // An HTTP status is faked by Newman on request fail: it's up to JavaScript tests to fail on unexpected statuses
        // BUT if someone put an empty request (no URL), Newman will fail the execution (with no assertion at all):
        // we must mark the request failed
        // Same when network error like "read ECONNRESET"
        final String requestStatus = getStatus(failures, REQUEST_LINE, skipped);
        boolean firstLine = true;
        for (String requestLine : requestStep) {
            content.append('\n')
                    .append(REQUEST_LINE).append(':')
                    .append(requestStatus).append(':');
            if (firstLine && response != null) {
                content.append(TimeUnit.MILLISECONDS.toNanos(response.getResponseTime())).append(':');
            }
            content.append(requestLine);
            firstLine = false;
        }

        // All assertions
        if (assertions != null) {
            int i = 0;
            for (Assertion assertion : assertions) {
                content.append('\n')
                        .append(i).append(':')
                        .append(getStatus(failures, i, skipped)).append(':')
                        .append(assertion.getName());
                i++;
            }
        }

        // Post-request test scripts
        content.append('\n')
                .append(LINE_TEST_SCRIPT).append(':')
                .append(getStatus(failures, LINE_TEST_SCRIPT, skipped)).append(':')
                .append(STEP_TEST_SCRIPT);

        return content.toString();
    }

    /**
     * Return the status ("passed" or "failed") of one line in a scenario, given the failed lines.
     *
     * @param failures      the list of failures for a given Postman request
     * @param requestedLine the line we want to know its status inside the given Postman request
     * @param skipped       force return the "skipped" status no matter what other parameters are
     * @return the status of that line in the scenario ("passed" or "failed"), or "skipped" if skipped parameter is true
     */
    String getStatus(List<Failure> failures, int requestedLine, boolean skipped) {
        if (skipped) {
            return Status.SKIPPED.getJsonValue();
        }
        if (failures != null) {
            for (Failure failure : failures) {
                if (toErrorLine(failure) == requestedLine) {
                    return Status.FAILED.getJsonValue();
                }
            }
        }
        return Status.PASSED.getJsonValue();
    }

    /**
     * Append errors to the scenario from the failures of the Newman request.
     *
     * @param newmanScenarios contains failures to extract to the ExecutedScenario where as errors
     */
    void buildScenarioErrors(List<NewmanScenario> newmanScenarios) {
        for (NewmanScenario newmanScenario : newmanScenarios) {
            for (Failure failure : newmanScenario.getFailures()) {
                newmanScenario.getScenario().addError(toError(newmanScenario, failure));
            }
        }
    }

    /**
     * Transform a Newman failure to an error to insert into ARA database.
     *
     * @param newmanScenario the scenario that failed
     * @param failure        one of the possibly many failures on that scenario
     * @return a properly build error mirroring the failure
     */
    com.decathlon.ara.domain.Error toError(NewmanScenario newmanScenario, Failure failure) {
        com.decathlon.ara.domain.Error error = new com.decathlon.ara.domain.Error();
        final Error newmanError = failure.getError();
        error.setException(newmanError.getStack());
        if (error.getException() == null) {
            final boolean hasName = StringUtils.isNotEmpty(newmanError.getName());
            final boolean hasMessage = StringUtils.isNotEmpty(newmanError.getMessage());
            if (hasName && hasMessage) {
                error.setException(newmanError.getName() + ": " + newmanError.getMessage());
            } else if (hasName) {
                error.setException(newmanError.getName());
            } else if (hasMessage) {
                error.setException(newmanError.getMessage());
            } else {
                error.setException("Unknown error");
            }
        }
        error.setStepLine(toErrorLine(failure));
        error.setStep(toStep(newmanScenario, error.getStepLine()));
        error.setStepDefinition(error.getStep());
        return error;
    }

    /**
     * Return the step of a failed line in the scenario (either "<Pre-Request Script>", "<Test Script>", the assertion name or the request, depending on the line).
     *
     * @param newmanScenario if the line is not pre- or post- request script, this method will use the execution's list
     *                       of assertions played during the execution of the request (can be null or empty) or, if the
     *                       line is not from an assertion, the item's request (an "empty request" error, for instance)
     * @param line           the failed line on the scenario for which we want the line "label"
     * @return the step at the given line
     */
    String toStep(NewmanScenario newmanScenario, int line) {
        if (line == LINE_PRE_REQUEST_SCRIPT) {
            return STEP_PRE_REQUEST_SCRIPT;
        }
        if (line == LINE_TEST_SCRIPT) {
            return STEP_TEST_SCRIPT;
        }
        Assertion[] assertions = newmanScenario.getExecution().getAssertions();
        if (assertions != null && line >= 0 && assertions.length > line) {
            return assertions[line].getName();
        }
        // Here, there is no assertion OR the line is not part of the assertions:
        // the only thing that can fail is the request itself.
        // When the request is empty, line is -1 and assertions is null: we catch other strange cases (more negative line, etc.) as a request failure
        return buildRequestStep(newmanScenario.getItem().getRequest());
    }

    /**
     * The line of the failure in the scenario (either pre-request line, test-script line, or the line the assertion, whichever failed).
     *
     * @param failure the failure from which to deduce a line number in the scenario
     * @return the line number of the failure inside the scenario
     */
    int toErrorLine(Failure failure) {
        if (Failure.AT_PRE_REQUEST_SCRIPT.equals(failure.getAt())) {
            return LINE_PRE_REQUEST_SCRIPT;
        }
        if (Failure.AT_TEST_SCRIPT.equals(failure.getAt())) {
            return LINE_TEST_SCRIPT;
        }
        return (failure.getError().getIndex() == null ? REQUEST_LINE : failure.getError().getIndex().intValue());
    }

    String buildRequestStep(Request request) {
        return request.getMethod() + " " + toUrlString(request.getUrl());
    }

    /**
     * @param url a Postman URL object (all fields are decomposed in a semantic object)
     * @return the recomposed URL as a String (eg. "http://server:8080/path?query=string")
     */
    String toUrlString(Url url) {
        final String protocol = url.getProtocol() == null ? "" : url.getProtocol() + "://";
        final String host = url.getHost() == null ? "" : String.join(".", url.getHost());
        final String port = StringUtils.isEmpty(url.getPort()) || "80".equals(url.getPort()) ? "" : ":" + url.getPort();
        final String path = url.getPath() == null ? "" : "/" + String.join("/", url.getPath());
        final String query = toQueryString(url.getQuery());
        return protocol + host + port + path + query;
    }

    /**
     * Return the query String to append to an URL (empty if no query parameter), given a list of key+value couples.
     *
     * @param query eg. [ { "key1": "value1" }, { "key2": "value2" } ]
     * @return eg. "?key1=value1&key2=value2" in the above example
     */
    String toQueryString(KeyValue[] query) {
        if (query == null || query.length == 0) {
            return "";
        }
        return "?" + Arrays.stream(query)
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }

    /**
     * The Newman report processing is a two-stage process.<br>
     * After the streaming of the file and the saving of some data to temporary files, we get a NewmanParsingResult aggregating the useful fragments of the report.<br>
     * This post-process method will take this NewmanParsingResult and do the heavy work of generating a list of ExecutedScenario, with errors and uploaded HTTP request+response log HTML file.<br>
     * This is mainly done by matching the 3 big objects of the report: the collection of items, the execution of request items, and the failures of these executions.
     *
     * @param run              mainly used to get the job URL to set the cucumberReportUrl of each executed scenario
     * @param result           the data parsed from the Newman report JSON file
     * @param newmanReportPath the path of the Newman JSON report file, relative to the jobUrl, used to deduce a collection file name
     * @param requestPosition  incremented for each executed scenario, for them to have a different line number across the entire run
     * @return the Newman reports formatted as executed scenarios with errors, ready to be inserted into database for the run
     */
    public List<ExecutedScenario> postProcess(Run run, NewmanParsingResult result, String newmanReportPath, AtomicInteger requestPosition) {
        final List<NewmanScenario> newmanScenarios = toScenarios(result.getCollection().getItem(), requestPosition, "");
        mapExecutionsToScenarios(result.getExecutions(), newmanScenarios);

        // Remove items without any execution: they were either
        // * not executed (we run root-folder "all" only: remove other root-folders) or
        // * folders (their pre-request and test scripts are executed at each request they contain)
        newmanScenarios.removeIf(s -> s.getExecution() == null);

        mapFailuresToScenarios(result.getFailures(), newmanScenarios);
        buildScenarioContents(newmanScenarios);
        buildScenarioErrors(newmanScenarios);

        // We will only upload HTTP logs for failed requests, to not overload the server with too much details
        for (NewmanScenario newmanScenario : newmanScenarios) {
            if (newmanScenario.getFailures().isEmpty()) {
                deleteTempFile(newmanScenario.getExecution().getResponse());
            }
        }

        final String collectionFileName = toCollectionFileName(run.getType().getSource(), newmanReportPath);
        final String collectionName = result.getCollection().getInfo().getName();
        for (NewmanScenario newmanScenario : newmanScenarios) {
            newmanScenario.getScenario().setFeatureFile(collectionFileName);
            newmanScenario.getScenario().setFeatureName(collectionName);
            newmanScenario.getScenario().setHttpRequestsUrl(uploadHttpLog(newmanScenario));
            newmanScenario.getScenario().setCucumberReportUrl(run.getJobUrl() + "Postman_Collection_Results/");
            newmanScenario.getScenario().setStartDateTime(getStartDateTime(newmanScenario.getExecution().getResponse()).map(Date::from).orElse(null));
        }

        return newmanScenarios.stream()
                .map(NewmanScenario::getScenario)
                .collect(Collectors.toList());
    }

    /**
     * Delete the temporary file generated for a Newman request (removing the HTTP response body file),
     * after having processed the file or after an exception was thrown during report processing, to clean up the server's temporary folder.
     *
     * @param response an HTTP response (can be null, with or without a body stream, itself with or without temporary file)
     */
    void deleteTempFile(Response response) {
        if (response != null && response.getStream() != null) {
            Stream stream = response.getStream();
            final File tempFile = stream.getTempFile();
            if (tempFile != null) {
                try {
                    Files.delete(Paths.get(tempFile.getPath()));
                } catch (IOException e) {
                    log.error("Cannot delete temporary file {}", tempFile, e);
                }
                stream.setTempFile(null);
            }
        }
    }

    /**
     * Delete all temporary files generated for the Newman requests (removing all HTTP response body files),
     * after having processed the files or after an exception was thrown, to clean up the server's temporary folder.
     *
     * @param newmanParsingResult the whole Newman parsing result from a Newman JSON report
     */
    public void deleteTempFiles(NewmanParsingResult newmanParsingResult) {
        if (newmanParsingResult.getExecutions() != null) {
            for (Execution execution : newmanParsingResult.getExecutions()) {
                deleteTempFile(execution.getResponse());
            }
        }
    }

    /**
     * If the given Postman request did execute AND did fail its assertions, generate an HTML log of the request+response, upload it and return the URL of the uploaded HTML debug information.<br>
     * Otherwise, do nothing and return null.
     *
     * @param newmanScenario containing an execution of the request by Newman
     * @return the URL of the uploaded file, or null if no file has been produced or uploaded or failed to upload
     */
    String uploadHttpLog(NewmanScenario newmanScenario) {
        if (!newmanScenario.getFailures().isEmpty()) {
            String html = generateHttpLogHtml(newmanScenario);

            try {
                return assetService.saveHttpLogs(html);
            } finally {
                deleteTempFile(newmanScenario.getExecution().getResponse());
            }
        }
        return null;
    }

    /**
     * Format the actual execution of a Postman request by presenting the request (URL+headers+body) and response (status code + headers + body) in a user-friendly way.
     *
     * @param newmanScenario containing an execution of the request by Newman
     * @return the HTML with all information of the request and its response
     */
    String generateHttpLogHtml(NewmanScenario newmanScenario) {
        StringBuilder html = new StringBuilder();

        html.append("<style>\n" +
                "  body { font-family: 'Avenir', Helvetica, Arial, sans-serif; background-color: #F5F7F9; color: #2c3e50; margin: 16px; }\n" +
                "  body, th, td { font-size: 12px; }\n" +
                "  h2 { margin: 16px 0 8px 8px; font-size: 18px; }\n" +
                "  .card { border: 1px solid #E3E8EE; border-radius: 4px 4px 0 0; background-color: #9CBED8; }\n" +
                "  p { padding: 0 8px; color: white; font-weight: bold; word-wrap: break-word; margin: 6px 0; }\n" +
                "  .content { background-color: white; border: 1px solid white; padding: 0 7px; }\n" +
                "  table { border-spacing: 0; }\n" +
                "  th, td { vertical-align: top; padding: 2px 0; }\n" +
                "  th { text-align: left; white-space: nowrap; padding-right: 4px; }\n" +
                "  table, pre { margin: 7px 0; }\n" +
                "  table pre { word-wrap: break-word; white-space: normal; margin: 0; }\n" +
                "</style>\n");

        html.append("<h2>Request</h2>\n");
        html.append("<div class=\"card\">\n");
        final Request request = newmanScenario.getExecution().getRequest();
        html.append("<p>").append(escapeHtml(buildRequestStep(request))).append(PARAGRAPH_AND_LINE_END);
        html.append(CONTENT_DIV_BEGIN_AND_LINE_END);
        appendKeyValues(html, request.getHeader());
        appendRequestBody(html, request.getBody());
        html.append(DIV_END_AND_LINE_END);
        html.append(DIV_END_AND_LINE_END);

        html.append("<h2>Response</h2>\n");
        html.append("<div class=\"card\">\n");
        final Response response = newmanScenario.getExecution().getResponse();
        if (response == null) {
            html.append("<p>Request Not Executed").append(PARAGRAPH_AND_LINE_END);
            html.append(CONTENT_DIV_BEGIN_AND_LINE_END);
        } else {
            html.append("<p>").append(response.getCode()).append(" ").append(escapeHtml(response.getStatus())).append(PARAGRAPH_AND_LINE_END);
            html.append(CONTENT_DIV_BEGIN_AND_LINE_END);
            appendKeyValues(html, response.getHeader());
            appendResponseBody(html, response.getStream(), getContentType(response.getHeader()));
        }
        html.append(DIV_END_AND_LINE_END);
        html.append(DIV_END_AND_LINE_END);

        return html.toString();
    }

    /**
     * @param headers a list of HTTP headers
     * @return the value of the key "Content-Type", or null if not found
     */
    String getContentType(KeyValue[] headers) {
        return Arrays.stream(headers)
                .filter(h -> "Content-Type".equals(h.getKey()))
                .findFirst()
                .map(KeyValue::getValue)
                .orElse(null);
    }

    /**
     * If any, append a table with all key/value couples into the StringBuilder.
     *
     * @param html      the StringBuilder in which to append the table
     * @param keyValues a list of key/value couples (can be null or empty: nothing will then be appended to html)
     */
    void appendKeyValues(StringBuilder html, KeyValue[] keyValues) {
        if (keyValues != null && keyValues.length != 0) {
            html.append("<table>\n");
            for (KeyValue header : keyValues) {
                html.append("<tr><th>").append(escapeHtml(header.getKey()))
                        .append(":</th><td><pre>").append(escapeHtml(header.getValue()))
                        .append("</pre></td></tr>\n");
            }
            html.append("</table>\n");
        }
    }

    /**
     * If any, append the body content of the HTTP request to the StringBuilder, with HTML formatting depending on the mode of request defined in Postman.
     *
     * @param html the StringBuilder in which to append the body
     * @param body the HTTP request body (can be null or have no mode: nothing will then be appended to html)
     */
    void appendRequestBody(StringBuilder html, Body body) {
        if (body == null || body.getMode() == null) {
            return;
        }

        switch (body.getMode()) {
            case "formdata":
                appendKeyValues(html, body.getFormData());
                break;
            case "urlencoded":
                appendKeyValues(html, body.getUrlEncoded());
                break;
            case "raw":
                html.append("<pre>").append(escapeHtml(body.getRaw())).append("</pre>\n");
                break;
            case "file":
                log.error("File upload was used in a collection, while its collection export is absent in Postman.");
                html.append(ERROR_PARAGRAPH +
                        "Postman does not export the files to upload, so it has not been sent..." +
                        PARAGRAPH_AND_LINE_END);
                break;
            default:
                log.error("Used new/unknown request body mode {}", body.getMode());
                html.append(ERROR_PARAGRAPH + "Used new (unknown to ARA) request body mode ")
                        .append(body.getMode())
                        .append(PARAGRAPH_AND_LINE_END);
                break;
        }
    }

    /**
     * If any, reads the temp file containing the HTTP response body and append it as String (prettyified if JSON) in the build HTML.
     *
     * @param html        the StringBuilder in which to append the body
     * @param stream      only tmpFile is used (but it is NOT deleted by this method, to allow several retry attempts)
     * @param contentType the content type of the file (charset is assumed UTF-8)
     */
    void appendResponseBody(StringBuilder html, Stream stream, String contentType) {
        if (stream.getTempFile() != null) {
            String fileContent;
            try {
                fileContent = FileUtils.readFileToString(stream.getTempFile(), StandardCharsets.UTF_8);
                fileContent = prettyPrint(fileContent, contentType);
            } catch (IOException e) {
                log.error("Cannot read temporary file {}", stream.getTempFile(), e);
                fileContent = "Error in ARA while reading the content of the response received by Newman:\n" +
                        ExceptionUtils.getStackTrace(e);
            }

            html.append("<pre>").append(escapeHtml(fileContent)).append("</pre>\n");
        }
    }

    /**
     * Indent JSON in a pretty format, or return the content as is if the content is not a JSON or cannot be parsed.
     *
     * @param fileContent data to format prettyly if it is a valid JSON
     * @param contentType the content type of the data
     * @return the content itself, or a nicely-indented JSON if it is a JSON and can be parsed
     */
    String prettyPrint(String fileContent, String contentType) {
        if (MediaType.APPLICATION_JSON_VALUE.equals(contentType) || StringUtils.startsWith(contentType, MediaType.APPLICATION_JSON_VALUE + ";")) {
            try {
                Object json = objectMapper.readValue(fileContent, Object.class);
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            } catch (@SuppressWarnings(
                    "squid:S1166") IOException e) { // Exception handlers should preserve the original exceptions
                // Ignore exception because it is a user malformed object, and we gracefully fallback by not indenting it
            }
        }
        return fileContent;
    }

    /**
     * @param unescaped a String to escape
     * @return the escaped value of the String, so it can be safely included as is in an HTML code
     */
    String escapeHtml(String unescaped) {
        return StringEscapeUtils.escapeXml10(unescaped);
    }

    /**
     * Given an HTTP response with headers, return the Date header value if any, null otherwise.
     *
     * @param response the HTTP response
     * @return the parsed Date header value or null if none or unparsable
     */
    Optional<Instant> getStartDateTime(Response response) {
        if (response == null) {
            return Optional.empty();
        }
        // @see https://tools.ietf.org/html/rfc7231#section-7.1.1.2
        return Arrays.stream(response.getHeader())
                .filter(header -> "Date".equalsIgnoreCase(header.getKey()))
                .findFirst()
                .map(KeyValue::getValue)
                .map(value -> {
                    try {
                        return ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
                    } catch (DateTimeParseException e) {
                        log.error("Cannot parse date from response header in Newman report: {}", value, e);
                        return null;
                    }
                });
    }

    /**
     * Given a path of Newman JSON report file, deduce the name of the original Postman collection file.<br>
     * A collection CAN be executed twice: with "--folder all" and with eg. "--folder us".<br>
     * There will optionally then be two reports for the same collection, with the country as a suffix.<br>
     * This method removes this suffix to deduce the same collection file name for both executions.<br>
     * Country suffix is a "_" followed by one or more 2 or 3 group of lowercase letters separated by a plus sign.
     *
     * @param newmanReportPath eg. "reports/collection_all.json", "reports/collection_fr+us.json" or "reports/collection.json"
     * @return eg. "collection.json" for the three given examples of {@code newmanReportPath}
     */
    String toCollectionFileName(Source source, String newmanReportPath) {
        if (source.isPostmanCountryRootFolders()) {
            // Remove optional country suffix ("_all", "_fr", "_all+fr+us"...)
            return newmanReportPath.replaceAll("_[a-z]{2,3}(\\+[a-z]{2,3})*.json$", ".json");
        }
        return newmanReportPath;
    }

}
