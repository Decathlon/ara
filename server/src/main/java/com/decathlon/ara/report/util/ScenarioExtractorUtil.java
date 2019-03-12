package com.decathlon.ara.report.util;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.domain.Source;
import com.decathlon.ara.postman.service.PostmanService;
import com.decathlon.ara.report.bean.Element;
import com.decathlon.ara.report.bean.Feature;
import com.decathlon.ara.report.bean.Tag;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;

/**
 * A set of static functions with no dependency nor side-effect (no download, upload, database access...) that take a parsed Cucumber's report.json (ideally with dry-run option and with ignored scenarios as well) and extract scenarios in it.
 */
@UtilityClass
public class ScenarioExtractorUtil {

    private static final Pattern FUNCTIONALITY_IDS_ZONE_PATTERN = Pattern.compile("F[uo]nction[n]?al[l]?it[yi][e]?[s]?[ \\t]([^:]+)[:](.*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern FUNCTIONALITY_PATTERN_ON_ID = Pattern.compile("[^;]+([;]f[uo]nction[n]?al[l]?it[yi][e]?[s]?-[^:]+[:][-]*).*", Pattern.CASE_INSENSITIVE);
    private static final Pattern FUNCTIONALITY_PATTERN_ON_NAME = Pattern.compile("(F[uo]nction[n]?al[l]?it[yi][e]?[s]?[ \\t][^:]*[:][ \\t]*).*", Pattern.CASE_INSENSITIVE);
    private static final String FUNCTIONALITY_IDS_SPLIT_PATTERN = "(,|&|and)";

    public static List<Scenario> extractScenarios(Source source, List<Feature> features) {
        List<Scenario> scenarios = new ArrayList<>();
        Scenario lastBackground = null;
        for (Feature feature : features) {
            for (Element element : feature.getElements()) {
                if (element.isBackground()) {
                    lastBackground = extractBackground(element);
                } else if (element.isScenario() && element.isSingleScenarioOrFirstOfOutline()) {
                    scenarios.add(extractScenario(source, feature, element, lastBackground));
                    lastBackground = null;
                }
            }
            lastBackground = null;
        }
        return scenarios;
    }

    private static Scenario extractBackground(Element element) {
        Scenario background = new Scenario();
        background.setContent(CucumberReportUtil.extractScenarioContent(element, null));
        return background;
    }

    private static Scenario extractScenario(Source source, Feature feature, Element element, Scenario lastBackground) {
        Set<String> featureTags = Tag.names(feature.getTags());
        Set<String> scenarioTags = Tag.names(element.getTags());
        Set<String> allTags = Sets.union(featureTags, scenarioTags);

        Scenario scenario = new Scenario();
        scenario.setSource(source);
        scenario.setFeatureFile(feature.getUri());
        scenario.setFeatureName(feature.getName());
        scenario.setFeatureTags(String.join(" ", featureTags));
        scenario.setTags(String.join(" ", scenarioTags));
        scenario.setIgnored(allTags.contains(Tag.IGNORE));
        scenario.setCountryCodes(String.join(Scenario.COUNTRY_CODES_SEPARATOR, Tag.extractCountryCodes(allTags)));
        scenario.setSeverity(Tag.extractSeverity(allTags, element.getName()));
        scenario.setName(element.getName());
        scenario.setLine(element.getLine().intValue());
        scenario.setContent(CucumberReportUtil.extractScenarioContent(element, lastBackground == null ? null : lastBackground.getContent()));
        return scenario;
    }

    public static List<Long> extractFunctionalityIds(String scenarioName) {
        List<Long> ids = new ArrayList<>();
        for (final String maybeId : maybeIds(scenarioName)) {
            try {
                ids.add(Long.valueOf(maybeId.trim()));
            } catch (final NumberFormatException e) {
                // Will be output in scenario.wrong_functionality_ids"
            }
        }
        return ids;
    }

    public static List<String> extractWrongFunctionalityIds(String scenarioName, Collection<Functionality> functionalities) {
        List<String> wrongIds = new ArrayList<>();
        for (final String rawMaybeId : maybeIds(scenarioName)) {
            String maybeId = rawMaybeId.trim();
            try {
                Long longId = Long.valueOf(maybeId);
                if (functionalities.stream().noneMatch(f -> longId.equals(f.getId()))) {
                    wrongIds.add(maybeId);
                }
            } catch (final NumberFormatException e) {
                wrongIds.add(maybeId);
            }
        }
        return wrongIds;
    }

    public static String removeFunctionalitiesFromScenarioCucumberId(String cucumberId) {
        if (cucumberId != null) {
            final Matcher matcher = FUNCTIONALITY_PATTERN_ON_ID.matcher(cucumberId);
            if (matcher.matches()) {
                int startIndex = matcher.start(1);
                int stopIndex = matcher.end(1);
                if (startIndex > 0 && startIndex == cucumberId.indexOf(';') && stopIndex < cucumberId.length()) {
                    return cucumberId.substring(0, startIndex) + ";" + cucumberId.substring(stopIndex);
                }
            }
        }
        return cucumberId;
    }

    public static String removeFunctionalitiesFromScenarioName(String name) {
        if (name != null) {
            final Matcher matcher = FUNCTIONALITY_PATTERN_ON_NAME.matcher(name);
            if (matcher.matches()) {
                return name.substring(matcher.group(1).length());
            }
        }
        return name;
    }

    private static String[] maybeIds(String scenarioName) {
        String[] maybeIds = new String[0];
        for (String scenarioNamePart : scenarioName.split(PostmanService.FOLDER_DELIMITER)) {
            final Matcher matcher = FUNCTIONALITY_IDS_ZONE_PATTERN.matcher(scenarioNamePart);
            if (matcher.matches()) {
                maybeIds = ArrayUtils.addAll(maybeIds, matcher.group(1).split(FUNCTIONALITY_IDS_SPLIT_PATTERN));
            }
        }
        return maybeIds;
    }

}
