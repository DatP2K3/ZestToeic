package com.zest.toeic.shared.exception;

import com.zest.toeic.shared.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MDC.clear();
        MDC.put("traceId", "test-trace-id");
    }

    @Test
    void handleNotFound_Returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("NOT_FOUND", response.getBody().getCode());
        assertEquals("Not found", response.getBody().getMessage());
        assertEquals("test-trace-id", response.getBody().getTraceId());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleDuplicate_Returns409() {
        DuplicateResourceException ex = new DuplicateResourceException("Duplicate");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicate(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("DUPLICATE", response.getBody().getCode());
        assertEquals("Duplicate", response.getBody().getMessage());
    }

    @Test
    void handleBadRequest_Returns400() {
        BadRequestException ex = new BadRequestException("Bad request");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("BAD_REQUEST", response.getBody().getCode());
        assertEquals("Bad request", response.getBody().getMessage());
    }

    @Test
    void handleUnauthorized_Returns401() {
        UnauthorizedException ex = new UnauthorizedException("Unauthorized");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUnauthorized(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("UNAUTHORIZED", response.getBody().getCode());
        assertEquals("Unauthorized", response.getBody().getMessage());
    }

    @Test
    void handleValidation_Returns400WithDetails() {
        MethodParameter parameter = mock(MethodParameter.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("object", "field1", "Error 1"),
                new FieldError("object", "field2", "Error 2")
        ));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().getCode());
        assertEquals(2, response.getBody().getDetails().size());
        assertEquals("field1", response.getBody().getDetails().get(0).getField());
        assertEquals("Error 1", response.getBody().getDetails().get(0).getMessage());
    }

    @Test
    void handleGeneric_Returns500() {
        when(request.getRequestURI()).thenReturn("/api/test");
        Exception ex = new Exception("Unexpected error");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneric(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_ERROR", response.getBody().getCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}
