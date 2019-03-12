package com.decathlon.ara.service.dto.ignore;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ScenarioIgnoreCountDTOTest {

    @Test
    public void getPercent_returns_0_when_no_total() {
        // GIVEN
        ScenarioIgnoreCountDTO count = new ScenarioIgnoreCountDTO().withTotal(0);

        // WHEN / THEN
        assertThat(count.getPercent()).isEqualTo(0);
    }

    @Test
    public void getPercent_returns_correct_percentage() {
        // GIVEN
        ScenarioIgnoreCountDTO count = new ScenarioIgnoreCountDTO().withIgnored(1).withTotal(4);

        // WHEN / THEN
        assertThat(count.getPercent()).isEqualTo(25);
    }

    @Test
    public void getPercent_returns_ceiled_percentage() {
        // GIVEN
        ScenarioIgnoreCountDTO count = new ScenarioIgnoreCountDTO().withIgnored(991).withTotal(1000);

        // WHEN / THEN
        assertThat(count.getPercent()).isEqualTo(100);
    }

}
