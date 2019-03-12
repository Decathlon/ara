package com.decathlon.ara.domain;

import com.decathlon.ara.domain.enumeration.CoverageLevel;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FunctionalityTest {

    @Test
    public void getCoverageLevel_should_return_COVERED() {
        // GIVEN
        Functionality functionality = new Functionality()
                .withScenarios(Collections.singleton(new Scenario()));

        // WHEN/THEN
        assertThat(functionality.getCoverageLevel()).isEqualTo(CoverageLevel.COVERED);
    }

    @Test
    public void getCoverageLevel_should_return_PARTIALLY_COVERED() {
        // GIVEN
        Functionality functionality = new Functionality()
                .withScenarios(new HashSet<>(Arrays.asList(
                        new Scenario().withLine(1),
                        new Scenario().withLine(2).withIgnored(true))));

        // WHEN/THEN
        assertThat(functionality.getCoverageLevel()).isEqualTo(CoverageLevel.PARTIALLY_COVERED);
    }

    @Test
    public void getCoverageLevel_should_return_IGNORED_COVERAGE() {
        // GIVEN
        Functionality functionality = new Functionality()
                .withScenarios(Collections.singleton(new Scenario().withIgnored(true)));

        // WHEN/THEN
        assertThat(functionality.getCoverageLevel()).isEqualTo(CoverageLevel.IGNORED_COVERAGE);
    }

    @Test
    public void getCoverageLevel_should_return_STARTED() {
        // GIVEN
        Functionality functionality = new Functionality().withStarted(Boolean.TRUE);

        // WHEN/THEN
        assertThat(functionality.getCoverageLevel()).isEqualTo(CoverageLevel.STARTED);
    }

    @Test
    public void getCoverageLevel_should_return_NOT_AUTOMATABLE() {
        // GIVEN
        Functionality functionality = new Functionality().withNotAutomatable(Boolean.TRUE);

        // WHEN/THEN
        assertThat(functionality.getCoverageLevel()).isEqualTo(CoverageLevel.NOT_AUTOMATABLE);
    }

    @Test
    public void getCoverageLevel_should_return_NOT_COVERED() {
        // GIVEN
        Functionality functionality = new Functionality();

        // WHEN/THEN
        assertThat(functionality.getCoverageLevel()).isEqualTo(CoverageLevel.NOT_COVERED);
    }

}
