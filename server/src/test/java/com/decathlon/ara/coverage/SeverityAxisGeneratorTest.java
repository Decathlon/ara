package com.decathlon.ara.coverage;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.enumeration.FunctionalitySeverity;
import com.decathlon.ara.service.dto.coverage.AxisPointDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SeverityAxisGeneratorTest {

    private static final int ANY_PROJECT_ID = -42;

    @InjectMocks
    private SeverityAxisGenerator cut;

    @Test
    public void testGetCode() {
        assertThat(cut.getCode()).isEqualTo("severity");
    }

    @Test
    public void testGetName() {
        assertThat(cut.getName()).isEqualTo("Severities");
    }

    @Test
    public void testGetPoints() {
        // WHEN / THEN
        assertThat(cut.getPoints(ANY_PROJECT_ID)).containsExactly(
                new AxisPointDTO("HIGH", "High", null),
                new AxisPointDTO("MEDIUM", "Medium", null),
                new AxisPointDTO("LOW", "Low", null));
    }

    @Test
    public void testGetValuePoints_without_severity() {
        // GIVEN
        Functionality functionality = new Functionality();

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isNull();
    }

    @Test
    public void testGetValuePoints_with_severity() {
        // GIVEN
        Functionality functionality = new Functionality().withSeverity(FunctionalitySeverity.HIGH);

        // WHEN / THEN
        assertThat(cut.getValuePoints(functionality)).isEqualTo(new String[] { "HIGH" });
    }

}
