package com.decathlon.ara.service.exception;

import com.decathlon.ara.Entities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ForbiddenExceptionTest {

    @Test
    void getMessage_returnMessage_whenNoContextGiven() {
        // Given
        var exception = new ForbiddenException("some_resource", "some actions");

        // When

        // Then
        assertThat(exception.getMessage()).isEqualTo("You are not allowed to perform this action (some actions) on this resource (some_resource).");
        assertThat(exception.getResourceName()).isEqualTo(Entities.SECURITY);
        assertThat(exception.getErrorKey()).isEqualTo("forbidden");
    }

    @Test
    void getMessage_returnMessage_whenSomeContextGiven() {
        // Given
        var exception = new ForbiddenException("some_resource", "some actions",
                Pair.of("context-name-1", "context-value-1"),
                Pair.of("context-name-2", "context-value-2"),
                Pair.of("context-name-3", "context-value-3")
        );

        // When

        // Then
        assertThat(exception.getMessage()).isEqualTo(
                "You are not allowed to perform this action (some actions) on this resource (some_resource)." +
                        "\nContext:" +
                        "\n[context-name-1]: 'context-value-1'" +
                        "\n[context-name-2]: 'context-value-2'" +
                        "\n[context-name-3]: 'context-value-3'"
        );
        assertThat(exception.getResourceName()).isEqualTo(Entities.SECURITY);
        assertThat(exception.getErrorKey()).isEqualTo("forbidden");
    }
}
