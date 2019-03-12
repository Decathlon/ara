package com.decathlon.ara.coverage;

import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.domain.enumeration.FunctionalityType;
import com.decathlon.ara.common.NotGonnaHappenException;
import com.decathlon.ara.service.dto.coverage.AxisDTO;
import com.decathlon.ara.service.dto.coverage.AxisPointDTO;
import com.decathlon.ara.service.dto.coverage.CoverageDTO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CoverageService {

    @NonNull
    private final FunctionalityRepository functionalityRepository;

    @NonNull
    private final CountryAxisGenerator countryAxisGenerator;

    @NonNull
    private final SeverityAxisGenerator severityAxisGenerator;

    @NonNull
    private final TeamAxisGenerator teamAxisGenerator;

    @NonNull
    private final CoverageAxisGenerator coverageAxisGenerator;

    public CoverageDTO computeCoverage(long projectId) {
        List<AxisGenerator> generators = new ArrayList<>();
        generators.add(countryAxisGenerator);
        generators.add(severityAxisGenerator);
        generators.add(teamAxisGenerator);
        generators.add(coverageAxisGenerator);

        Set<Functionality> functionalities = functionalityRepository.findAllByProjectIdAndType(projectId, FunctionalityType.FUNCTIONALITY);

        CoverageDTO coverage = new CoverageDTO();
        coverage.setAxes(generators.stream()
                .map(generator -> new AxisDTO(
                        generator.getCode(),
                        generator.getName(),
                        getAllPoints(generator, projectId))
                ).collect(Collectors.toList()));

        coverage.setValues(computeValues(functionalities, coverage.getAxes(), generators));

        return coverage;
    }

    List<AxisPointDTO> getAllPoints(AxisGenerator generator, long projectId) {
        List<AxisPointDTO> points = new ArrayList<>();
        points.add(AxisPointDTO.ALL);
        points.addAll(generator.getPoints(projectId).collect(Collectors.toList()));
        return points;
    }

    int[] computeValues(Collection<Functionality> functionalities, List<AxisDTO> axes, List<AxisGenerator> axisGenerators) {
        int[] values = new int[axes.stream().mapToInt(axis -> axis.getPoints().size()).reduce(1, Math::multiplyExact)];

        for (Functionality functionality : functionalities) {
            List<String[]> functionalityValuePoints = new ArrayList<>();
            for (AxisGenerator axisGenerator : axisGenerators) {
                functionalityValuePoints.add(axisGenerator.getValuePoints(functionality));
            }
            incrementValueForFunctionalityProperties(values, functionalityValuePoints, axes);
        }

        return values;
    }

    /**
     * Given a multi-dimensional array defined by {@code axes} with values {@code values}, increment the value at the point {@code functionalityValuePoints}.
     *
     * @param values                   all values of the multi-dimensional array: the value at the given data-point will be incremented
     * @param functionalityValuePoints the id of the points for each axis/dimension of the multi-dimensional array at the given data-point to search for
     * @param axes                     the axis/dimension definitions of the multi-dimensional array
     */
    void incrementValueForFunctionalityProperties(int[] values, List<String[]> functionalityValuePoints, List<AxisDTO> axes) {
        incrementValueForFunctionalityProperties(values, functionalityValuePoints, axes, new int[axes.size()], 0);
    }

    /**
     * Given a multi-dimensional array defined by {@code axes} with values {@code values}, increment the value at the point {@code functionalityValuePoints}.
     *
     * @param values                   all values of the multi-dimensional array: the value at the given data-point will be incremented
     * @param functionalityValuePoints the id of the points for each axis/dimension of the multi-dimensional array at the given data-point to search for
     * @param axes                     the axis/dimension definitions of the multi-dimensional array
     * @param coordinates              [for recursion purpose; initially filled with 0s] the coordinates of the data-point in the multi-dimensional array: one coordinate is found and filled at each recursion
     * @param level                    [for recursion purpose; initially 0] the current level of the recursion: will fill the {@code coordinates} at index {@code level}
     */
    private void incrementValueForFunctionalityProperties(int[] values, List<String[]> functionalityValuePoints, List<AxisDTO> axes, int[] coordinates, int level) {
        if (level == axes.size()) {
            int flatIndex = flatIndex(axes, coordinates);
            values[flatIndex]++;
        } else {
            // "All"
            coordinates[level] = 0;
            incrementValueForFunctionalityProperties(values, functionalityValuePoints, axes, coordinates, level + 1);
            // Others, if any
            final String[] valuePoints = functionalityValuePoints.get(level);
            if (valuePoints != null) {
                for (String valuePointCode : valuePoints) {
                    coordinates[level] = indexOf(axes.get(level).getPoints(), valuePointCode);
                    incrementValueForFunctionalityProperties(values, functionalityValuePoints, axes, coordinates, level + 1);
                }
            }
        }
    }

    /**
     * @param points all the points of one axis of a multi-dimensional array
     * @param id     the id of the point to find on that axis
     * @return the index of the point found on that axis
     */
    int indexOf(List<AxisPointDTO> points, String id) {
        for (int i = 0; i < points.size(); i++) {
            if (id.equals(points.get(i).getId())) {
                return i;
            }
        }
        throw new NotGonnaHappenException("Ids are generated from the points, so it's unlikely an unknown id will be requested");
    }

    /**
     * @param axes        the dimensions of the multi-dimension array
     * @param coordinates coordinates in this multi-dimension array
     * @return the index where to find the data in the single-dimension array internally baking the multi-dimension array
     */
    int flatIndex(List<AxisDTO> axes, int[] coordinates) {
        int index = coordinates[0];
        int multiplier = 1;
        for (int i = 1; i < coordinates.length; ++i) {
            multiplier *= axes.get(i - 1).getPoints().size();
            index += coordinates[i] * multiplier;
        }
        return index;
    }

}
