package com.sprintstack;

import com.sprintstack.exception.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {
    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void invalidQueryValuesReturnAClientErrorWithoutImplementationDetails() {
        var exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("priority");

        var response = handler.handleTypeMismatch(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Invalid value for priority");
    }

    @Test
    void malformedBodiesReturnAConsistentClientError() {
        var response = handler.handleUnreadableBody(new HttpMessageNotReadableException("malformed"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Request body is invalid or contains an unsupported value");
    }
}
