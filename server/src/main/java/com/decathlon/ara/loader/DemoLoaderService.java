package com.decathlon.ara.loader;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Utility methods used by data loads for the the Demo project.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DemoLoaderService {

    /**
     * Replace all functionality placeholders by their real IDs.<br>
     * Eg. "Functionality {{F-A}}: Important Functionality" => "Functionality 42: Important Functionality"
     *
     * @param functionalityIds a map of letters (eg. "A" for the placeholder "{{F-A}}") as keys, and functionality IDs
     *                         as values
     * @param fileContent      a text file content where to replace all functionality placeholders
     * @return the modified fileContent with replaced placeholders
     */
    String replaceFunctionalityIdPlaceholders(Map<String, Long> functionalityIds, String fileContent) {
        String replaced = fileContent;
        for (Map.Entry<String, Long> entry : functionalityIds.entrySet()) {
            replaced = replaced.replace("{{F-" + entry.getKey() + "}}", entry.getValue().toString());
        }
        return replaced;
    }

}
