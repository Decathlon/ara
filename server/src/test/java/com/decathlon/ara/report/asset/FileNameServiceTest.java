package com.decathlon.ara.report.asset;

import com.decathlon.ara.ci.service.DateService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.decathlon.ara.util.TestUtil.timestamp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileNameServiceTest {

    @Mock
    private DateService dateService;

    @InjectMocks
    private FileNameService cut;

    @Test
    public void generateReportFileName_ShouldGenerateADatedFileName_WhenCalledWithAnExtension() {
        // GIVEN
        when(dateService.now()).thenReturn(timestamp(2018, 1, 1, 1, 1, 1));

        // WHEN
        final String fileName = cut.generateReportFileName("Name", "ext");

        // THEN
        assertThat(fileName).isEqualTo("2018.02.01-01h01m01.000-Name.ext");
    }

    @Test
    public void generateReportFileName_ShouldGenerateADatedDirectoryName_WhenCalledWithoutExtension() {
        // GIVEN
        when(dateService.now()).thenReturn(timestamp(2018, 1, 1, 1, 1, 1));

        // WHEN
        final String fileName = cut.generateReportFileName("Name", null);

        // THEN
        assertThat(fileName).isEqualTo("2018.02.01-01h01m01.000-Name");
    }

    @Test
    public void generateReportFileName_ShouldTruncateName_WhenCalledWithALargeScenarioName() {
        // GIVEN
        when(dateService.now()).thenReturn(timestamp(2018, 1, 1, 1, 1, 1));

        // WHEN
        final String fileName = cut.generateReportFileName("" +
                "Veeeeeeeeeeeeeeeeeeeeerrrrrrrrrrrrrrrrrrrrrryyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy" +
                "Loooooooooooooooooooooooooooooooonnnnnnnnnnnnnnnng" +
                "Name", null);

        // THEN
        assertThat(fileName).isEqualTo("2018.02.01-01h01m01.000-VeeeeeeeeeeeeeeeeeeeeerrrrrrrrrrrrrrrrrrrrrryyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyLoooooooooooooooooooooooooooooooonnnnnnnnnnnnnnnngN");
    }

    @Test
    public void generateReportFileName_ShouldReplaceSpacesWithDashes_WhenScenarioNameHasSpaces() {
        // GIVEN
        when(dateService.now()).thenReturn(timestamp(2018, 1, 1, 1, 1, 1));

        // WHEN
        final String fileName = cut.generateReportFileName("Scenario Name", null);

        // THEN
        assertThat(fileName).isEqualTo("2018.02.01-01h01m01.000-Scenario-Name");
    }

    @Test
    public void generateReportFileName_ShouldRemoveNonAlphaNumericCharacters_WhenScenarioNameHasSpecialCharacters() {
        // GIVEN
        when(dateService.now()).thenReturn(timestamp(2018, 1, 1, 1, 1, 1));

        // WHEN
        final String fileName = cut.generateReportFileName("Scenario+Name!042", null);

        // THEN
        assertThat(fileName).isEqualTo("2018.02.01-01h01m01.000-ScenarioName042");
    }

}
