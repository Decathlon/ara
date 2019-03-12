package com.decathlon.ara.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

@UtilityClass
public final class TestUtil {

    public static final Long NONEXISTENT = Long.valueOf(-42);

    public static String loadUtf8ResourceAsString(String fileName) throws IOException {
        return IOUtils.toString(openResourceStream(fileName), StandardCharsets.UTF_8);
    }

    public static InputStream openResourceStream(String fileName) {
        return TestUtil.class.getClassLoader().getResourceAsStream(fileName);
    }

    /**
     * Constructs a Timestamp with the given date and time set for the default time zone with the default locale.
     *
     * @param year       the value used to set the <code>YEAR</code> calendar field in the calendar.
     * @param month      the value used to set the <code>MONTH</code> calendar field in the calendar. Month value is 0-based. e.g., 0 for January.
     * @param dayOfMonth the value used to set the <code>DAY_OF_MONTH</code> calendar field in the calendar.
     * @param hourOfDay  the value used to set the <code>HOUR_OF_DAY</code> calendar field in the calendar.
     * @param minute     the value used to set the <code>MINUTE</code> calendar field in the calendar.
     * @param second     the value used to set the <code>SECOND</code> calendar field in the calendar.
     */
    public static Timestamp timestamp(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
        return new Timestamp(new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second).getTimeInMillis());
    }

    public static String header(ResponseEntity<?> response, String headerName) {
        for (Entry<String, List<String>> header : response.getHeaders().entrySet()) {
            if (header.getKey().equals(headerName)) {
                if (header.getValue().size() > 1) {
                    throw new RuntimeException("More than one value for header " + headerName);
                }
                return header.getValue().get(0);
            }
        }
        return null;
    }

    public static Long[] longs(int... integers) {
        return Arrays.stream(integers).asLongStream().boxed().toArray(Long[]::new);
    }

    public static Long[] longs(long... longs) {
        return Arrays.stream(longs).boxed().toArray(Long[]::new);
    }

    public static PageRequest firstPageOf10() {
        int pageIndex = 0;
        int pageSize = 10;
        return PageRequest.of(pageIndex, pageSize);
    }

    public static <T> T get(Set<T> set, int index) {
        Iterator<T> iterator = set.iterator();
        T current = null;
        int i = -1;
        for (; i < index; i++) { // We will never ask an index out of range
            current = iterator.next();
        }
        if (i == index) {
            return current;
        }
        throw new RuntimeException("Cannot find index " + index);
    }

}
