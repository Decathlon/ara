package com.decathlon.ara.coverage;

import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.enumeration.CoverageLevel;
import com.decathlon.ara.service.dto.coverage.AxisPointDTO;
import java.util.Arrays;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class CoverageAxisGenerator implements AxisGenerator {

    @Override
    public String getCode() {
        return "coverage";
    }

    @Override
    public String getName() {
        return "Coverage level";
    }

    @Override
    public Stream<AxisPointDTO> getPoints(long projectId) {
        return Arrays.stream(CoverageLevel.values())
                .map(coverageLevel -> new AxisPointDTO(coverageLevel.name(), coverageLevel.getLabel(), coverageLevel.getTooltip()));
    }

    @Override
    public String[] getValuePoints(Functionality functionality) {
        return new String[] { functionality.getCoverageLevel().name() };
    }

}
