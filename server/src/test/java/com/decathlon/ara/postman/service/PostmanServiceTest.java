package com.decathlon.ara.postman.service;

import com.decathlon.ara.domain.Error;
import com.decathlon.ara.domain.ExecutedScenario;
import com.decathlon.ara.domain.Run;
import com.decathlon.ara.domain.Type;
import com.decathlon.ara.postman.bean.Assertion;
import com.decathlon.ara.postman.bean.Body;
import com.decathlon.ara.postman.bean.Collection;
import com.decathlon.ara.postman.bean.Execution;
import com.decathlon.ara.postman.bean.Failure;
import com.decathlon.ara.postman.bean.Info;
import com.decathlon.ara.postman.bean.Item;
import com.decathlon.ara.postman.bean.ItemId;
import com.decathlon.ara.postman.bean.KeyValue;
import com.decathlon.ara.postman.bean.Request;
import com.decathlon.ara.postman.bean.Response;
import com.decathlon.ara.postman.bean.Source;
import com.decathlon.ara.postman.bean.Stream;
import com.decathlon.ara.postman.bean.Url;
import com.decathlon.ara.postman.model.NewmanParsingResult;
import com.decathlon.ara.postman.model.NewmanScenario;
import com.decathlon.ara.report.asset.AssetService;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static com.decathlon.ara.util.TestUtil.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PostmanServiceTest {

    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String JSON_RAW = "[{\"key\":\"value\"}]";
    private static final String JSON_INDENTED = "[ {" + NEW_LINE + "  \"key\" : \"value\"" + NEW_LINE + "} ]";
    private static final String[] NO_PARENT = new String[0];

    @Spy
    private ObjectMapper objectMapper;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private AssetService assetService;

    @Spy
    @InjectMocks
    private PostmanService cut;

    @Test(expected = IOException.class)
    public void parse_should_throw_exception_when_malformed_json() throws IOException {
        // GIVEN
        when(Boolean.valueOf(jsonParser.isClosed())).thenReturn(Boolean.FALSE);
        when(jsonParser.nextToken()).thenReturn(JsonToken.START_ARRAY);

        // WHEN
        cut.parse(jsonParser, null);
    }

    @Test(expected = IOException.class)
    public void parse_should_throw_exception_when_closed_stream() throws IOException {
        // GIVEN
        when(Boolean.valueOf(jsonParser.isClosed())).thenReturn(Boolean.TRUE);

        // WHEN
        cut.parse(jsonParser, null);
    }

    @Test
    public void parse_should_parse_the_newman_report_stream() throws IOException {
        // Yes, for a proper JUnit test, JsonParser should be mocked and all parse*() methods should be tested independently.
        // This would be too time-consuming to write, and we are not sure yet if the streaming benefit is that strong, so the code could go away in a future version.

        // GIVEN
        String json = "{\n" +
                "    \"collection\": {\n" +
                "        \"info\": {\n" +
                "            \"name\": \"collection-name\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"run\": {\n" +
                "        \"executions\": [\n" +
                "            {\n" +
                "                \"item\": {\n" +
                "                    \"id\": \"execution-item-id\"\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"item\": {\n" +
                "                    \"id\": \"another-execution-item-id\"\n" +
                "                }\n" +
                "            }\n" +
                "        ],\n" +
                "        \"failures\": [\n" +
                "            {\n" +
                "                \"at\": \"some-line\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"at\": \"some-other-line\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"unmapped\": -42\n" +
                "    },\n" +
                "    \"unmapped\": -42\n" +
                "}";
        JsonParser jsonParser = new JsonFactory().createParser(json);
        NewmanParsingResult result = new NewmanParsingResult();

        doAnswer(invocation -> null).when(cut).saveExecutionStreamToFile(any(Execution.class));

        // WHEN
        cut.parse(jsonParser, result);

        // THEN
        verify(cut, times(2)).saveExecutionStreamToFile(any(Execution.class));
        assertThat(result.getCollection().getInfo().getName()).isEqualTo("collection-name");
        assertThat(result.getExecutions()).hasSize(2);
        assertThat(result.getExecutions().get(0).getItem().getId()).isEqualTo("execution-item-id");
        assertThat(result.getExecutions().get(1).getItem().getId()).isEqualTo("another-execution-item-id");
        assertThat(result.getFailures()).hasSize(2);
        assertThat(result.getFailures().get(0).getAt()).isEqualTo("some-line");
        assertThat(result.getFailures().get(1).getAt()).isEqualTo("some-other-line");
    }

    @Test
    public void saveExecutionStreamToFile_should_not_crash_if_execution_has_null_response() throws IOException {
        // GIVEN
        Execution execution = new Execution();

        // WHEN
        cut.saveExecutionStreamToFile(execution);
    }

    @Test
    public void saveExecutionStreamToFile_should_not_crash_if_response_has_null_stream() throws IOException {
        // GIVEN
        Execution execution = new Execution()
                .withResponse(new Response());

        // WHEN
        cut.saveExecutionStreamToFile(execution);
    }

    @Test
    public void saveExecutionStreamToFile_should_not_crash_if_stream_has_null_data() throws IOException {
        // GIVEN
        Execution execution = new Execution()
                .withResponse(new Response()
                        .withStream(new Stream()));

        // WHEN
        cut.saveExecutionStreamToFile(execution);
    }

    @Test
    public void saveExecutionStreamToFile_should_save_execution_stream_to_file() throws IOException {
        // GIVEN
        final Stream stream = new Stream()
                .withData(new byte[] { 'a', 'b', 'c' });
        Execution execution = new Execution()
                .withResponse(new Response()
                        .withStream(stream));

        try {
            // WHEN
            cut.saveExecutionStreamToFile(execution);

            // THEN
            assertThat(stream.getData()).isNull();
            assertThat(stream.getTempFile()).isNotNull();
            assertThat(FileUtils.readFileToString(stream.getTempFile(), StandardCharsets.UTF_8)).isEqualTo("abc");
        } finally {
            FileUtils.deleteQuietly(stream.getTempFile());
        }
    }

    @Test
    public void toScenarios_should_return_empty_list_when_items_is_null() {
        // WHEN
        final List<NewmanScenario> newmanScenarios = cut.toScenarios(null, null, null);

        // THEN
        assertThat(newmanScenarios).isEmpty();
    }

    @Test
    public void toScenarios_should_wrap_item_tree_into_newman_scenario_list() {
        // GIVEN
        Item[] items = new Item[] {
                new Item()
                        .withName("parent")
                        .withChildren(new Item[] {
                        new Item().withName("request1"),
                        new Item().withName("request2")
                })
        };

        // WHEN
        final List<NewmanScenario> newmanScenarios = cut.toScenarios(items, new AtomicInteger(-42), null, NO_PARENT);

        // THEN
        assertThat(newmanScenarios.stream().map(n -> n.getItem().getName()))
                .containsExactly("parent", "request1", "request2");
    }

    @Test
    public void toScenarios_should_append_executed_scenarios_to_newman_scenarios() {
        // GIVEN
        Item[] items = new Item[] {
                new Item()
                        .withName("parent")
                        .withChildren(new Item[] {
                        new Item().withName("request1"),
                        new Item().withName("request2")
                })
        };

        // WHEN
        final List<NewmanScenario> newmanScenarios = cut.toScenarios(items, new AtomicInteger(-42), null, NO_PARENT);

        // THEN
        assertThat(newmanScenarios.stream().map(n -> n.getScenario().getName()))
                .containsExactly("parent", "parent \u25b6 request1", "parent \u25b6 request2");
    }

    @Test
    public void toScenarios_should_increment_lines() {
        // GIVEN
        Item[] items = new Item[] {
                new Item()
                        .withName("parent")
                        .withChildren(new Item[] {
                        new Item().withName("request1"),
                        new Item().withName("request2")
                })
        };
        AtomicInteger requestPosition = new AtomicInteger(5);

        // WHEN
        final List<NewmanScenario> newmanScenarios = cut.toScenarios(items, requestPosition, null, NO_PARENT);

        // THEN
        assertThat(newmanScenarios.stream().map(n -> Integer.valueOf(n.getScenario().getLine())))
                .containsExactly(Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(8));
    }

    @Test
    public void toScenarios_should_fill_the_severity_of_executed_scenarios_by_calling_toSeverity_with_parent_and_request_names() {
        // GIVEN
        Item[] items = new Item[] { new Item().withName("request") };
        final String[] parentFolders = new String[] { "parent" };
        doReturn("the-severity").when(cut).getSeverity(eq("parent"), eq("request"));

        // WHEN
        final List<NewmanScenario> newmanScenarios = cut.toScenarios(items, new AtomicInteger(-42), null, parentFolders);

        // THEN
        final ExecutedScenario executedScenario = newmanScenarios.get(0).getScenario();
        assertThat(executedScenario.getSeverity()).isEqualTo("the-severity");
    }

    @Test
    public void toScenarios_should_fill_tags_of_executed_scenarios_with_the_severity() {
        // GIVEN
        Item[] items = new Item[] { new Item().withName("request") };
        doReturn("the-severity").when(cut).getSeverity(eq("request"));

        // WHEN
        final List<NewmanScenario> newmanScenarios = cut.toScenarios(items, new AtomicInteger(-42), null, NO_PARENT);

        // THEN
        final ExecutedScenario executedScenario = newmanScenarios.get(0).getScenario();
        assertThat(executedScenario.getTags()).isEqualTo("@severity-the-severity");
    }

    @Test
    public void toScenarios_should_not_fill_tags_of_executed_scenarios_if_the_severity_is_empty() {
        // GIVEN
        Item[] items = new Item[] { new Item().withName("request") };
        doReturn("").when(cut).getSeverity(eq("request"));

        // WHEN
        final List<NewmanScenario> newmanScenarios = cut.toScenarios(items, new AtomicInteger(-42), null, NO_PARENT);

        // THEN
        final ExecutedScenario executedScenario = newmanScenarios.get(0).getScenario();
        assertThat(executedScenario.getTags()).isNull();
    }

    @Test
    public void toScenarios_should_fill_name_of_executed_scenarios_with_the_parent_and_request_names() {
        // GIVEN
        Item[] items = new Item[] { new Item().withName("request") };
        final String[] parentFolders = new String[] { "parent" };
        doReturn("request-without-severity").when(cut).removeSeverityTag(eq("request"));

        // WHEN
        final List<NewmanScenario> newmanScenarios = cut.toScenarios(items, new AtomicInteger(-42), null, parentFolders);

        // THEN
        final ExecutedScenario executedScenario = newmanScenarios.get(0).getScenario();
        assertThat(executedScenario.getName()).isEqualTo("parent \u25b6 request-without-severity");
    }

    @Test
    public void toScenarios_should_fill_cucumber_id_of_executed_scenarios_with_toCucumberId_with_the_name_without_severity() {
        // GIVEN
        Item[] items = new Item[] { new Item().withName("request") };
        doReturn("request-without-severity").when(cut).removeSeverityTag(eq("request"));
        doReturn("cucumber-id").when(cut).toCucumberId(AdditionalMatchers.aryEq(new String[] { "request-without-severity" }));

        // WHEN
        final List<NewmanScenario> newmanScenarios = cut.toScenarios(items, new AtomicInteger(-42), null, NO_PARENT);

        // THEN
        final ExecutedScenario executedScenario = newmanScenarios.get(0).getScenario();
        assertThat(executedScenario.getCucumberId()).isEqualTo("cucumber-id");
    }

    @Test
    public void toScenarios_should_inherit_parent_severity_when_no_severity_in_name() {
        // GIVEN
        Item[] items = new Item[] { new Item().withName("request") };
        doReturn("").when(cut).getSeverity(eq("request"));

        // WHEN
        final List<NewmanScenario> newmanScenarios = cut.toScenarios(items, new AtomicInteger(-42), "parent-severity", NO_PARENT);

        // THEN
        final ExecutedScenario executedScenario = newmanScenarios.get(0).getScenario();
        assertThat(executedScenario.getSeverity()).isEqualTo("parent-severity");
    }

    @Test
    public void toScenarios_should_override_parent_severity_when_a_severity_is_present_in_name() {
        // GIVEN
        Item[] items = new Item[] { new Item().withName("request") };
        doReturn("severity").when(cut).getSeverity(eq("request"));

        // WHEN
        final List<NewmanScenario> newmanScenarios = cut.toScenarios(items, new AtomicInteger(-42), "parent-severity", NO_PARENT);

        // THEN
        final ExecutedScenario executedScenario = newmanScenarios.get(0).getScenario();
        assertThat(executedScenario.getSeverity()).isEqualTo("severity");
    }

    @Test
    public void toCucumberId_should_return_concatenated_and_without_functionalities() {
        // GIVEN
        String[] path = new String[] {
                // Testing only a few functionality syntaxes: all possibilities are tested by
                // ScenarioExtractorUtil.removeFunctionalitiesFromScenarioName()
                "all",
                "Functionality 42: folder",
                "Functionalities 43, 44: sub-folder",
                "sub-sub-folder",
                "Functionality 45: request"
        };

        // WHEN
        final String id = cut.toCucumberId(path);

        // THEN
        assertThat(id).isEqualTo("all/folder/sub-folder/sub-sub-folder/request");
    }

    @Test
    public void toCucumberId_should_return_limit_max_characters() {
        // GIVEN
        String[] path = new String[] {
                // Testing only a few functionality syntaxes: all possibilities are tested by
                // ScenarioExtractorUtil.removeFunctionalitiesFromScenarioName()
                "_7890123456789012345678901234567890",
                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
        };

        // WHEN
        final String id = cut.toCucumberId(path);

        // THEN
        assertThat(id).isEqualTo("" +
                "7890123456789012345678901234567890/" +
                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890/" +
                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890/" +
                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890/" +
                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890/" +
                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890/" +
                "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
    }

    @Test
    public void testRemoveSeverityTag() {
        assertThat(cut.removeSeverityTag("@severity-high - real title")).isEqualTo("real title");
        assertThat(cut.removeSeverityTag("@severity-high : real title")).isEqualTo("real title");
        assertThat(cut.removeSeverityTag("@severity-high: real title")).isEqualTo("real title");
        assertThat(cut.removeSeverityTag("@severity-high real title")).isEqualTo("real title");
        assertThat(cut.removeSeverityTag("@severity-high : - \t ")).isEqualTo("Untitled");
        assertThat(cut.removeSeverityTag("@severity-high")).isEqualTo("Untitled");
        assertThat(cut.removeSeverityTag("@severity-NOT_A_SEVERITY : real title")).isEqualTo("@severity-NOT_A_SEVERITY : real title");
        assertThat(cut.removeSeverityTag("@not-a-severity : real title")).isEqualTo("@not-a-severity : real title");

        // '-' should be in the middle to be included: if at the end, it denotes a separation of the tag from the title
        assertThat(cut.removeSeverityTag("@severity-sanity-check Some")).isEqualTo("Some");
        assertThat(cut.removeSeverityTag("@severity-higher-sanity-check- Some")).isEqualTo("Some");
        assertThat(cut.removeSeverityTag("@severity-high- Some")).isEqualTo("Some");
    }

    @Test
    public void testGetSeverity() {
        // Only a request
        assertThat(cut.getSeverity("@severity-high request")).isEqualTo("high");

        // Dashes should also be part of the severity
        assertThat(cut.getSeverity("@severity-sanity-check request")).isEqualTo("sanity-check");

        // The severity is in the folder
        assertThat(cut.getSeverity("@severity-high folder", "request")).isEqualTo("high");

        // Conflicting severities: the last one wins
        assertThat(cut.getSeverity("@severity-high folder", "@severity-medium request")).isEqualTo("medium");

        // Sub folders complete example
        assertThat(cut.getSeverity("base-folder", "@severity-sanity-check folder", "@severity-medium sub-folder", "request")).isEqualTo("medium");

        // Uppercase not recognized
        assertThat(cut.getSeverity("@severity-SANITY-CHECK request")).isEmpty();

        // No prefix not recognized
        assertThat(cut.getSeverity("high request")).isEmpty();

        // '-' should be in the middle to be included: if at the end, it denotes a separation of the tag from the title
        assertThat(cut.getSeverity("@severity-sanity-check Some ")).isEqualTo("sanity-check");
        assertThat(cut.getSeverity("@severity-higher-sanity-check- Some ")).isEqualTo("higher-sanity-check");
        assertThat(cut.getSeverity("@severity-high- Some ")).isEqualTo("high");
    }

    @Test
    public void mapExecutionsToScenarios_should_match_executions_to_scenarios_if_any() {
        // GIVEN
        final Execution execution2 = new Execution().withItem(new ItemId().withId("id2"));
        final Execution execution3 = new Execution().withItem(new ItemId().withId("id3"));
        List<Execution> executions = Arrays.asList(execution2, execution3);

        final NewmanScenario newmanScenario1 = new NewmanScenario().withItem(new Item().withId("id1"));
        final NewmanScenario newmanScenario2 = new NewmanScenario().withItem(new Item().withId("id2"));
        List<NewmanScenario> newmanScenarios = Arrays.asList(newmanScenario1, newmanScenario2);

        // WHEN
        cut.mapExecutionsToScenarios(executions, newmanScenarios);

        // THEN
        assertThat(newmanScenario1.getExecution()).isNull();
        assertThat(newmanScenario2.getExecution()).isSameAs(execution2);
    }

    @Test
    public void mapFailuresToScenarios_should_match_failures_to_scenarios_if_any() {
        // GIVEN
        final Failure failure2 = new Failure().withSource(new Source().withId("id2"));
        final Failure failure3 = new Failure().withSource(new Source().withId("id3"));
        List<Failure> failures = Arrays.asList(failure2, failure3);

        final NewmanScenario newmanScenario1 = new NewmanScenario().withItem(new Item().withId("id1"));
        final NewmanScenario newmanScenario2 = new NewmanScenario().withItem(new Item().withId("id2"));
        List<NewmanScenario> newmanScenarios = Arrays.asList(newmanScenario1, newmanScenario2);

        // WHEN
        cut.mapFailuresToScenarios(failures, newmanScenarios);

        // THEN
        assertThat(newmanScenario1.getFailures()).isEmpty();
        assertThat(newmanScenario2.getFailures()).hasSize(1);
        assertThat(newmanScenario2.getFailures().get(0)).isSameAs(failure2);
    }

    @Test
    public void buildScenarioContents_should_build_the_scenario_content() {
        // GIVEN
        final NewmanScenario newmanScenario = new NewmanScenario()
                .withItem(new Item()
                        .withRequest(new Request()))
                .withExecution(new Execution()
                        .withResponse(new Response()
                                .withResponseTime(12))
                        .withAssertions(new Assertion[] {
                                new Assertion("assertion-1"),
                                new Assertion("assertion-2")
                        }))
                .withScenario(new ExecutedScenario());
        List<NewmanScenario> newmanScenarios = Collections.singletonList(newmanScenario);

        doReturn("pre_status").when(cut).getStatus(same(newmanScenario.getFailures()), eq(-100000), eq(false));
        doReturn("request_step").when(cut).buildRequestStep(same(newmanScenario.getItem().getRequest()));
        doReturn("assertion_0_status").when(cut).getStatus(same(newmanScenario.getFailures()), eq(0), eq(false));
        doReturn("assertion_1_status").when(cut).getStatus(same(newmanScenario.getFailures()), eq(1), eq(false));
        doReturn("test_status").when(cut).getStatus(same(newmanScenario.getFailures()), eq(100000), eq(false));

        // WHEN
        cut.buildScenarioContents(newmanScenarios);

        // THEN
        assertThat(newmanScenarios.get(0).getScenario().getContent()).isEqualTo("" +
                "-100000:pre_status:<Pre-Request Script>\n" +
                "-1:passed:12000000:request_step\n" +
                "0:assertion_0_status:assertion-1\n" +
                "1:assertion_1_status:assertion-2\n" +
                "100000:test_status:<Test Script>");
    }

    @Test
    public void buildScenarioContents_should_build_the_scenario_content_when_no_assertions_are_made() {
        // GIVEN
        final NewmanScenario newmanScenario = new NewmanScenario()
                .withItem(new Item()
                        .withRequest(new Request()))
                .withExecution(new Execution())
                .withScenario(new ExecutedScenario());
        List<NewmanScenario> newmanScenarios = Collections.singletonList(
                newmanScenario);

        doReturn("pre_status").when(cut).getStatus(same(newmanScenario.getFailures()), eq(-100000), eq(false));
        doReturn("request_status").when(cut).getStatus(same(newmanScenario.getFailures()), eq(-1), eq(false));
        doReturn("request_step").when(cut).buildRequestStep(same(newmanScenario.getItem().getRequest()));
        doReturn("test_status").when(cut).getStatus(same(newmanScenario.getFailures()), eq(100000), eq(false));

        // WHEN
        cut.buildScenarioContents(newmanScenarios);

        // THEN
        assertThat(newmanScenarios.get(0).getScenario().getContent()).isEqualTo("" +
                "-100000:pre_status:<Pre-Request Script>\n" +
                "-1:request_status:request_step\n" +
                "100000:test_status:<Test Script>");
    }

    @Test
    public void buildScenarioContents_should_support_multi_lines_requests() {
        // GIVEN
        final NewmanScenario newmanScenario = new NewmanScenario()
                .withItem(new Item()
                        .withRequest(new Request()))
                .withExecution(new Execution().withResponse(new Response().withResponseTime(42)))
                .withScenario(new ExecutedScenario());
        List<NewmanScenario> newmanScenarios = Collections.singletonList(
                newmanScenario);

        doReturn("pre_status").when(cut).getStatus(same(newmanScenario.getFailures()), eq(-100000), eq(false));
        doReturn("request_status").when(cut).getStatus(same(newmanScenario.getFailures()), eq(-1), eq(false));
        doReturn("the\r\nrequest\nstep").when(cut).buildRequestStep(same(newmanScenario.getItem().getRequest()));
        doReturn("test_status").when(cut).getStatus(same(newmanScenario.getFailures()), eq(100000), eq(false));

        // WHEN
        cut.buildScenarioContents(newmanScenarios);

        // THEN
        assertThat(newmanScenarios.get(0).getScenario().getContent()).isEqualTo("" +
                "-100000:pre_status:<Pre-Request Script>\n" +
                "-1:request_status:42000000:the\n" +
                "-1:request_status:request\n" +
                "-1:request_status:step\n" +
                "100000:test_status:<Test Script>");
    }

    @Test
    public void getStatus_should_return_failed_when_the_requested_line_is_failed() {
        // GIVEN
        Failure failure1 = new Failure();
        Failure failure2 = new Failure();
        List<Failure> failures = Arrays.asList(failure1, failure2);

        doReturn(Integer.valueOf(1)).when(cut).toErrorLine(same(failure1));
        doReturn(Integer.valueOf(2)).when(cut).toErrorLine(same(failure2));

        // WHEN
        final String status = cut.getStatus(failures, 2, false);

        // THEN
        assertThat(status).isEqualTo("failed");
    }

    @Test
    public void getStatus_should_return_passed_when_the_requested_line_did_not_fail() {
        // GIVEN
        Failure failure1 = new Failure();
        Failure failure2 = new Failure();
        List<Failure> failures = Arrays.asList(failure1, failure2);

        doReturn(Integer.valueOf(1)).when(cut).toErrorLine(same(failure1));
        doReturn(Integer.valueOf(2)).when(cut).toErrorLine(same(failure2));

        // WHEN
        final String status = cut.getStatus(failures, 5, false);

        // THEN
        assertThat(status).isEqualTo("passed");
    }

    @Test
    public void getStatus_should_return_skipped_when_asked_to_override_other_statuses() {
        // GIVEN
        List<Failure> failures = Collections.singletonList(new Failure());

        // WHEN
        final String status = cut.getStatus(failures, 5, true);

        // THEN
        assertThat(status).isEqualTo("skipped");
    }

    @Test
    public void getStatus_should_return_passed_when_no_failure() {
        // GIVEN
        List<Failure> failures = Collections.emptyList();

        // WHEN
        final String status = cut.getStatus(failures, 5, false);

        // THEN
        assertThat(status).isEqualTo("passed");
    }

    @Test
    public void getStatus_should_return_passed_when_null_failure_list() {
        // WHEN
        final String status = cut.getStatus(null, 5, false);

        // THEN
        assertThat(status).isEqualTo("passed");
    }

    @Test
    public void buildScenarioErrors_should_fill_list_with_generated_errors() {
        // GIVEN
        Failure failure11 = new Failure();
        Failure failure12 = new Failure();
        Failure failure21 = new Failure();

        NewmanScenario scenario1 = new NewmanScenario().withScenario(new ExecutedScenario().withId(Long.valueOf(1)));
        NewmanScenario scenario2 = new NewmanScenario().withScenario(new ExecutedScenario().withId(Long.valueOf(2)));

        scenario1.getFailures().add(failure11);
        scenario1.getFailures().add(failure12);
        scenario2.getFailures().add(failure21);

        List<NewmanScenario> newmanScenarios = Arrays.asList(scenario1, scenario2);

        Error error11 = new Error().withStepLine(11);
        Error error12 = new Error().withStepLine(12);
        Error error21 = new Error().withStepLine(21);

        doReturn(error11).when(cut).toError(same(scenario1), same(failure11));
        doReturn(error12).when(cut).toError(same(scenario1), same(failure12));
        doReturn(error21).when(cut).toError(same(scenario2), same(failure21));

        // WHEN
        cut.buildScenarioErrors(newmanScenarios);

        // THEN
        assertThat(scenario1.getScenario().getErrors()).hasSize(2);
        assertThat(get(scenario1.getScenario().getErrors(), 0)).isSameAs(error11);
        assertThat(get(scenario1.getScenario().getErrors(), 1)).isSameAs(error12);
        assertThat(scenario2.getScenario().getErrors()).hasSize(1);
        assertThat(get(scenario2.getScenario().getErrors(), 0)).isSameAs(error21);
    }

    @Test
    public void toError_should_fill_up_error() {
        // GIVEN
        Request request = new Request();
        final Assertion[] assertions = new Assertion[0];
        NewmanScenario newmanScenario = new NewmanScenario()
                .withExecution(new Execution()
                        .withAssertions(assertions))
                .withItem(new Item().withRequest(request));
        Failure failure = new Failure()
                .withError(new com.decathlon.ara.postman.bean.Error()
                        .withStack("stack"));
        doReturn(Integer.valueOf(42)).when(cut).toErrorLine(same(failure));
        doReturn("step").when(cut).toStep(same(newmanScenario), eq(42));

        // WHEN
        Error error = cut.toError(newmanScenario, failure);

        // THEN
        assertThat(error.getException()).isEqualTo("stack");
        assertThat(error.getStepLine()).isEqualTo(42);
        assertThat(error.getStep()).isEqualTo("step");
        assertThat(error.getStepDefinition()).isEqualTo("step");
    }

    @Test
    public void toError_should_return_name_and_message_when_null_stack() {
        // GIVEN
        NewmanScenario newmanScenario = new NewmanScenario()
                .withExecution(new Execution());
        Failure failure = new Failure()
                .withError(new com.decathlon.ara.postman.bean.Error()
                        .withName("name")
                        .withMessage("message"));
        doReturn(Integer.valueOf(42)).when(cut).toErrorLine(any());
        doReturn("any").when(cut).toStep(any(), eq(42));

        // WHEN
        Error error = cut.toError(newmanScenario, failure);

        // THEN
        assertThat(error.getException()).isEqualTo("name: message");
    }

    @Test
    public void toError_should_return_name_when_empty_stack_and_null_message() {
        // GIVEN
        NewmanScenario newmanScenario = new NewmanScenario()
                .withExecution(new Execution());
        Failure failure = new Failure()
                .withError(new com.decathlon.ara.postman.bean.Error()
                        .withName("name")
                        .withMessage(null));
        doReturn(Integer.valueOf(42)).when(cut).toErrorLine(any());
        doReturn("any").when(cut).toStep(any(), eq(42));

        // WHEN
        Error error = cut.toError(newmanScenario, failure);

        // THEN
        assertThat(error.getException()).isEqualTo("name");
    }

    @Test
    public void toError_should_return_name_when_empty_stack_and_empty_message() {
        // GIVEN
        NewmanScenario newmanScenario = new NewmanScenario()
                .withExecution(new Execution());
        Failure failure = new Failure()
                .withError(new com.decathlon.ara.postman.bean.Error()
                        .withName("name")
                        .withMessage(""));
        doReturn(Integer.valueOf(42)).when(cut).toErrorLine(any());
        doReturn("any").when(cut).toStep(any(), eq(42));

        // WHEN
        Error error = cut.toError(newmanScenario, failure);

        // THEN
        assertThat(error.getException()).isEqualTo("name");
    }

    @Test
    public void toError_should_return_message_when_empty_stack_and_null_name() {
        // GIVEN
        NewmanScenario newmanScenario = new NewmanScenario()
                .withExecution(new Execution());
        Failure failure = new Failure()
                .withError(new com.decathlon.ara.postman.bean.Error()
                        .withName(null)
                        .withMessage("message"));
        doReturn(Integer.valueOf(42)).when(cut).toErrorLine(any());
        doReturn("any").when(cut).toStep(any(), eq(42));

        // WHEN
        Error error = cut.toError(newmanScenario, failure);

        // THEN
        assertThat(error.getException()).isEqualTo("message");
    }

    @Test
    public void toError_should_return_message_when_empty_stack_and_empty_name() {
        // GIVEN
        NewmanScenario newmanScenario = new NewmanScenario()
                .withExecution(new Execution());
        Failure failure = new Failure()
                .withError(new com.decathlon.ara.postman.bean.Error()
                        .withName("")
                        .withMessage("message"));
        doReturn(Integer.valueOf(42)).when(cut).toErrorLine(any());
        doReturn("any").when(cut).toStep(any(), eq(42));

        // WHEN
        Error error = cut.toError(newmanScenario, failure);

        // THEN
        assertThat(error.getException()).isEqualTo("message");
    }

    @Test
    public void toError_should_return_unknown_error_when_empty_stack_and_null_name_and_message() {
        // GIVEN
        NewmanScenario newmanScenario = new NewmanScenario()
                .withExecution(new Execution());
        Failure failure = new Failure()
                .withError(new com.decathlon.ara.postman.bean.Error()
                        .withName(null)
                        .withMessage(null));
        doReturn(Integer.valueOf(42)).when(cut).toErrorLine(any());
        doReturn("any").when(cut).toStep(any(), eq(42));

        // WHEN
        Error error = cut.toError(newmanScenario, failure);

        // THEN
        assertThat(error.getException()).isEqualTo("Unknown error");
    }

    @Test
    public void toError_should_return_unknown_error_when_empty_stack_and_empty_name_and_message() {
        // GIVEN
        NewmanScenario newmanScenario = new NewmanScenario()
                .withExecution(new Execution());
        Failure failure = new Failure()
                .withError(new com.decathlon.ara.postman.bean.Error()
                        .withName("")
                        .withMessage(""));
        doReturn(Integer.valueOf(42)).when(cut).toErrorLine(any());
        doReturn("any").when(cut).toStep(any(), eq(42));

        // WHEN
        Error error = cut.toError(newmanScenario, failure);

        // THEN
        assertThat(error.getException()).isEqualTo("Unknown error");
    }

    @Test
    public void toStep_should_return_pre_request_script_for_minus_100000() {
        // WHEN
        final String step = cut.toStep(null, -100000);

        // THEN
        assertThat(step).isEqualTo("<Pre-Request Script>");
    }

    @Test
    public void toStep_should_return_test_script_for_100000() {
        // WHEN
        final String step = cut.toStep(null, 100000);

        // THEN
        assertThat(step).isEqualTo("<Test Script>");
    }

    @Test
    public void toStep_should_return_the_assertion_index() {
        // GIVEN
        NewmanScenario newmanScenario = new NewmanScenario()
                .withExecution(new Execution()
                        .withAssertions(new Assertion[] {
                                new Assertion("name0"),
                                new Assertion("name1"),
                                new Assertion("name2"),
                                new Assertion("name3")
                        }));

        // WHEN
        final String step = cut.toStep(newmanScenario, 2); // Avoid testing index 0 or 1, as they are values easily answered by regressions

        // THEN
        assertThat(step).isEqualTo("name2");
    }

    @Test
    public void toStep_should_return_the_assertion_index_even_at_index_0() { // 0 is first index (do not confound with request line -1)
        // GIVEN
        NewmanScenario newmanScenario = new NewmanScenario()
                .withExecution(new Execution()
                        .withAssertions(new Assertion[] {
                                new Assertion("name0"),
                                new Assertion("name1")
                        }));

        // WHEN
        final String step = cut.toStep(newmanScenario, 0); // Avoid testing index 0 or 1, as they are values easily answered by regressions

        // THEN
        assertThat(step).isEqualTo("name0");
    }

    @Test
    public void toStep_should_return_request_step_when_requesting_an_index_with_null_assertions() {
        // GIVEN
        final Request request = new Request();
        final NewmanScenario newmanScenario = new NewmanScenario()
                .withItem(new Item()
                        .withRequest(request))
                .withExecution(new Execution()
                        .withAssertions(null));
        doReturn("request_step").when(cut).buildRequestStep(eq(request));

        // WHEN
        final String step = cut.toStep(newmanScenario, 1);

        // THEN
        assertThat(step).isEqualTo("request_step");
    }

    @Test
    public void toStep_should_return_request_step_when_requesting_and_index_not_in_assertions() {
        // GIVEN
        final Request request = new Request();
        final NewmanScenario newmanScenario = new NewmanScenario()
                .withItem(new Item()
                        .withRequest(request))
                .withExecution(new Execution()
                        .withAssertions(new Assertion[0]));
        doReturn("request_step").when(cut).buildRequestStep(eq(request));

        // WHEN
        final String step = cut.toStep(newmanScenario, 0);

        // THEN
        assertThat(step).isEqualTo("request_step");
    }

    @Test
    public void toStep_should_return_request_step_when_requesting_request_line() {
        // GIVEN
        final Request request = new Request();
        final NewmanScenario newmanScenario = new NewmanScenario()
                .withItem(new Item()
                        .withRequest(request))
                .withExecution(new Execution()
                        .withAssertions(new Assertion[] {
                                new Assertion()
                        }));
        doReturn("request_step").when(cut).buildRequestStep(eq(request));

        // WHEN
        final String step = cut.toStep(newmanScenario, -1);

        // THEN
        assertThat(step).isEqualTo("request_step");
    }

    @Test
    public void toErrorLine_should_return_minus_100000_for_pre_request_script() {
        // GIVEN
        Failure failure = new Failure().withAt("prerequest-script");

        // WHEN
        final int line = cut.toErrorLine(failure);

        // THEN
        assertThat(line).isEqualTo(-100000);
    }

    @Test
    public void toErrorLine_should_return_100000_for_test_script() {
        // GIVEN
        Failure failure = new Failure().withAt("test-script");

        // WHEN
        final int line = cut.toErrorLine(failure);

        // THEN
        assertThat(line).isEqualTo(100000);
    }

    @Test
    public void toErrorLine_should_return_request_line_for_null_error_index() {
        // GIVEN
        Failure failure = new Failure().withError(new com.decathlon.ara.postman.bean.Error());

        // WHEN
        final int line = cut.toErrorLine(failure);

        // THEN
        assertThat(line).isEqualTo(-1);
    }

    @Test
    public void toErrorLine_should_return_error_index_when_present() {
        // GIVEN
        Failure failure = new Failure().withError(new com.decathlon.ara.postman.bean.Error().withIndex(Integer.valueOf(5)));

        // WHEN
        final int line = cut.toErrorLine(failure);

        // THEN
        assertThat(line).isEqualTo(5);
    }

    @Test
    public void buildRequestStep_should_return_method_and_url() {
        // GIVEN
        Url url = new Url();
        Request request = new Request()
                .withMethod("METHOD")
                .withUrl(url);
        doReturn("URL").when(cut).toUrlString(same(url));

        // WHEN
        final String step = cut.buildRequestStep(request);

        // THEN
        assertThat(step).isEqualTo("METHOD URL");
    }

    @Test
    public void toUrlString_should_append_all_url_fields() {
        // GIVEN
        final KeyValue[] query = new KeyValue[0];
        Url url = new Url()
                .withProtocol("protocol")
                .withHost(new String[] { "the", "real", "host" })
                .withPort("port")
                .withPath(new String[] { "the", "real", "path" })
                .withQuery(query);
        doReturn("?query=string").when(cut).toQueryString(same(query));

        // WHEN
        final String urlString = cut.toUrlString(url);

        // THEN
        assertThat(urlString).isEqualTo("protocol://the.real.host:port/the/real/path?query=string");
    }

    @Test
    public void toUrlString_should_not_append_missing_optional_fields() {
        // GIVEN
        Url url = new Url()
                .withProtocol("protocol")
                .withHost(new String[] { "host" })
                .withPort(null) // Postman does not put the port if not filled
                .withPath(NO_PARENT); // Trying empty value: postman sometimes use this
        doReturn("").when(cut).toQueryString(any());

        // WHEN
        final String urlString = cut.toUrlString(url);

        // THEN
        assertThat(urlString).isEqualTo("protocol://host/");
    }

    @Test
    public void toUrlString_should_not_crash_when_no_url() {
        // GIVEN
        Url url = new Url(); // When user provides no URL, all fields are null, except "query": it's an empty array
        doReturn("").when(cut).toQueryString(any());

        // WHEN
        final String urlString = cut.toUrlString(url);

        // THEN
        assertThat(urlString).isEqualTo("");
    }

    @Test
    public void toUrlString_should_return_host_when_only_host() {
        // GIVEN
        Url url = new Url()
                .withHost(new String[] { "host" });
        doReturn("").when(cut).toQueryString(any());

        // WHEN
        final String urlString = cut.toUrlString(url);

        // THEN
        assertThat(urlString).isEqualTo("host");
    }

    @Test
    public void toUrlString_should_not_append_port_if_empty() {
        // GIVEN
        Url url = new Url()
                .withProtocol("protocol")
                .withHost(new String[] { "host" })
                .withPort("")
                .withPath(new String[] { "path" });
        doReturn("").when(cut).toQueryString(any());

        // WHEN
        final String urlString = cut.toUrlString(url);

        // THEN
        assertThat(urlString).isEqualTo("protocol://host/path");
    }

    @Test
    public void toUrlString_should_not_append_port_if_80() {
        // GIVEN
        Url url = new Url()
                .withProtocol("protocol")
                .withHost(new String[] { "host" })
                .withPort("80")
                .withPath(new String[] { "path" });
        doReturn("").when(cut).toQueryString(any());

        // WHEN
        final String urlString = cut.toUrlString(url);

        // THEN
        assertThat(urlString).isEqualTo("protocol://host/path");
    }

    @Test
    public void toQueryString_should_return_empty_string_on_null_query() {
        // WHEN
        final String queryString = cut.toQueryString(null);

        // THEN
        assertThat(queryString).isEmpty();
    }

    @Test
    public void toQueryString_should_return_empty_string_on_empty_query() {
        // GIVEN
        KeyValue[] query = new KeyValue[0];

        // WHEN
        final String queryString = cut.toQueryString(query);

        // THEN
        assertThat(queryString).isEmpty();
    }

    @Test
    public void toQueryString_should_concatenate_keys_and_values_as_is_because_postman_already_percent_encode_them() {
        // GIVEN
        KeyValue[] query = new KeyValue[] {
                new KeyValue().withKey("key1").withValue("value1=%26encoded"),
                new KeyValue().withKey("key2").withValue("value2"),
                new KeyValue().withKey("key3").withValue("value3")
        };

        // WHEN
        final String queryString = cut.toQueryString(query);

        // THEN
        assertThat(queryString).isEqualTo("?key1=value1=%26encoded&key2=value2&key3=value3");
    }

    @Test
    public void postProcess_should_work() {
        // GIVEN
        com.decathlon.ara.domain.Source source = new com.decathlon.ara.domain.Source()
                .withPostmanCountryRootFolders(true);

        Run run = new Run()
                .withType(new Type().withSource(source))
                .withJobUrl("run-url/");

        final Item[] items = new Item[0];
        final List<Execution> executions = Collections.emptyList();
        final List<Failure> failures = Collections.emptyList();
        NewmanParsingResult result = new NewmanParsingResult()
                .withCollection(new Collection()
                        .withItem(items)
                        .withInfo(new Info()
                                .withName("collection-name")))
                .withExecutions(executions)
                .withFailures(failures);

        final NewmanScenario newmanScenarioWithoutExecution = new NewmanScenario();
        Stream stream = new Stream();
        List<NewmanScenario> newmanScenarios = new ArrayList<>();
        newmanScenarios.add(newmanScenarioWithoutExecution);

        final Response response1 = new Response()
                .withStream(stream);
        final NewmanScenario newmanScenarioWithExecution = new NewmanScenario()
                .withExecution(new Execution()
                        .withResponse(response1))
                .withScenario(new ExecutedScenario());
        newmanScenarios.add(newmanScenarioWithExecution);

        final Response response2 = new Response()
                .withStream(stream);
        final NewmanScenario newmanScenarioWithExecutionAndFailure = new NewmanScenario()
                .withExecution(new Execution()
                        .withResponse(response2))
                .withScenario(new ExecutedScenario());
        newmanScenarioWithExecutionAndFailure.getFailures().add(new Failure());
        newmanScenarios.add(newmanScenarioWithExecutionAndFailure);

        String newmanReportPath = "report";
        AtomicInteger requestPosition = new AtomicInteger(-42);

        doReturn(newmanScenarios).when(cut).toScenarios(same(items), same(requestPosition), eq(""));
        doNothing().when(cut).mapExecutionsToScenarios(same(executions), same(newmanScenarios));
        doNothing().when(cut).mapFailuresToScenarios(same(failures), same(newmanScenarios));
        doNothing().when(cut).buildScenarioContents(same(newmanScenarios));
        doNothing().when(cut).buildScenarioErrors(same(newmanScenarios));
        doNothing().when(cut).deleteTempFile(same(response1));
        doReturn("collection-file-name").when(cut).toCollectionFileName(source, newmanReportPath);
        doReturn("http-log-url-1").when(cut).uploadHttpLog(same(newmanScenarioWithExecution));
        doReturn("http-log-url-2").when(cut).uploadHttpLog(same(newmanScenarioWithExecutionAndFailure));
        doReturn(Optional.of(Instant.EPOCH)).when(cut).getStartDateTime(eq(response1));

        // WHEN
        final List<ExecutedScenario> executedScenarios = cut.postProcess(run, result, newmanReportPath, requestPosition);

        // THEN
        verify(cut, times(1)).mapExecutionsToScenarios(same(executions), same(newmanScenarios));
        verify(cut, times(1)).mapFailuresToScenarios(same(failures), same(newmanScenarios));
        verify(cut, times(1)).buildScenarioContents(same(newmanScenarios));
        verify(cut, times(1)).buildScenarioErrors(same(newmanScenarios));
        verify(cut, times(1)).deleteTempFile(same(response1));
        verify(cut, never()).deleteTempFile(same(response2));

        assertThat(executedScenarios).hasSize(2);
        assertThat(executedScenarios.get(0).getFeatureFile()).isEqualTo("collection-file-name");
        assertThat(executedScenarios.get(0).getFeatureName()).isEqualTo("collection-name");
        assertThat(executedScenarios.get(0).getHttpRequestsUrl()).isEqualTo("http-log-url-1");
        assertThat(executedScenarios.get(0).getCucumberReportUrl()).isEqualTo("run-url/Postman_Collection_Results/");
        assertThat(executedScenarios.get(0).getStartDateTime()).isEqualTo(Date.from(Instant.EPOCH));
    }

    @Test
    public void deleteTempFile_should_not_crash_if_response_is_null() {
        // WHEN
        cut.deleteTempFile(null);
    }

    @Test
    public void deleteTempFile_should_not_crash_if_response_stream_is_null() {
        // WHEN
        cut.deleteTempFile(new Response());
    }

    @Test
    public void deleteTempFile_should_not_crash_if_tempFile_is_null() {
        // WHEN
        cut.deleteTempFile(new Response().withStream(new Stream()));
    }

    @Test
    public void deleteTempFile_should_delete_the_temporary_file_and_set_it_to_null() throws IOException {
        // GIVEN
        File tempFile = File.createTempFile("ara_temp_unit_test_file_", ".bin");
        tempFile.deleteOnExit();
        Response response = new Response().withStream(new Stream().withTempFile(tempFile));

        try {
            // WHEN
            cut.deleteTempFile(response);

            // THEN
            assertThat(tempFile.exists()).isFalse();
            assertThat(response.getStream().getTempFile()).isNull();
        } finally {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    @Test
    public void deleteTempFiles_should_so_nothing_on_null_executions() {
        // GIVEN
        NewmanParsingResult newmanParsingResult = new NewmanParsingResult();

        // WHEN
        cut.deleteTempFiles(newmanParsingResult);

        // THEN
        verify(cut, never()).deleteTempFile(any());
    }

    @Test
    public void deleteTempFiles_should_delete_all_files() {
        // GIVEN
        final Response response1 = new Response();
        final Response response2 = new Response();
        NewmanParsingResult newmanParsingResult = new NewmanParsingResult()
                .withExecutions(Arrays.asList(
                        new Execution().withResponse(response1),
                        new Execution().withResponse(response2)));

        ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);
        doNothing().when(cut).deleteTempFile(argument.capture());

        // WHEN
        cut.deleteTempFiles(newmanParsingResult);

        // THEN
        assertThat(argument.getAllValues()).containsExactly(response1, response2);
    }

    @Test
    public void uploadHttpLog_should_do_nothing_and_return_null_on_empty_failures() {
        // GIVEN
        NewmanScenario newmanScenario = new NewmanScenario(); // Empty failures by default

        // WHEN
        final String url = cut.uploadHttpLog(newmanScenario);

        // THEN
        assertThat(url).isNull();
        verify(cut, never()).generateHttpLogHtml(any());
        verify(cut, never()).deleteTempFile(any());
    }

    @Test
    public void uploadHttpLog_should_upload_generated_html_and_return_uploaded_url_when_there_are_failures() {
        // GIVEN
        NewmanScenario newmanScenario = new NewmanScenario()
                .withExecution(new Execution()
                        .withResponse(new Response()));
        newmanScenario.getFailures().add(new Failure());
        doReturn("html").when(cut).generateHttpLogHtml(same(newmanScenario));
        when(assetService.saveHttpLogs("html")).thenReturn("url");
        doNothing().when(cut).deleteTempFile(any());

        // WHEN
        final String url = cut.uploadHttpLog(newmanScenario);

        // THEN
        assertThat(url).isEqualTo("url");
    }

    @Test
    public void uploadHttpLog_should_delete_temp_file_after_upload() {
        // GIVEN
        final Response response = new Response();
        NewmanScenario newmanScenario = new NewmanScenario()
                .withExecution(new Execution()
                        .withResponse(response));
        newmanScenario.getFailures().add(new Failure());
        doReturn("any").when(cut).generateHttpLogHtml(any());
        when(assetService.saveHttpLogs(any())).thenReturn("any");
        doNothing().when(cut).deleteTempFile(any());

        // WHEN
        cut.uploadHttpLog(newmanScenario);

        // THEN
        verify(cut, times(1)).deleteTempFile(same(response));
    }

    @Test
    public void generateHttpLogHtml_should_return_the_generated_html() {
        // GIVEN
        KeyValue[] requestHeaders = new KeyValue[0];
        KeyValue[] responseHeaders = new KeyValue[0];
        final Body requestBody = new Body();
        final Stream responseStream = new Stream();
        Request request = new Request()
                .withHeader(requestHeaders)
                .withBody(requestBody);
        Response response = new Response()
                .withCode(200)
                .withStatus("<status>")
                .withHeader(responseHeaders)
                .withStream(responseStream);
        NewmanScenario newmanScenario = new NewmanScenario()
                .withExecution(new Execution()
                        .withRequest(request)
                        .withResponse(response));
        doReturn("<request step>").when(cut).buildRequestStep(same(request));
        doAnswer(invocation -> {
            StringBuilder builder = (StringBuilder) invocation.getArguments()[0];
            builder.append("{REQUEST_HEADERS}\n");
            return null;
        }).when(cut).appendKeyValues(any(StringBuilder.class), same(requestHeaders));
        doAnswer(invocation -> {
            StringBuilder builder = (StringBuilder) invocation.getArguments()[0];
            builder.append("{REQUEST_BODY}\n");
            return null;
        }).when(cut).appendRequestBody(any(StringBuilder.class), same(requestBody));
        doAnswer(invocation -> {
            StringBuilder builder = (StringBuilder) invocation.getArguments()[0];
            builder.append("{RESPONSE_HEADERS}\n");
            return null;
        }).when(cut).appendKeyValues(any(StringBuilder.class), same(responseHeaders));
        doReturn("type").when(cut).getContentType(same(responseHeaders));
        doAnswer(invocation -> {
            StringBuilder builder = (StringBuilder) invocation.getArguments()[0];
            builder.append("{RESPONSE_BODY}\n");
            return null;
        }).when(cut).appendResponseBody(any(StringBuilder.class), same(responseStream), eq("type"));

        // WHEN
        final String html = cut.generateHttpLogHtml(newmanScenario);

        // THEN
        assertThat(html.replaceAll("<style>\n[^<]+\n</style>", "<style>\nwhatever\n</style>")).isEqualTo("" +
                "<style>\n" +
                "whatever\n" +
                "</style>\n" +
                "<h2>Request</h2>\n" +
                "<div class=\"card\">\n" +
                "<p>&lt;request step&gt;</p>\n" +
                "<div class=\"content\">\n" +
                "{REQUEST_HEADERS}\n" +
                "{REQUEST_BODY}\n" +
                "</div>\n" +
                "</div>\n" +
                "<h2>Response</h2>\n" +
                "<div class=\"card\">\n" +
                "<p>200 &lt;status&gt;</p>\n" +
                "<div class=\"content\">\n" +
                "{RESPONSE_HEADERS}\n" +
                "{RESPONSE_BODY}\n" +
                "</div>\n" +
                "</div>\n");
    }

    @Test
    public void generateHttpLogHtml_should_return_the_generated_html_with_an_indication_when_null_response() {
        // GIVEN
        KeyValue[] requestHeaders = new KeyValue[0];
        final Body requestBody = new Body();
        Request request = new Request()
                .withHeader(requestHeaders)
                .withBody(requestBody);
        NewmanScenario newmanScenario = new NewmanScenario()
                .withExecution(new Execution()
                        .withRequest(request)
                        .withResponse(null));
        doReturn("<request step>").when(cut).buildRequestStep(same(request));
        doAnswer(invocation -> {
            StringBuilder builder = (StringBuilder) invocation.getArguments()[0];
            builder.append("{REQUEST_HEADERS}\n");
            return null;
        }).when(cut).appendKeyValues(any(StringBuilder.class), same(requestHeaders));
        doAnswer(invocation -> {
            StringBuilder builder = (StringBuilder) invocation.getArguments()[0];
            builder.append("{REQUEST_BODY}\n");
            return null;
        }).when(cut).appendRequestBody(any(StringBuilder.class), same(requestBody));

        // WHEN
        final String html = cut.generateHttpLogHtml(newmanScenario);

        // THEN
        assertThat(html.replaceAll("<style>\n[^<]+\n</style>", "<style>\nwhatever\n</style>")).isEqualTo("" +
                "<style>\n" +
                "whatever\n" +
                "</style>\n" +
                "<h2>Request</h2>\n" +
                "<div class=\"card\">\n" +
                "<p>&lt;request step&gt;</p>\n" +
                "<div class=\"content\">\n" +
                "{REQUEST_HEADERS}\n" +
                "{REQUEST_BODY}\n" +
                "</div>\n" +
                "</div>\n" +
                "<h2>Response</h2>\n" +
                "<div class=\"card\">\n" +
                "<p>Request Not Executed</p>\n" +
                "<div class=\"content\">\n" +
                "</div>\n" +
                "</div>\n");
    }

    @Test
    public void getContentType_should_return_null_when_not_found() {
        // GIVEN
        KeyValue[] headers = new KeyValue[] {
                new KeyValue().withKey("key").withValue("value")
        };

        // WHEN
        final String contentType = cut.getContentType(headers);

        // THEN
        assertThat(contentType).isNull();
    }

    @Test
    public void getContentType_should_return_the_content_type() {
        // GIVEN
        KeyValue[] headers = new KeyValue[] {
                new KeyValue().withKey("key1").withValue("value1"),
                new KeyValue().withKey("Content-Type").withValue("the-content-type"),
                new KeyValue().withKey("key3").withValue("value3")
        };

        // WHEN
        final String contentType = cut.getContentType(headers);

        // THEN
        assertThat(contentType).isEqualTo("the-content-type");
    }

    @Test
    public void appendKeyValues_should_append_nothing_on_null_keyValues() {
        // GIVEN
        StringBuilder html = new StringBuilder();

        // WHEN
        cut.appendKeyValues(html, null);

        // THEN
        assertThat(html).isEmpty();
    }

    @Test
    public void appendKeyValues_should_append_nothing_on_empty_keyValues() {
        // GIVEN
        StringBuilder html = new StringBuilder();

        // WHEN
        cut.appendKeyValues(html, new KeyValue[0]);

        // THEN
        assertThat(html).isEmpty();
    }

    @Test
    public void appendKeyValues_should_append_escaped_keyValues() {
        // GIVEN
        StringBuilder html = new StringBuilder();
        KeyValue[] keyValues = new KeyValue[] {
                new KeyValue().withKey("key1 with <html>").withValue("value1"),
                new KeyValue().withKey("key2").withValue("value2 with <html>")
        };

        // WHEN
        cut.appendKeyValues(html, keyValues);

        // THEN
        assertThat(html.toString()).isEqualTo("<table>\n" +
                "<tr><th>key1 with &lt;html&gt;:</th><td><pre>value1</pre></td></tr>\n" +
                "<tr><th>key2:</th><td><pre>value2 with &lt;html&gt;</pre></td></tr>\n" +
                "</table>\n");
    }

    @Test
    public void appendRequestBody_should_do_nothing_on_null_body() {
        // GIVEN
        StringBuilder html = new StringBuilder();

        // WHEN
        cut.appendRequestBody(html, null);

        // THEN
        assertThat(html.toString()).isEmpty();
    }

    @Test
    public void appendRequestBody_should_do_nothing_on_null_mode() {
        // GIVEN
        StringBuilder html = new StringBuilder();
        Body body = new Body();

        // WHEN
        cut.appendRequestBody(html, body);

        // THEN
        assertThat(html.toString()).isEmpty();
    }

    @Test
    public void appendRequestBody_should_append_form_data() {
        // GIVEN
        StringBuilder html = new StringBuilder();
        final KeyValue[] formData = new KeyValue[0];
        Body body = new Body().withMode("formdata").withFormData(formData);
        doAnswer(invocation -> {
            StringBuilder builder = (StringBuilder) invocation.getArguments()[0];
            builder.append("prettyFormData");
            return null;
        }).when(cut).appendKeyValues(same(html), same(formData));

        // WHEN
        cut.appendRequestBody(html, body);

        // THEN
        assertThat(html.toString()).isEqualTo("prettyFormData");
    }

    @Test
    public void appendRequestBody_should_append_url_encoded() {
        // GIVEN
        StringBuilder html = new StringBuilder();
        final KeyValue[] urlEncoded = new KeyValue[0];
        Body body = new Body().withMode("urlencoded").withUrlEncoded(urlEncoded);
        doAnswer(invocation -> {
            StringBuilder builder = (StringBuilder) invocation.getArguments()[0];
            builder.append("prettyUrlEncoded");
            return null;
        }).when(cut).appendKeyValues(same(html), same(urlEncoded));

        // WHEN
        cut.appendRequestBody(html, body);

        // THEN
        assertThat(html.toString()).isEqualTo("prettyUrlEncoded");
    }

    @Test
    public void appendRequestBody_should_append_pretty_raw() {
        // GIVEN
        StringBuilder html = new StringBuilder();
        Body body = new Body().withMode("raw").withRaw("<html>");

        // WHEN
        cut.appendRequestBody(html, body);

        // THEN
        assertThat(html.toString()).isEqualTo("<pre>&lt;html&gt;</pre>\n");
    }

    @Test
    public void appendRequestBody_should_append_error_for_unsupported_file() {
        // GIVEN
        StringBuilder html = new StringBuilder();
        Body body = new Body().withMode("file");

        // WHEN
        cut.appendRequestBody(html, body);

        // THEN
        assertThat(html.toString()).isEqualTo("<p style=\"background-color: red; color: white;\">" +
                "Postman does not export the files to upload, so it has not been sent...</p>\n");
    }

    @Test
    public void appendRequestBody_should_append_error_for_unknown_mode() {
        // GIVEN
        StringBuilder html = new StringBuilder();
        Body body = new Body().withMode("unknown");

        // WHEN
        cut.appendRequestBody(html, body);

        // THEN
        assertThat(html.toString()).isEqualTo("<p style=\"background-color: red; color: white;\">" +
                "Used new (unknown to ARA) request body mode unknown</p>\n");
    }

    @Test
    public void appendResponseBody_should_append_nothing_when_null_stream_file() {
        // GIVEN
        StringBuilder html = new StringBuilder();

        // WHEN
        cut.appendResponseBody(html, new Stream(), "any");

        // THEN
        assertThat(html.toString()).isEmpty();
    }

    @Test
    public void appendResponseBody_should_append_pretty_file_content() throws IOException {
        // GIVEN
        File tempFile = File.createTempFile("ara_temp_unit_test_file_", ".bin");
        tempFile.deleteOnExit();

        try {
            StringBuilder html = new StringBuilder();
            FileUtils.writeStringToFile(tempFile, "content", StandardCharsets.UTF_8);
            Stream stream = new Stream().withTempFile(tempFile);
            doReturn("<p>pretty</p>").when(cut).prettyPrint(eq("content"), eq("application/json"));

            // WHEN
            cut.appendResponseBody(html, stream, "application/json");

            // THEN
            assertThat(html.toString()).isEqualTo("<pre>&lt;p&gt;pretty&lt;/p&gt;</pre>\n");
        } finally {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    @Test
    public void appendResponseBody_should_append_error_message_when_file_read_error() {
        // GIVEN
        StringBuilder html = new StringBuilder();
        Stream stream = new Stream().withTempFile(new File("nonexistent"));

        // WHEN
        cut.appendResponseBody(html, stream, "any");

        // THEN
        assertThat(html.toString()).startsWith("<pre>Error in ARA while reading the content of the response received by Newman:\n" +
                "java.io.FileNotFoundException: File");
        assertThat(html.toString()).endsWith("</pre>\n");
    }

    @Test
    public void prettyPrint_should_indent_application_json() {
        assertThat(cut.prettyPrint(JSON_RAW, "application/json")).isEqualTo(JSON_INDENTED);
    }

    @Test
    public void prettyPrint_should_indent_application_json_with_charset() {
        assertThat(cut.prettyPrint(JSON_RAW, "application/json; charset=iso-8859-1")).isEqualTo(JSON_INDENTED);
    }

    @Test
    public void prettyPrint_should_not_indent_other_mime_types() {
        assertThat(cut.prettyPrint(JSON_RAW, "other/mime-type")).isEqualTo(JSON_RAW);
    }

    @Test
    public void prettyPrint_should_not_indent_malformed_json() {
        assertThat(cut.prettyPrint("{malformed", "application/json")).isEqualTo("{malformed");
    }

    @Test
    public void testEscapeHtml() {
        assertThat(cut.escapeHtml("<p class=\"any\">&amp; foo'bar")).isEqualTo("&lt;p class=&quot;any&quot;&gt;&amp;amp; foo&apos;bar");
    }

    @Test
    public void testGetStartDateTime_should_return_the_parsed_date_for_utc() {
        // GIVEN
        final KeyValue[] headers = {
                new KeyValue("Some", "value"),
                new KeyValue("Date", "Wed, 21 Oct 2015 07:28:01 GMT"),
                new KeyValue("Other", "value")
        };
        Calendar expected = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expected.set(2015, Calendar.OCTOBER, 21, 7, 28, 1);
        expected.set(Calendar.MILLISECOND, 0);

        // WHEN
        final Optional<Instant> startDateTime = cut.getStartDateTime(new Response().withHeader(headers));

        // THEN
        assertThat(startDateTime.orElse(null)).isEqualTo(expected.toInstant());
    }

    @Test
    public void testGetStartDateTime_should_return_the_parsed_date_for_other_time_zone() {
        // GIVEN
        final KeyValue[] headers = {
                new KeyValue("Some", "value"),
                new KeyValue("Date", "Wed, 21 Oct 2015 07:28:01 -0500"), // CDT
                new KeyValue("Other", "value")
        };
        Calendar expected = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expected.set(2015, Calendar.OCTOBER, 21, 7 + 5, 28, 1);
        expected.set(Calendar.MILLISECOND, 0);

        // WHEN
        final Optional<Instant> startDateTime = cut.getStartDateTime(new Response().withHeader(headers));

        // THEN
        assertThat(startDateTime.orElse(null)).isEqualTo(expected.toInstant());
    }

    @Test
    public void testGetStartDateTime_should_return_empty_if_null_response() {
        // GIVEN
        final Response response = null;

        // WHEN
        final Optional<Instant> startDateTime = cut.getStartDateTime(response);

        // THEN
        assertThat(startDateTime).isEmpty();
    }

    @Test
    public void testGetStartDateTime_should_return_empty_if_none() {
        // GIVEN
        final KeyValue[] headers = { new KeyValue("No", "date") };

        // WHEN
        final Optional<Instant> startDateTime = cut.getStartDateTime(new Response().withHeader(headers));

        // THEN
        assertThat(startDateTime).isEmpty();
    }

    @Test
    public void testGetStartDateTime_should_return_empty_if_not_able_to_parse() {
        // GIVEN
        final KeyValue[] headers = { new KeyValue("Date", "not able to parse") };

        // WHEN
        final Optional<Instant> startDateTime = cut.getStartDateTime(new Response().withHeader(headers));

        // THEN
        assertThat(startDateTime).isEmpty();
    }

    @Test
    public void toCollectionFileName_ShouldRemoveCountryCodes_WhenSourceRequiresIt() {
        com.decathlon.ara.domain.Source sourceWithCountries = new com.decathlon.ara.domain.Source()
                .withPostmanCountryRootFolders(true);

        // With no countries
        assertThat(cut.toCollectionFileName(sourceWithCountries, "file.json")).isEqualTo("file.json");

        // With one country
        assertThat(cut.toCollectionFileName(sourceWithCountries, "file_all.json")).isEqualTo("file.json");
        assertThat(cut.toCollectionFileName(sourceWithCountries, "file_cn.json")).isEqualTo("file.json");

        // With several countries
        assertThat(cut.toCollectionFileName(sourceWithCountries, "file_be+cn.json")).isEqualTo("file.json");
        assertThat(cut.toCollectionFileName(sourceWithCountries, "file_all+be+cn.json")).isEqualTo("file.json");

        // With something resembling a country suffix, but not one
        assertThat(cut.toCollectionFileName(sourceWithCountries, "file_a.json")).isEqualTo("file_a.json");
        assertThat(cut.toCollectionFileName(sourceWithCountries, "file_alli.json")).isEqualTo("file_alli.json");
        assertThat(cut.toCollectionFileName(sourceWithCountries, "file_AA.json")).isEqualTo("file_AA.json");
        assertThat(cut.toCollectionFileName(sourceWithCountries, "file_a1.json")).isEqualTo("file_a1.json");
        assertThat(cut.toCollectionFileName(sourceWithCountries, "file_AA+bb.json")).isEqualTo("file_AA+bb.json");

        // Keep the postman_collection suffix in the feature_file (not in the feature_name, so make sure it will stay like that)
        assertThat(cut.toCollectionFileName(sourceWithCountries, "file.postman_collection.json")).isEqualTo("file.postman_collection.json");
        assertThat(cut.toCollectionFileName(sourceWithCountries, "file.postman_collection_all.json")).isEqualTo("file.postman_collection.json");
    }

    @Test
    public void toCollectionFileName_ShouldNotRemoveCountryCodes_WhenSourceDoesNotRequireIt() {
        com.decathlon.ara.domain.Source sourceWithoutCountries = new com.decathlon.ara.domain.Source()
                .withPostmanCountryRootFolders(false);

        // With no countries
        assertThat(cut.toCollectionFileName(sourceWithoutCountries, "file.json")).isEqualTo("file.json");

        // With one country
        assertThat(cut.toCollectionFileName(sourceWithoutCountries, "file_all.json")).isEqualTo("file_all.json");
        assertThat(cut.toCollectionFileName(sourceWithoutCountries, "file_cn.json")).isEqualTo("file_cn.json");

        // With several countries
        assertThat(cut.toCollectionFileName(sourceWithoutCountries, "file_be+cn.json")).isEqualTo("file_be+cn.json");
        assertThat(cut.toCollectionFileName(sourceWithoutCountries, "file_all+be+cn.json")).isEqualTo("file_all+be+cn.json");

        // Keep the postman_collection suffix in the feature_file (not in the feature_name, so make sure it will stay like that)
        assertThat(cut.toCollectionFileName(sourceWithoutCountries, "file.postman_collection.json")).isEqualTo("file.postman_collection.json");
    }

}
