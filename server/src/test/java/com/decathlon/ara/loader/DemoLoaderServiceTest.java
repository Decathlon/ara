package com.decathlon.ara.loader;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DemoLoaderServiceTest {

    @InjectMocks
    private DemoLoaderService cut;

    @Test
    public void replaceFunctionalityIdPlaceholders_ShouldReplaceIds_WhenSomeOfThemArePresent() {
        // GIVEN
        Map<String, Long> functionalityIds = new HashMap<>();
        functionalityIds.put("A", Long.valueOf(21));
        functionalityIds.put("B", Long.valueOf(22));
        functionalityIds.put("C", Long.valueOf(23));
        String fileContent = "Functionality {{F-A}}, {{F-B}}, {{F-A}} & {{F-Z}}: Some Title";

        // WHEN
        final String replacedContent = cut.replaceFunctionalityIdPlaceholders(functionalityIds, fileContent);

        // THEN
        assertThat(replacedContent).isEqualTo("Functionality 21, 22, 21 & {{F-Z}}: Some Title");
    }

}
