/******************************************************************************
 * Copyright (C) 2019 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

package com.decathlon.ara.defect.rtc;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import java.util.Date;

import org.junit.jupiter.api.Test;

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
