package com.decathlon.ara.service.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

@Service
public class DateService {

    /**
     * @return the current date and time
     */
    public Date now() {
        return new Date();
    }

    /**
     * Return today's date minus a period
     * @param value how much unit
     * @param periodType what type (are accepted: DAY, WEEK, MONTH or YEAR - case insensitive)
     * @return the date
     */
    public Optional<Date> getTodayDateMinusPeriod(Integer value, String periodType) {
        if (value == null || value < 0 || StringUtils.isBlank(periodType)) {
            return Optional.empty();
        }
        var today = LocalDate.now();
        var localDateByPeriod = Map.ofEntries(
                entry("DAY", today.minusDays(value)),
                entry("WEEK", today.minusWeeks(value)),
                entry("MONTH", today.minusMonths(value)),
                entry("YEAR", today.minusYears(value))
        );
        var localDateMinusPeriod = localDateByPeriod.get(periodType.toUpperCase());
        if (localDateMinusPeriod == null) {
            return Optional.empty();
        }
        return Optional.of(Date.from(localDateMinusPeriod.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
    }

}
