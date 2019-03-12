package com.decathlon.ara.coverage;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.enumeration.FunctionalitySeverity;
import com.decathlon.ara.service.dto.coverage.AxisPointDTO;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class SeverityAxisGenerator implements AxisGenerator {

    @Override
    public String getCode() {
        return "severity";
    }

    @Override
    public String getName() {
        return "Severities";
    }

    @Override
    public Stream<AxisPointDTO> getPoints(long projectId) {
        return Arrays.stream(FunctionalitySeverity.values())
                .sorted(Collections.reverseOrder()) // From the highest severity to the lowest one
                .map(severity -> new AxisPointDTO(severity.name(), StringUtils.capitalize(severity.name().toLowerCase()), null));
    }

    @Override
    public String[] getValuePoints(Functionality functionality) {
        return (functionality.getSeverity() == null ? null : new String[] { functionality.getSeverity().name() });
    }

}
