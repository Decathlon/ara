package com.decathlon.ara.service.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DateServiceTest {

    @InjectMocks
    private DateService dateService;

    @Test
    void now_returnTheCurrentDate() {
        // Given
        Long before = Long.valueOf(new Date().getTime() - 1);

        // When
        Date now = new DateService().now();

        // Then
        Long after = Long.valueOf(new Date().getTime() + 1);
        assertThat(now.getTime()).isBetween(before, after);
    }

    @Test
    void getTodayDateMinusPeriod_returnEmptyOptional_whenValueIsNull() {
        // Given
        Integer value = null;
        String type = null;

        // When

        // Then
        var resultDate = dateService.getTodayDateMinusPeriod(value, type);
        assertThat(resultDate).isNotNull().isEmpty();
    }

    @Test
    void getTodayDateMinusPeriod_returnEmptyOptional_whenValueIsNegative() {
        // Given
        var value = -1;
        String type = null;

        // When

        // Then
        var resultDate = dateService.getTodayDateMinusPeriod(value, type);
        assertThat(resultDate).isNotNull().isEmpty();
    }

    @Test
    void getTodayDateMinusPeriod_returnEmptyOptional_whenTypeIsNull() {
        // Given
        var value = 3;
        String type = null;

        // When

        // Then
        var resultDate = dateService.getTodayDateMinusPeriod(value, type);
        assertThat(resultDate).isNotNull().isEmpty();
    }

    @Test
    void getTodayDateMinusPeriod_returnEmptyOptional_whenTypeIsBlank() {
        // Given
        var value = 3;
        var type = "         ";

        // When

        // Then
        var resultDate = dateService.getTodayDateMinusPeriod(value, type);
        assertThat(resultDate).isNotNull().isEmpty();
    }

    @Test
    void getTodayDateMinusPeriod_returnEmptyOptional_whenTypeIsUnknown() {
        // Given
        var value = 3;
        var type = "unknown-type";

        // When

        // Then
        var resultDate = dateService.getTodayDateMinusPeriod(value, type);
        assertThat(resultDate).isNotNull().isEmpty();
    }

    @Test
    void getTodayDateMinusPeriod_returnSameDay_whenTypeIsDayAndValueIsZero() {
        // Given
        var value = 0;
        var type = "DAY";
        var expectedDate = Date.from(LocalDate.now().minusDays(0).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        // When

        // Then
        var resultDate = dateService.getTodayDateMinusPeriod(value, type);
        assertThat(resultDate).isNotNull().isNotEmpty();
        var actualDate = resultDate.get();
        assertThat(actualDate).isEqualTo(expectedDate);
    }

    @Test
    void getTodayDateMinusPeriod_returnSameDay_whenTypeIsWeekAndValueIsZero() {
        // Given
        var value = 0;
        var type = "WEEK";
        var expectedDate = Date.from(LocalDate.now().minusWeeks(0).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        // When

        // Then
        var resultDate = dateService.getTodayDateMinusPeriod(value, type);
        assertThat(resultDate).isNotNull().isNotEmpty();
        var actualDate = resultDate.get();
        assertThat(actualDate).isEqualTo(expectedDate);
    }

    @Test
    void getTodayDateMinusPeriod_returnSameDay_whenTypeIsMonthAndValueIsZero() {
        // Given
        var value = 0;
        var type = "MONTH";
        var expectedDate = Date.from(LocalDate.now().minusMonths(0).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        // When

        // Then
        var resultDate = dateService.getTodayDateMinusPeriod(value, type);
        assertThat(resultDate).isNotNull().isNotEmpty();
        var actualDate = resultDate.get();
        assertThat(actualDate).isEqualTo(expectedDate);
    }

    @Test
    void getTodayDateMinusPeriod_returnTodayDateMinusDay_whenTypeIsDay() {
        // Given
        var value = 3;
        var type = "DAY";
        var expectedDate = Date.from(LocalDate.now().minusDays(3).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        // When

        // Then
        var resultDate = dateService.getTodayDateMinusPeriod(value, type);
        assertThat(resultDate).isNotNull().isNotEmpty();
        var actualDate = resultDate.get();
        assertThat(actualDate).isEqualTo(expectedDate);
    }

    @Test
    void getTodayDateMinusPeriod_returnTodayDateMinusWeek_whenTypeIsWeek() {
        // Given
        var value = 3;
        var type = "WEEK";
        var expectedDate = Date.from(LocalDate.now().minusWeeks(3).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        // When

        // Then
        var resultDate = dateService.getTodayDateMinusPeriod(value, type);
        assertThat(resultDate).isNotNull().isNotEmpty();
        var actualDate = resultDate.get();
        assertThat(actualDate).isEqualTo(expectedDate);
    }

    @Test
    void getTodayDateMinusPeriod_returnTodayDateMinusMonth_whenTypeIsMonth() {
        // Given
        var value = 3;
        var type = "MONTH";
        var expectedDate = Date.from(LocalDate.now().minusMonths(3).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        // When

        // Then
        var resultDate = dateService.getTodayDateMinusPeriod(value, type);
        assertThat(resultDate).isNotNull().isNotEmpty();
        var actualDate = resultDate.get();
        assertThat(actualDate).isEqualTo(expectedDate);
    }

    @ParameterizedTest
    @CsvSource({
            "0,YEAR",
            "3,YEAR",
            "3,yEaR"
    })
    void getTodayDateMinusPeriod_returnTodayDateMinusYear_whenTypeIsYear(Integer value, String type) {
        // Given
        var expectedDate = Date.from(LocalDate.now().minusYears(value).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        // When

        // Then
        var resultDate = dateService.getTodayDateMinusPeriod(value, type);
        assertThat(resultDate).isNotNull().isNotEmpty();
        var actualDate = resultDate.get();
        assertThat(actualDate).isEqualTo(expectedDate);
    }

}
