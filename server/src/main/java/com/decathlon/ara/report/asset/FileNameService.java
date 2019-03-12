package com.decathlon.ara.report.asset;

import com.decathlon.ara.ci.service.DateService;
import java.text.SimpleDateFormat;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.decathlon.ara.lib.embed.producer.StructuredEmbeddingsBuilder.HUMAN_AND_MACHINE_READABLE_TIMESTAMP_PATTERN;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FileNameService {

    /**
     * For the URL to be stored in a VARCHAR(256), accounting for HTTP domain/port/folderPath/date/extension.
     */
    private static final int MAX_SCENARIO_NAME_LENGTH = 128;

    @NonNull
    private final DateService dateService;

    /**
     * @param scenarioName the raw scenario name, as displayed to users
     * @param extension    file extension without the dot; can be null or empty to generate a file name without extension (for a folder, for
     *                     instance)
     * @return a file or folder name with current date, time and scenario name escaped
     */
    String generateReportFileName(final String scenarioName, final String extension) {
        final String truncatedScenarioName = StringUtils.left(scenarioName, MAX_SCENARIO_NAME_LENGTH);
        final String safeScenarioName = truncatedScenarioName.replace(' ', '-').replaceAll("[^a-zA-Z0-9\\-]", "");
        final String formattedDate = new SimpleDateFormat(HUMAN_AND_MACHINE_READABLE_TIMESTAMP_PATTERN).format(dateService.now());
        return formattedDate + "-" + safeScenarioName + (StringUtils.isEmpty(extension) ? "" : ("." + extension));
    }

}
