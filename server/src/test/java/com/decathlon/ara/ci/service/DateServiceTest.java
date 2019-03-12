package com.decathlon.ara.ci.service;

import java.util.Date;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DateServiceTest {

    private DateService cut = new DateService();

    @Test
    public void now_should_return_the_current_date() {
        // GIVEN
        Long before = Long.valueOf(new Date().getTime() - 1);

        // WHEN
        Date now = cut.now();

        // THEN
        Long after = Long.valueOf(new Date().getTime() + 1);
        assertThat(now.getTime()).isBetween(before, after);
    }

}
