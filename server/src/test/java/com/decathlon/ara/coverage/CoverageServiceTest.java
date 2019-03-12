package com.decathlon.ara.coverage;

import com.decathlon.ara.repository.FunctionalityRepository;
import com.decathlon.ara.service.dto.coverage.AxisDTO;
import com.decathlon.ara.service.dto.coverage.AxisPointDTO;
import com.decathlon.ara.domain.Functionality;
import com.decathlon.ara.common.NotGonnaHappenException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CoverageServiceTest {

    private static final int A_PROJECT_ID = 42;

    @Mock
    private FunctionalityRepository functionalityRepository;

    @Mock
    private CountryAxisGenerator countryAxisGenerator;

    @Mock
    private SeverityAxisGenerator severityAxisGenerator;

    @Mock
    private TeamAxisGenerator teamAxisGenerator;

    @Mock
    private CoverageAxisGenerator coverageAxisGenerator;

    @Mock
    private AxisGenerator generator1;

    @Mock
    private AxisGenerator generator2;

    @InjectMocks
    private CoverageService cut;

    private static AxisDTO dimensionOfSize(int size) {
        List<AxisPointDTO> points = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            points.add(new AxisPointDTO());
        }
        return new AxisDTO().withPoints(points);
    }

    @Test
    public void getAllPoints_should_return_ALL_plus_the_points_of_the_generator() {
        // GIVEN
        when(generator1.getPoints(A_PROJECT_ID)).thenReturn(Stream.of(
                new AxisPointDTO().withId("Z"),
                new AxisPointDTO().withId("A")));

        // WHEN
        final List<AxisPointDTO> points = cut.getAllPoints(generator1, A_PROJECT_ID);

        // THEN
        assertThat(points.stream().map(AxisPointDTO::getId)).containsExactly("", "Z", "A");
    }

    @Test
    public void computeValues_should_increment_all_and_value_points() {
        // GIVEN
        final Functionality functionality = new Functionality();
        List<Functionality> functionalities = Collections.singletonList(functionality);
//        when(generator1.getPoints()).thenReturn(Stream.of(
//                new AxisPointDTO().withId("A"),
//                new AxisPointDTO().withId("B")));
//        when(generator2.getPoints()).thenReturn(Stream.of(
//                new AxisPointDTO().withId("1")));
        when(generator1.getValuePoints(functionality)).thenReturn(new String[] { "A" });
        when(generator2.getValuePoints(functionality)).thenReturn(new String[] {});
        List<AxisGenerator> axisGenerators = Arrays.asList(
                generator1,
                generator2);
        List<AxisDTO> axes = Arrays.asList(
                new AxisDTO().withPoints(Arrays.asList(
                        AxisPointDTO.ALL,
                        new AxisPointDTO().withId("A"),
                        new AxisPointDTO().withId("B"))),
                new AxisDTO().withPoints(Arrays.asList(
                        AxisPointDTO.ALL,
                        new AxisPointDTO().withId("1"))));

        // WHEN
        int[] values = cut.computeValues(functionalities, axes, axisGenerators);

        // THEN
        assertThat(values).containsExactly(
                1, // ALL, ALL
                1, // "A", ALL
                0,
                0,
                0,
                0);
    }

    @Test
    public void incrementValueForFunctionalityProperties_should_increment_all_and_value_points() {
        // GIVEN
        List<String[]> functionalityValuePoints = Arrays.asList(
                new String[] { "A" },
                new String[] { "1", "3" });
        List<AxisDTO> axes = Arrays.asList(
                new AxisDTO().withPoints(Arrays.asList(
                        AxisPointDTO.ALL,
                        new AxisPointDTO().withId("A"),
                        new AxisPointDTO().withId("B"))),
                new AxisDTO().withPoints(Arrays.asList(
                        AxisPointDTO.ALL,
                        new AxisPointDTO().withId("1"),
                        new AxisPointDTO().withId("2"),
                        new AxisPointDTO().withId("3"))));
        int[] values = new int[3 * 4];

        // WHEN
        cut.incrementValueForFunctionalityProperties(values, functionalityValuePoints, axes);

        // THEN
        assertThat(values).containsExactly(
                1, // ALL, ALL
                1, // "A", ALL
                0,
                1, // ALL, "1"
                1, // "A", "1"
                0,
                0,
                0,
                0,
                1, // ALL, "3"
                1, // "A", "3"
                0);
    }

    @Test
    public void incrementValueForFunctionalityProperties_should_increment_only_all_when_value_points_are_null() {
        // GIVEN
        List<String[]> functionalityValuePoints = Collections.singletonList(null);
        List<AxisDTO> axes = Collections.singletonList(
                new AxisDTO().withPoints(Arrays.asList(
                        AxisPointDTO.ALL,
                        new AxisPointDTO().withId("A"))));
        int[] values = new int[2];

        // WHEN
        cut.incrementValueForFunctionalityProperties(values, functionalityValuePoints, axes);

        // THEN
        assertThat(values).containsExactly(
                1, // ALL
                0 // "A"
        );
    }

    @Test
    public void indexOf_should_return_index_of_requested_id_at_start() {
        // GIVEN
        List<AxisPointDTO> points = Arrays.asList(
                new AxisPointDTO().withId("1"),
                new AxisPointDTO().withId("2"));

        // WHEN
        final int index = cut.indexOf(points, "1");

        // THEN
        assertThat(index).isEqualTo(0);
    }

    @Test
    public void indexOf_should_return_index_of_requested_id_in_middle() {
        // GIVEN
        List<AxisPointDTO> points = Arrays.asList(
                new AxisPointDTO().withId("1"),
                new AxisPointDTO().withId("2"),
                new AxisPointDTO().withId("3"));

        // WHEN
        final int index = cut.indexOf(points, "3");

        // THEN
        assertThat(index).isEqualTo(2);
    }

    // TODO flatIndex_should_throw_MultiArrayIndexOutOfBoundsException

    @Test(expected = NotGonnaHappenException.class)
    public void indexOf_should_throw_NotGonnaHappenException_on_unknown_id_which_will_assert_false() {
        // GIVEN
        List<AxisPointDTO> points = Collections.emptyList();

        // WHEN
        cut.indexOf(points, "404");
    }

    @Test
    public void flatIndex_should_work_with_one_dimension() {
        // GIVEN
        List<AxisDTO> axes = Collections.singletonList(
                dimensionOfSize(3));
        int[] coordinates = new int[] { 2 };

        // WHEN
        final int flatIndex = cut.flatIndex(axes, coordinates);

        // THEN
        assertThat(flatIndex).isEqualTo(2);
    }

    @Test
    public void flatIndex_should_work_with_two_dimensions() {
        // GIVEN
        // This example has no "duplicate" numbers:
        // 0,1: indices of dimensions
        // 2,3: accessed indices in the multi-dimensional array
        // 4,5: size of the dimensions
        List<AxisDTO> axes = Arrays.asList(
                dimensionOfSize(4),
                dimensionOfSize(5));
        int[] coordinates = new int[] { 2, 3 };

        // WHEN
        final int flatIndex = cut.flatIndex(axes, coordinates);

        // THEN
        // Stored in a single array as:
        // [ [a,b,c,d], [e,f,g,h], [i,j,k,l], [m,n,o,p], [q,r,s,t] ]
        // ________________________________________^________________
        // multiDimensionalArray[2][3] = 'o'
        assertThat(flatIndex).isEqualTo(14); // = (1) * 2 + (1*4) * 3
        // TODO The storage is quite illogical: the dimensions declaration is inverted
        // Need to change both service and client code
        // multiDimensionalArray[2] = [c,g,k,o,s]
    }

    @Test
    public void flatIndex_should_work_with_three_dimensions() {
        // GIVEN
        // This example has no "duplicate" numbers:
        // 0,1,2: indices of dimensions
        // 3,4,5: accessed indices in the multi-dimensional array
        // 6,7,8: size of the dimensions
        List<AxisDTO> axes = Arrays.asList(
                dimensionOfSize(6),
                dimensionOfSize(7),
                dimensionOfSize(8));
        int[] coordinates = new int[] { 3, 4, 5 };

        // WHEN
        final int flatIndex = cut.flatIndex(axes, coordinates);

        // THEN
        assertThat(flatIndex).isEqualTo(237); // = (1) * 3 + (1*6) * 4 + (1*6*7) * 5
    }

}
