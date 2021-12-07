package com.decathlon.ara.service.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    /**
     * Format a duration between 2 dates.
     * For instance if the gap is: 2 days, 13 hours, 34 minutes, 58 seconds and 679 milliseconds, then the result is 2d 13h 34m 58s 679ms
     * @param startDate the start date
     * @param endDate the end date
     * @return a formatted duration between those 2 dates
     */
    public String getFormattedDurationBetween2Dates(LocalDateTime startDate, LocalDateTime endDate) {
        var totalMilliseconds = Math.abs(ChronoUnit.MILLIS.between(startDate, endDate));

        if (totalMilliseconds == 0) {
            return "0ms";
        }

        var millisecondsInASecond = 1000;
        var secondsInAMinute = 60;
        var minutesInAnHour = 60;
        var hoursInADay = 24;

        var millisecondsInAMinute = millisecondsInASecond * secondsInAMinute;
        var millisecondsInAnHour = millisecondsInAMinute * minutesInAnHour;
        var millisecondsInADay = millisecondsInAnHour * hoursInADay;

        var milliseconds = totalMilliseconds % millisecondsInASecond;
        var seconds = (totalMilliseconds / millisecondsInASecond) % secondsInAMinute;
        var minutes = ((totalMilliseconds / millisecondsInAMinute) % minutesInAnHour);
        var hours   = ((totalMilliseconds / millisecondsInAnHour) % hoursInADay);
        var days = totalMilliseconds / millisecondsInADay;

        var formattedMilliseconds = milliseconds > 0 ? String.format("%dms", milliseconds) : "";
        var formattedSeconds = seconds > 0 ? String.format("%ds", seconds) : "";
        var formattedMinutes = minutes > 0 ? String.format("%dm", minutes) : "";
        var formattedHours = hours > 0 ? String.format("%dh", hours) : "";
        var formattedDays = days > 0 ? String.format("%dd", days) : "";

        return List.of(formattedDays, formattedHours, formattedMinutes, formattedSeconds, formattedMilliseconds)
                .stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(" "));
    }

}
