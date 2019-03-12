package com.decathlon.ara.postman.service;

import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.domain.Source;
import com.decathlon.ara.postman.bean.Assertion;
import com.decathlon.ara.postman.bean.CollectionWithScripts;
import com.decathlon.ara.postman.bean.Event;
import com.decathlon.ara.postman.bean.ItemWithScripts;
import com.decathlon.ara.postman.bean.Listen;
import com.decathlon.ara.postman.util.JavaScriptCommentRemover;
import com.decathlon.ara.report.bean.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PostmanScenarioIndexerService {

    private static final Pattern POSTMAN_TEST_START_PATTERN = Pattern.compile("(pm|postman)\\s*.\\s*test\\s*\\(\\s*[\"']");

    @NonNull
    private final ObjectMapper objectMapper;

    @NonNull
    private final PostmanService postmanService;

    /**
     * Extract all Postman requests in JSON collection files in a ZIP archive, and return them as Cucumber-scenarios
     * equivalents.
     *
     * @param source  the source of the Postman collection in the Version Control System (also describes if root folders
     *                are country codes)
     * @param zipFile a ZIP archive containing one or several JSON files, in any sub-folders
     * @return scenarios describing all the given Postman requests (leafs in the tree)
     * @throws IOException if something goes wrong while reading the ZIP archive or parsing the JSON collection files
     */
    public List<Scenario> extractScenarios(Source source, File zipFile) throws IOException {
        List<Scenario> scenarios = new ArrayList<>();

        final FileSystem zip = FileSystems.newFileSystem(zipFile.toPath(), this.getClass().getClassLoader());

        for (Path jsonFilePath : listJsonFilePaths(zip)) {
            try (InputStream input = Files.newInputStream(jsonFilePath)) {
                final CollectionWithScripts collection = objectMapper.readValue(input, CollectionWithScripts.class);
                final String pathToStore = jsonFilePath.toString().substring(1); // Remove leading slash
                scenarios.addAll(collectCollectionScenarios(collection, source, pathToStore));
            }
        }

        return scenarios;
    }

    /**
     * Given a Postman collection, return all recursively-extracted requests as Cucumber scenarios.
     *
     * @param collection   the Postman collection with a tree of folders and leaf-requests
     * @param source       the source of the Postman collection in the Version Control System (also describes if root folders
     *                     are country codes)
     * @param jsonFilePath the path of the JSON collection file, relative to the {@code source} base URL
     * @return scenarios describing all the given Postman requests (leafs in the tree)
     */
    List<Scenario> collectCollectionScenarios(CollectionWithScripts collection, Source source, String jsonFilePath) {
        List<Scenario> scenarios = collectItemScenarios(collection.getItem(), source, new AtomicInteger(0), "", Collections.emptyList());

        final String collectionName = collection.getInfo().getName();
        for (Scenario scenario : scenarios) {
            scenario.setFeatureFile(jsonFilePath);
            scenario.setFeatureName(collectionName);
        }

        return scenarios;
    }

    /**
     * Given a tree of Postman collection items, return all recursively-extracted requests as Cucumber scenarios.
     *
     * @param items            a folder or request of a Postman collection, possibly with children
     * @param source           the source of the Postman collection in the Version Control System (also describes if root folders
     *                         are country codes)
     * @param requestPosition  the position of the last request in the Postman collection file (to increment to get the
     *                         position of the next request)
     * @param parentSeverity   the severity of the parent (if any, empty if none) folder: deepest folders&requests have
     *                         higher priority and will replace this parent severity if any
     * @param parentAssertions the assertions of the parent items of this Postman item (deepest items' assertions come
     *                         first)
     * @param parentFolders    the complete path of the parent-folders of the given items in the Postman collection file
     * @return scenarios describing all the given Postman requests (leafs in the tree): featureFile and featureName are
     * NOT initialized
     */
    List<Scenario> collectItemScenarios(ItemWithScripts[] items, Source source, AtomicInteger requestPosition, String parentSeverity, List<Assertion> parentAssertions, String... parentFolders) {
        List<Scenario> scenarios = new ArrayList<>();
        if (items != null) {
            for (ItemWithScripts item : items) {
                boolean isRootFolder = (parentFolders.length == 0);
                boolean isFolder = isRootFolder || item.isSubFolder();

                // Compute item severity (deepest items' severity override severity of previous ones)
                String severity = postmanService.getSeverity(ArrayUtils.addAll(parentFolders, item.getName()));
                if (StringUtils.isEmpty(severity)) {
                    severity = parentSeverity;
                }

                // Compute item path (without any tag)
                String nameWithoutTag = postmanService.removeSeverityTag(item.getName());
                String[] path = ArrayUtils.addAll(parentFolders, nameWithoutTag);

                // Compute assertions of item (deepest items' assertions come first)
                List<Assertion> assertions = ListUtils.union(extractAssertions(item), parentAssertions);

                if (isFolder) {
                    // Add the folder's children
                    scenarios.addAll(collectItemScenarios(item.getChildren(), source, requestPosition, severity, assertions, path));
                } else {
                    // Add the request
                    scenarios.add(toScenario(item, source, requestPosition, severity, assertions, path));
                }
            }
        }
        return scenarios;
    }

    /**
     * Given a request item from a Postman collection, return an equivalent Cucumber scenario.
     *
     * @param item            the Postman request item to transform to a scenario
     * @param source          the source of the Postman collection in the Version Control System (also describes if root folders
     *                        are country codes)
     * @param requestPosition the position of the request in the Postman collection file
     * @param severity        the deduced severity of this Postman request
     * @param assertions      the assertions of this Postman request followed by those of the parent folders, starting with
     *                        the deepest one
     * @param path            the complete path (parent-folders + request) of the request in the Postman collection file
     * @return a scenario describing the given Postman request
     */
    Scenario toScenario(ItemWithScripts item, Source source, AtomicInteger requestPosition, String severity, List<Assertion> assertions, String... path) {
        final String countryCodes;
        if (source.isPostmanCountryRootFolders()) {
            countryCodes = extractCountryCodes(path);
        } else {
            countryCodes = Tag.COUNTRY_ALL;
        }

        final Assertion[] assertionsArray = assertions.toArray(new Assertion[0]);

        String content = postmanService.buildScenarioContent(item.getRequest(), null, assertionsArray, null, true);

        Scenario scenario = new Scenario();
        scenario.setSource(source);
        scenario.setTags(StringUtils.isEmpty(severity) ? null : Tag.SEVERITY_PREFIX + severity);
        scenario.setCountryCodes(countryCodes);
        scenario.setSeverity(severity);
        scenario.setName(String.join(PostmanService.FOLDER_DELIMITER, path));
        scenario.setLine(requestPosition.incrementAndGet());
        scenario.setContent(content);
        return scenario;
    }

    /**
     * Extract all assertions for the item (excluding its children) from its first "test"-event script.
     *
     * @param item a folder or request Postman item
     * @return the extracted assertions: may be empty but never null
     */
    List<Assertion> extractAssertions(ItemWithScripts item) {
        if (item.getEvents() != null) {
            for (Event event : item.getEvents()) {
                if (event.getListen() == Listen.TEST) {
                    return guessAssertions(event.getScript().getExec());
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Extract all Postman assertions from a JavaScript source code (pm.test() and postman.test() calls, excluding the
     * ones commented out).
     *
     * @param javaScriptLines the JavaScript code containing Postman assertions
     * @return found assertions, if any: may be empty but never null
     */
    List<Assertion> guessAssertions(String[] javaScriptLines) {
        String javaScript = removeComments(String.join("\n", javaScriptLines));
        List<Assertion> assertions = new ArrayList<>();

        Matcher matcher = POSTMAN_TEST_START_PATTERN.matcher(javaScript);
        while (matcher.find()) {
            final int startIndex = matcher.end();
            char stringCharacter = javaScript.charAt(startIndex - 1); // Either ' or "
            int stopIndex = startIndex - 1;
            do {
                stopIndex = javaScript.indexOf(stringCharacter, stopIndex + 1);
            } while (isEscapedCharacter(javaScript, stopIndex)); // isEscapedCharacter==false anyway if stopIndex==-1
            if (stopIndex == -1) {
                stopIndex = javaScript.length();
            }
            String assertionName = StringEscapeUtils.unescapeJava(javaScript.substring(startIndex, stopIndex));
            assertions.add(new Assertion(assertionName));
        }

        return assertions;
    }

    /**
     * Given a JavaScript source code, a string in this code, and a character position in this string, return true if
     * the character is escaped by a previous backslash and the backslash is not escaped itself.
     *
     * @param javaScript     the JavaScript code containing the string in which we want to know if a character is escaped
     * @param characterIndex the index of the character in a string in the JavaScript code
     * @return true if the character of a String in the JavaScript code is escaped by a previous backslash, not escaped
     * itself
     */
    boolean isEscapedCharacter(String javaScript, int characterIndex) {
        int backslashCount = 0;
        for (int i = characterIndex - 1; i >= 0; i--) {
            if (javaScript.charAt(i) == '\\') {
                backslashCount++;
            } else {
                break;
            }
        }
        return (backslashCount % 2 == 1);
    }

    /**
     * Remove JavaScript comments.
     *
     * @param sourceCode the JavaScript source code where to remove comments
     * @return the same JavaScript source code without its comments
     * @see JavaScriptCommentRemover#removeComments(String)
     */
    String removeComments(String sourceCode) {
        // A very simple method, useful to enclose the new operator into a comment-remover that can be mocked
        return new JavaScriptCommentRemover().removeComments(sourceCode);
    }

    /**
     * Given the path of a request, extract country codes from the root folder (expected to be separated by '+'), and
     * concatenate them with ','.<br>
     * Note: the found codes are not matched with the Country table: found codes are not guaranteed to exist if
     * developers wrongly named the Postman collection' root folders.<br>
     * Only the first item of the path (the root folder) will be used for the extraction.<br>
     * The root folder must contains codes separated by '+'.<br>
     * Empty codes (eg. "++"...) will be ignored.<br>
     * If no country code is found, "all" is returned.<br>
     * If one of the country codes is "all", only that special-value is returned ("all+anyOther" still means "all").
     *
     * @param path the path of a Postman request (each parent folder and the request itself are path parts)
     * @return the extracted country codes (delimited by commas), or "all" if no code found or at least one of the codes
     * is "all"
     */
    String extractCountryCodes(String[] path) {
        if (path != null && path.length != 0) {
            String rootFolder = path[0];
            if (StringUtils.isNotEmpty(rootFolder)) {
                final Set<String> countryCodes = Arrays.stream(rootFolder.split("\\+"))
                        .map(String::trim)
                        .filter(StringUtils::isNotEmpty)
                        .collect(Collectors.toCollection(TreeSet::new)); // distinct and sorted

                // If "all" is present, other are redundant
                // Also return "all" if all country-codes were empty strings
                if (countryCodes.isEmpty() || countryCodes.contains(Tag.COUNTRY_ALL)) {
                    countryCodes.clear();
                    countryCodes.add(Tag.COUNTRY_ALL);
                }

                return String.join(Scenario.COUNTRY_CODES_SEPARATOR, countryCodes);
            }
        }

        return Tag.COUNTRY_ALL;
    }

    /**
     * Get a listing of all JSON files in a ZIP archive.
     *
     * @param zip a file-system open on a ZIP file
     * @return a sorted list of paths of all .json files in the ZIP file (eg. [ "/file1.zip", "/sub/folder/file2.zip" ])
     * @throws IOException if an I/O error is thrown when accessing the starting file
     */
    List<Path> listJsonFilePaths(FileSystem zip) throws IOException {
        List<Path> jsonFilePaths = new ArrayList<>();

        for (Path root : zip.getRootDirectories()) {
            try (final Stream<Path> walker = Files.walk(root)) {
                jsonFilePaths.addAll(walker
                        .filter(path -> path.toString().endsWith(".json"))
                        .sorted()
                        .collect(Collectors.toList()));
            }
        }

        return jsonFilePaths;
    }

}
