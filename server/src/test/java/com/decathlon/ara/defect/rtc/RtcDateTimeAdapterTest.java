package com.decathlon.ara.defect.rtc;

import java.text.ParseException;
import java.util.Date;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RtcDateTimeAdapterTest {

    private final RtcDateTimeAdapter cut = new RtcDateTimeAdapter();

    @Test
    public void unmarshal_should_parse_date() throws ParseException {
        // WHEN
        final Date date = cut.unmarshal("2000-12-31T23:59:55.987+0200");

        // THEN
        assertThat(date.getTime()).isEqualTo(978299995987L);
    }

    @Test
    public void unmarshal_should_return_null_for_null_values() throws ParseException {
        // WHEN
        final Date date = cut.unmarshal(null);

        // THEN
        assertThat(date).isNull();
    }

    @Test
    public void unmarshal_should_return_null_for_empty_values() throws ParseException {
        // WHEN
        final Date date = cut.unmarshal("");

        // THEN
        assertThat(date).isNull();
    }

    @Test
    public void marshal_should_format_date() {
        // WHEN
        final String formattedDate = cut.marshal(new Date(978299995987L));

        // THEN
        assertThat(formattedDate).isEqualTo("2000-12-31T21:59:55.987+0000");
    }

    @Test
    public void marshal_should_return_null_for_null_values() {
        // WHEN
        final String formattedDate = cut.marshal(null);

        // THEN
        assertThat(formattedDate).isNull();
    }

}
