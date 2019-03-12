package com.decathlon.ara.loader;

import com.decathlon.ara.common.NotGonnaHappenException;
import com.decathlon.ara.service.ScenarioService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DemoScenarioLoaderTest {

    @Mock
    private ScenarioService scenarioService;

    @Mock
    private DemoLoaderService demoLoaderService;

    @InjectMocks
    private DemoScenarioLoader cut;

    @Test
    public void getResourceAsUtf8String_ShouldReturnUtf8Content_WhenResourceExists() {
        // GIVEN
        String resource = "demo/resource";

        // WHEN
        final String content = cut.getResourceAsUtf8String(resource);

        // THEN
        assertThat(content.replaceAll("[\r\n]", "")).isEqualTo("content-withÜTF8");
    }

    @Test(expected = NotGonnaHappenException.class)
    public void getResourceAsUtf8String_ShouldThrowNotGonnaHappenException_WhenDeveloperFucksUpCodeOrPackaging() {
        cut.getResourceAsUtf8String("nonexistent");
    }

}
