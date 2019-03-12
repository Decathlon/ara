package com.decathlon.ara.web.rest.advice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResponseDtoValidatorTest {

    @Mock
    private WebRequest request;

    @InjectMocks
    private ResponseDtoValidator cut;

    @Test
    public void getResourceName_ShouldReturnSubResourceOfProject_WhenUrlHasASubResourceOfProject() {
        // GIVEN
        when(request.getDescription(false))
                .thenReturn("uri=/api/projects/some-project/some-resources/api");

        // WHEN
        final String resourceName = cut.getResourceName(request);

        // THEN
        assertThat(resourceName).isEqualTo("some-resources");
    }

    @Test
    public void getResourceName_ShouldReturnProjects_WhenUrlIsAProjectUrl() {
        // GIVEN
        when(request.getDescription(false))
                .thenReturn("uri=/api/projects/some-project");

        // WHEN
        final String resourceName = cut.getResourceName(request);

        // THEN
        assertThat(resourceName).isEqualTo("projects");
    }

}
