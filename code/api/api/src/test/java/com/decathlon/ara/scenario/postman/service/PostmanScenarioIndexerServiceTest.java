/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

package com.decathlon.ara.scenario.postman.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.domain.Source;
import com.decathlon.ara.scenario.postman.bean.Assertion;
import com.decathlon.ara.scenario.postman.bean.CollectionWithScripts;
import com.decathlon.ara.scenario.postman.bean.Event;
import com.decathlon.ara.scenario.postman.bean.Info;
import com.decathlon.ara.scenario.postman.bean.ItemWithScripts;
import com.decathlon.ara.scenario.postman.bean.Listen;
import com.decathlon.ara.scenario.postman.bean.Request;
import com.decathlon.ara.scenario.postman.bean.Script;
import com.decathlon.ara.util.TestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class PostmanScenarioIndexerServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PostmanService postmanService;

    @Spy
    @InjectMocks
    private PostmanScenarioIndexerService cut;

    @Test
    void collectCollectionScenarios_should_call_collectItemScenarios_with_right_parameters_and_return_its_return_value() {
        // GIVEN
        final ItemWithScripts[] items = new ItemWithScripts[0];
        CollectionWithScripts collection = collectionWithScripts(items, new Info());
        Source source = new Source();
        ArgumentCaptor<AtomicInteger> argumentRequestPosition = ArgumentCaptor.forClass(AtomicInteger.class);
        Scenario expectedScenario = new Scenario();
        List<Scenario> expectedScenarios = Collections.singletonList(expectedScenario);
        doReturn(expectedScenarios).when(cut).collectItemScenarios(same(items), same(source), argumentRequestPosition.capture(), eq(""), eq(Collections.emptyList()));

        // WHEN
        List<Scenario> scenarios = cut.collectCollectionScenarios(collection, source, null);

        // THEN
        assertThat(argumentRequestPosition.getValue().get()).isEqualTo(0);
        assertThat(scenarios).hasSize(1);
        assertThat(scenarios.get(0)).isSameAs(expectedScenario);
    }

    @Test
    void collectCollectionScenarios_should_set_featureFile_and_featureName_of_all_scenarios() {
        // GIVEN
        final ItemWithScripts[] items = new ItemWithScripts[0];
        Info info = new Info();
        TestUtil.setField(info, "name", "collectionName");
        CollectionWithScripts collection = collectionWithScripts(items, info);
        String jsonFilePath = "filePath";
        doReturn(Arrays.asList(new Scenario(), new Scenario()))
                .when(cut).collectItemScenarios(any(), any(), any(), any(), any());

        // WHEN
        List<Scenario> scenarios = cut.collectCollectionScenarios(collection, null, jsonFilePath);

        // THEN
        assertThat(scenarios.get(0).getFeatureFile()).isEqualTo("filePath");
        assertThat(scenarios.get(0).getFeatureName()).isEqualTo("collectionName");
        assertThat(scenarios.get(1).getFeatureFile()).isEqualTo("filePath");
        assertThat(scenarios.get(1).getFeatureName()).isEqualTo("collectionName");
    }

    @Test
    void collectItemScenarios_should_return_empty_list_if_items_is_null() {
        // WHEN
        List<Scenario> scenarios = cut.collectItemScenarios(null, null, null, null, null);

        // THEN
        assertThat(scenarios).isEmpty();
    }

    @Test
    void collectItemScenarios_should_recursively_call_and_correctly_set_request_name_and_severity() {
        // GIVEN
        final Request request = new Request();
        ItemWithScripts[] items = new ItemWithScripts[] { itemWithScripts("root", null, new ItemWithScripts[] {
                itemWithScripts("folder", null, new ItemWithScripts[] {
                        itemWithScripts("request", request, null, false, new Event[] { event(Listen.TEST, script("pm.test('Item assertion')")) })
                }, true, new Event[] { event(Listen.TEST, script("pm.test('Folder assertion')")) })
        }, false, null) };
        Source source = new Source();
        AtomicInteger requestPosition = new AtomicInteger(5);
        doReturn("severity").when(postmanService).getSeverity("root");
        doReturn("folder-severity").when(postmanService).getSeverity("root-without-severity", "folder");
        doReturn("").when(postmanService).getSeverity("root-without-severity", "folder-without-severity", "request");
        doReturn("root-without-severity").when(postmanService).removeSeverityTag("root");
        doReturn("folder-without-severity").when(postmanService).removeSeverityTag("folder");
        doReturn("request-without-severity").when(postmanService).removeSeverityTag("request");
        final Assertion[] expectedJoinedAssertions = {
                assertion("Item assertion"),
                assertion("Folder assertion")
        };
        doReturn("content").when(postmanService)
                .buildScenarioContent(eq(request), eq(null), argThat((assertions) -> {
                    if (assertions.length != expectedJoinedAssertions.length) {
                        return false;
                    }
                    for (int i = 0; i < assertions.length; i++) {
                        if (!assertions[i].getName().equals(expectedJoinedAssertions[i].getName())) {
                            return false;
                        }
                    }
                    return true;
                }), eq(null), eq(true));

        // WHEN
        List<Scenario> scenarios = cut.collectItemScenarios(items, source, requestPosition, null, Collections.emptyList());

        // THEN
        assertThat(scenarios).hasSize(1);
        assertThat(scenarios.get(0).getSource()).isSameAs(source);
        assertThat(scenarios.get(0).getSeverity()).isEqualTo("folder-severity");
        assertThat(scenarios.get(0).getName())
                .isEqualTo("root-without-severity \u25b6 folder-without-severity \u25b6 request-without-severity");
        assertThat(scenarios.get(0).getLine()).isEqualTo(6);
        assertThat(scenarios.get(0).getContent()).isEqualTo("content");
    }

    @Test
    void toScenario_should_set_a_few_properties_of_scenario() {
        // GIVEN
        final Request request = new Request();
        ItemWithScripts item = itemWithScripts(null, request, null, false, null);
        ;
        Source source = source(true);
        AtomicInteger requestPosition = new AtomicInteger(3);
        String[] path = { "folder1", "folder2", "request" };
        doReturn("country-codes").when(cut).extractCountryCodes(same(path));
        final Assertion assertion = new Assertion();
        doReturn("content").when(postmanService)
                .buildScenarioContent(request, null, new Assertion[] { assertion }, null, true);

        // WHEN
        final Scenario scenario = cut.toScenario(item, source, requestPosition, "the-severity", Collections.singletonList(assertion), path);

        // THEN
        assertThat(scenario.getSource()).isSameAs(source);
        assertThat(scenario.getTags()).isEqualTo("@severity-the-severity");
        assertThat(scenario.getCountryCodes()).isEqualTo("country-codes");
        assertThat(scenario.getSeverity()).isEqualTo("the-severity");
        assertThat(scenario.getName()).isEqualTo("folder1 \u25b6 folder2 \u25b6 request");
        assertThat(scenario.getLine()).isEqualTo(4);
        assertThat(scenario.getContent()).isEqualTo("content");
    }

    @Test
    void toScenario_should_set_country_all_when_source_has_no_country_in_root_folders() {
        // GIVEN
        ItemWithScripts item = new ItemWithScripts();
        Source source = source(false);
        AtomicInteger requestPosition = new AtomicInteger();
        String[] path = { "any" };

        // WHEN
        final Scenario scenario = cut.toScenario(item, source, requestPosition, "the-severity", Collections.emptyList(), path);

        // THEN
        assertThat(scenario.getCountryCodes()).isEqualTo("all");
    }

    @Test
    void toScenario_should_set_null_tags_when_no_severity() {
        // GIVEN
        ItemWithScripts item = new ItemWithScripts();
        Source source = new Source();
        AtomicInteger requestPosition = new AtomicInteger();
        String[] path = new String[0];

        // WHEN
        final Scenario scenario = cut.toScenario(item, source, requestPosition, null, Collections.emptyList(), path);

        // THEN
        assertThat(scenario.getTags()).isNull();
    }

    @Test
    void extractAssertions_should_call_guessAssertions_on_first_test_event() {
        // GIVEN
        final String[] exec = { "pm.test('Must be extracted');" };
        ItemWithScripts item = itemWithScripts(null, null, null, false, new Event[] {
                event(Listen.PRE_REQUEST, script("Must not be extracted")),
                event(Listen.TEST, script(exec)),
                event(Listen.TEST, script("Only first test event is extracted"))
        });
        final List<Assertion> expectedAssertions = Collections.singletonList(new Assertion());
        doReturn(expectedAssertions).when(cut).guessAssertions(same(exec));

        // WHEN
        final List<Assertion> actualAssertions = cut.extractAssertions(item);

        // THEN
        verify(cut, times(1)).guessAssertions(any());
        assertThat(actualAssertions).isSameAs(expectedAssertions);
    }

    @Test
    void extractAssertions_should_return_empty_list_when_item_events_is_null() {
        // GIVEN
        ItemWithScripts item = new ItemWithScripts();

        // WHEN
        final List<Assertion> assertions = cut.extractAssertions(item);

        // THEN
        assertThat(assertions).isEmpty();
    }

    @Test
    void guessAssertions_should_recognize_pm_test_and_postman_test() {
        // GIVEN
        String[] javaScriptLines = new String[] {
                "const someVariable = pm.response.json();",
                "",
                "// Some comment",
                "pm.test(\"Assertion with pm 1\", function () {",
                "    pm.response.to.have.status(200);",
                "});",
                "",
                "postman.test(\"Assertion with postman 1\", function () {",
                "    pm.response.to.be.ok;",
                "});",
                "",
                "pm.test(\"Assertion with pm 2\", function () {",
                "     pm.response.to.be.json; ",
                "});",
                "",
                "postman.test(\"Assertion with postman 2\", function () {",
                "     pm.response.to.be.withBody; ",
                "});"
        };
        doAnswer(returnsFirstArg()).when(cut).removeComments(anyString());

        // WHEN
        List<Assertion> assertions = cut.guessAssertions(javaScriptLines);

        // THEN
        assertThat(assertions.stream().map(Assertion::getName)).containsExactly(
                "Assertion with pm 1",
                "Assertion with postman 1",
                "Assertion with pm 2",
                "Assertion with postman 2");
    }

    @Test
    void guessAssertions_should_call_removeComments_on_joined_lines_and_work_on_it() {
        // GIVEN
        String[] javaScriptLines = new String[] { "some", "lines" };
        doReturn("pm.test(\"Assertion\");").when(cut).removeComments("some\nlines");

        // WHEN
        List<Assertion> assertions = cut.guessAssertions(javaScriptLines);

        // THEN
        assertThat(assertions.stream().map(Assertion::getName)).containsExactly("Assertion");
    }

    @Test
    void guessAssertions_should_return_assertion_name_even_with_ending_double_quote() {
        // GIVEN
        doReturn("pm.test(\"Assertion \\\"escaped\");").when(cut).removeComments(anyString());

        // WHEN
        List<Assertion> assertions = cut.guessAssertions(new String[0]);

        // THEN
        assertThat(assertions.stream().map(Assertion::getName)).containsExactly("Assertion \"escaped");
    }

    @Test
    void guessAssertions_should_return_unescaped_assertion_name_even_with_string_ending_with_an_escaped_backslash() {
        // GIVEN
        // String content 'Escaped backslash\' looks like it ends with an escaped-double-quote,
        // but it is only ended by an escaped-backslash!
        doReturn("pm.test(\"Escaped backslash\\\\\");").when(cut).removeComments(anyString());

        // WHEN
        List<Assertion> assertions = cut.guessAssertions(new String[0]);

        // THEN
        assertThat(assertions.stream().map(Assertion::getName)).containsExactly("Escaped backslash\\");
    }

    @Test
    void guessAssertions_should_return_assertion_when_script_abruptly_ends_inside_assertion_name() {
        // GIVEN
        // Will not compile, but gracefully handle this case anyway: it can add useful debug information
        doReturn("pm.test(\"Non-terminated-string").when(cut).removeComments(anyString());

        // WHEN
        List<Assertion> assertions = cut.guessAssertions(new String[0]);

        // THEN
        assertThat(assertions.stream().map(Assertion::getName)).containsExactly("Non-terminated-string");
    }

    @Test
    void guessAssertions_should_detect_pm_test_call_with_spacing_characters_in_between() {
        // GIVEN
        // Will not compile, but gracefully handle this case anyway: it can add useful debug information
        doReturn("pm  \t.  test\r\n\t  (\"Call with spacings\")").when(cut).removeComments(anyString());

        // WHEN
        List<Assertion> assertions = cut.guessAssertions(new String[0]);

        // THEN
        assertThat(assertions.stream().map(Assertion::getName)).containsExactly("Call with spacings");
    }

    @Test
    void guessAssertions_should_return_handle_empty_assertion_name() {
        // GIVEN
        // Will not compile, but gracefully handle this case anyway: it can add useful debug information
        doReturn("pm.test(\"\");").when(cut).removeComments(anyString());

        // WHEN
        List<Assertion> assertions = cut.guessAssertions(new String[0]);

        // THEN
        assertThat(assertions.stream().map(Assertion::getName)).containsExactly("");
    }

    @Test
    void isEscapedCharacter_should_work() {
        assertThat(cut.isEscapedCharacter("'", 0)).isFalse();
        assertThat(cut.isEscapedCharacter("a'", 1)).isFalse();
        assertThat(cut.isEscapedCharacter("aa'", 2)).isFalse();
        assertThat(cut.isEscapedCharacter("a\\'", 2)).isTrue();
        assertThat(cut.isEscapedCharacter("aa\\'", 3)).isTrue();
        assertThat(cut.isEscapedCharacter("\\'", 1)).isTrue();
        assertThat(cut.isEscapedCharacter("\\\\'", 2)).isFalse();
        assertThat(cut.isEscapedCharacter("\\\\\\'", 3)).isTrue();
        assertThat(cut.isEscapedCharacter("\\\\\\\\'", 4)).isFalse();
        assertThat(cut.isEscapedCharacter("\\\\\\\\\\'", 5)).isTrue();
        assertThat(cut.isEscapedCharacter("\\\\\\\\\\\\'", 6)).isFalse();
    }

    @Test
    void removeComments_should_call_JavaScriptCommentRemover_removeComments() {
        // GIVEN
        String sourceCode = "" +
                "var a = // Comment\n" +
                "1 / /* Block /\n" +
                "comment //too */ 2; // line/*comment\r\n" +
                "var b = /* 42 */ 3;\n" +
                "var c = \" \\\" /* 42 */ // 3\";\n";

        // WHEN
        String sourceWithoutComments = cut.removeComments(sourceCode);

        // THEN
        // removeComments is a very simple method, useful to enclose the new operator into a comment-remover that can be
        // mocked, but the method itself cannot be mocked.
        // So we test quite a complicated example to be pretty-sure the real method has been called
        assertThat(sourceWithoutComments).isEqualTo("" +
                "var a = \n" +
                "1 /  2; \r\n" +
                "var b =  3;\n" +
                "var c = \" \\\" /* 42 */ // 3\";\n");
    }

    @Test
    void extractCountryCodes_should_return_all_when_root_folder_is_all() {
        assertThat(cut.extractCountryCodes(new String[] { "all" })).isEqualTo("all");
    }

    @Test
    void extractCountryCodes_should_return_all_when_no_folder_provided() {
        assertThat(cut.extractCountryCodes(null)).isEqualTo("all");
        assertThat(cut.extractCountryCodes(new String[] {})).isEqualTo("all");
        assertThat(cut.extractCountryCodes(new String[] { null })).isEqualTo("all");
        assertThat(cut.extractCountryCodes(new String[] { "" })).isEqualTo("all");
        assertThat(cut.extractCountryCodes(new String[] { "+" })).isEqualTo("all");
        assertThat(cut.extractCountryCodes(new String[] { "++" })).isEqualTo("all");
        assertThat(cut.extractCountryCodes(new String[] { "+  +" })).isEqualTo("all");
    }

    @Test
    void extractCountryCodes_should_return_country_codes_if_at_least_one_is_present() {
        assertThat(cut.extractCountryCodes(new String[] { "a" })).isEqualTo("a");
        assertThat(cut.extractCountryCodes(new String[] { "fr+us" })).isEqualTo("fr,us");
    }

    @Test
    void extractCountryCodes_should_return_sorted_country_codes() {
        // "edd" to mess up with String hash-code (sum of each character value multiplied by a constant)
        assertThat(cut.extractCountryCodes(new String[] { "f+a+c+edd+b+d" })).isEqualTo("a,b,c,d,edd,f");
    }

    @Test
    void extractCountryCodes_should_return_all_if_at_least_one_is_all() {
        assertThat(cut.extractCountryCodes(new String[] { "fr+all+us" })).isEqualTo("all");
        assertThat(cut.extractCountryCodes(new String[] { "us+all" })).isEqualTo("all");
    }

    @Test
    void extractCountryCodes_should_remove_empty_country_codes() {
        assertThat(cut.extractCountryCodes(new String[] { "fr++us+" })).isEqualTo("fr,us");
    }

    @Test
    void extractCountryCodes_should_ignore_sub_folders() {
        assertThat(cut.extractCountryCodes(new String[] { "fr+us", "fr+de" })).isEqualTo("fr,us");
    }

    @Test
    void extractCountryCodes_should_trim_codes() {
        assertThat(cut.extractCountryCodes(new String[] { " fr + us " })).isEqualTo("fr,us");
    }

    @Test
    void listJsonFilePaths_should_list_all_json_files_in_zip() throws URISyntaxException, IOException {
        // GIVEN
        final ClassLoader classLoader = cut.getClass().getClassLoader();
        final String zipName = "postman/files-in-folders.zip";
        final URL zipUrl = classLoader.getResource(zipName);
        if (zipUrl == null) {
            throw new IOException("Cannot find given ZIP file " + zipName);
        }
        // Also works on Windows: "file:/C:/.../target/...zip" => "C:\...\target\...zip"
        final Path zipPath = Paths.get(new File(zipUrl.toURI()).getPath());
        final FileSystem zip = FileSystems.newFileSystem(zipPath, classLoader);

        // WHEN
        final List<Path> paths = cut.listJsonFilePaths(zip);

        // THEN
        assertThat(paths.stream().map(Path::toString)).containsExactly( // Ordered by name ASC
                "/folder/sub-file1.json",
                "/folder/sub-file2.json",
                "/folder/sub-folder/sub-sub-file1.json",
                "/folder/sub-folder/sub-sub-file2.json",
                "/root-file1.json",
                "/root-file2.json");
    }

    private CollectionWithScripts collectionWithScripts(ItemWithScripts[] item, Info info) {
        CollectionWithScripts collectionWithScripts = new CollectionWithScripts();
        TestUtil.setField(collectionWithScripts, "item", item);
        TestUtil.setField(collectionWithScripts, "info", info);
        return collectionWithScripts;
    }

    private ItemWithScripts itemWithScripts(String name, Request request, ItemWithScripts[] children, boolean isSubFolder, Event[] events) {
        ItemWithScripts itemWithScripts = new ItemWithScripts();
        TestUtil.setField(itemWithScripts, "name", name);
        TestUtil.setField(itemWithScripts, "request", request);
        TestUtil.setField(itemWithScripts, "children", children);
        TestUtil.setField(itemWithScripts, "isSubFolder", isSubFolder);
        TestUtil.setField(itemWithScripts, "events", events);
        return itemWithScripts;
    }

    private Event event(Listen listen, Script script) {
        Event event = new Event();
        TestUtil.setField(event, "listen", listen);
        TestUtil.setField(event, "script", script);
        return event;

    }

    private Script script(String... exec) {
        Script script = new Script();
        TestUtil.setField(script, "exec", exec);
        return script;
    }

    private Source source(boolean postmanCountryRootFolders) {
        Source source = new Source();
        TestUtil.setField(source, "postmanCountryRootFolders", postmanCountryRootFolders);
        return source;
    }

    private Assertion assertion(String name) {
        Assertion assertion = new Assertion();
        TestUtil.setField(assertion, "name", name);
        return assertion;
    }

}
