package com.decathlon.ara.service;

import com.decathlon.ara.domain.Severity;
import com.decathlon.ara.repository.SeverityRepository;
import com.decathlon.ara.service.dto.severity.SeverityDTO;
import com.decathlon.ara.service.mapper.SeverityMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SeverityServiceTest {

    @Mock
    private SeverityRepository severityRepository;

    @Mock
    private SeverityMapper severityMapper;

    @InjectMocks
    private SeverityService cut;

    @Test
    public void getSeveritiesWithAll_ShouldCallRepositoryFindAllByOrderByPosition_WhenCalled() {
        // GIVEN
        long aProjectId = 42;

        // WHEN
        cut.getSeveritiesWithAll(aProjectId);

        // THEN
        verify(severityRepository, only()).findAllByProjectIdOrderByPosition(aProjectId);
    }

    @Test
    public void getSeveritiesWithAll_ShouldAddTheAllSpecialSeverity_WhenCalled() {
        // GIVEN
        long aProjectId = 42;
        List<Severity> severities = new ArrayList<>();
        severities.add(new Severity());
        when(severityRepository.findAllByProjectIdOrderByPosition(aProjectId)).thenReturn(severities);

        // WHEN
        cut.getSeveritiesWithAll(aProjectId);

        // THEN
        assertThat(severities.get(1)).isSameAs(Severity.ALL);
    }

    @Test
    public void getSeveritiesWithAll_ShouldCallMapperWithResultOfRepositoryFindAllByOrderByPosition_WhenCalled() {
        // GIVEN
        long aProjectId = 42;
        List<Severity> severities = new ArrayList<>();
        when(severityRepository.findAllByProjectIdOrderByPosition(aProjectId)).thenReturn(severities);

        // WHEN
        cut.getSeveritiesWithAll(aProjectId);

        // THEN
        verify(severityMapper, only()).toDto(severities);
    }

    @Test
    public void getSeveritiesWithAll_ShouldReturnMapperResult_WhenCalled() {
        // GIVEN
        long aProjectId = 42;
        List<SeverityDTO> severityDTOs = Collections.singletonList(new SeverityDTO());
        when(severityMapper.toDto(anyCollection())).thenReturn(severityDTOs);

        // WHEN / THEN
        assertThat(cut.getSeveritiesWithAll(aProjectId)).isSameAs(severityDTOs);
    }

    @Test
    public void getDefaultSeverityCode_ShouldReturnTheDefaultOne_WhenOneIsDefinedAsDefault() {
        // GIVEN
        List<SeverityDTO> severities = Arrays.asList(
                new SeverityDTO().withCode("a"),
                new SeverityDTO().withCode("b").withDefaultOnMissing(true));

        // WHEN
        final String defaultSeverityCode = cut.getDefaultSeverityCode(severities);

        // THEN
        assertThat(defaultSeverityCode).isEqualTo("b");
    }

    @Test
    public void getDefaultSeverityCode_ShouldReturnNull_WhenNoSeverityIsDefinedAsDefault() {
        // GIVEN
        List<SeverityDTO> severities = Arrays.asList(
                new SeverityDTO().withCode("a"),
                new SeverityDTO().withCode("b"));

        // WHEN
        final String defaultSeverityCode = cut.getDefaultSeverityCode(severities);

        // THEN
        assertThat(defaultSeverityCode).isNull();
    }

}
