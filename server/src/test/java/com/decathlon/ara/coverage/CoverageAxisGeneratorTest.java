package com.decathlon.ara.coverage;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.Scenario;
import com.decathlon.ara.domain.enumeration.CoverageLevel;
import com.decathlon.ara.service.dto.coverage.AxisPointDTO;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CoverageAxisGeneratorTest {

    private static final int ANY_PROJECT_ID = -42;

    @InjectMocks
    private CoverageAxisGenerator cut;

    private static AxisPointDTO axisPointOf(CoverageLevel level) {
        return new AxisPointDTO(level.name(), level.getLabel(), level.getTooltip());
    }

    @Test
    public void testGetCode() {
        assertThat(cut.getCode()).isEqualTo("coverage");
    }

    @Test
    public void testGetName() {
        assertThat(cut.getName()).isEqualTo("Coverage level");
    }

    @Test
    public void testGetPoints() {
        // WHEN / THEN
        assertThat(cut.getPoints(ANY_PROJECT_ID)).containsExactly(
                axisPointOf(CoverageLevel.COVERED),
                axisPointOf(CoverageLevel.PARTIALLY_COVERED),
                axisPointOf(CoverageLevel.IGNORED_COVERAGE),
                axisPointOf(CoverageLevel.STARTED),
                axisPointOf(CoverageLevel.NOT_AUTOMATABLE),
                axisPointOf(CoverageLevel.NOT_COVERED));
    }

    @Test
    public void testGetValuePoints_COVERED() {
        // GIVEN
        Functionality functionality = new Functionality()
                .withScenarios(Collections.singleton(new Scenario()));

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "COVERED" });
    }

    @Test
    public void testGetValuePoints_PARTIALLY_COVERED() {
        // GIVEN
        Functionality functionality = new Functionality()
                .withScenarios(new HashSet<>(Arrays.asList(
                        new Scenario().withLine(1),
                        new Scenario().withLine(2).withIgnored(true))));

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "PARTIALLY_COVERED" });
    }

    @Test
    public void testGetValuePoints_IGNORED_COVERAGE() {
        // GIVEN
        Functionality functionality = new Functionality()
                .withScenarios(Collections.singleton(new Scenario().withIgnored(true)));

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "IGNORED_COVERAGE" });
    }

    @Test
    public void testGetValuePoints_STARTED() {
        // GIVEN
        Functionality functionality = new Functionality().withStarted(Boolean.TRUE);

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "STARTED" });
    }

    @Test
    public void testGetValuePoints_NOT_AUTOMATABLE() {
        // GIVEN
        Functionality functionality = new Functionality().withNotAutomatable(Boolean.TRUE);

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "NOT_AUTOMATABLE" });
    }

    @Test
    public void testGetValuePoints_NOT_COVERED() {
        // GIVEN
        Functionality functionality = new Functionality();

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "NOT_COVERED" });
    }

}
