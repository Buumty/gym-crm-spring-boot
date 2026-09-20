package pl.wojciechandrzejczak.gym_crm_spring_boot.web.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import pl.wojciechandrzejczak.gym_crm_spring_boot.exception.GlobalExceptionHandler;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    @SuppressWarnings("unchecked")
    void shouldHandleMethodArgumentNotValid() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult =
                mock(BindingResult.class);

        FieldError firstNameError =
                new FieldError(
                        "request",
                        "firstName",
                        "must not be blank"
                );

        FieldError lastNameError =
                new FieldError(
                        "request",
                        "lastName",
                        null,
                        false,
                        null,
                        null,
                        null
                );

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(
                        List.of(
                                firstNameError,
                                lastNameError
                        )
                );

        ResponseEntity<Object> response =
                handler.handleMethodArgumentNotValid(
                        exception,
                        new HttpHeaders(),
                        HttpStatus.BAD_REQUEST,
                        webRequest()
                );

        assert response != null;
        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertInstanceOf(
                ProblemDetail.class,
                response.getBody()
        );

        ProblemDetail problem =
                (ProblemDetail) response.getBody();

        assertEquals(
                400,
                problem.getStatus()
        );

        assertEquals(
                "Request validation failed",
                problem.getDetail()
        );

        assertNotNull(
                problem.getProperties()
        );

        Map<String, String> errors =
                (Map<String, String>)
                        problem.getProperties()
                                .get("errors");

        assertNotNull(errors);

        assertEquals(
                "must not be blank",
                errors.get("firstName")
        );

        // defaultMessage == null
        assertEquals(
                "Invalid value",
                errors.get("lastName")
        );
    }


    @Test
    void shouldHandleHttpMessageNotReadable() {
        HttpMessageNotReadableException exception =
                mock(HttpMessageNotReadableException.class);

        ResponseEntity<Object> response =
                handler.handleHttpMessageNotReadable(
                        exception,
                        new HttpHeaders(),
                        HttpStatus.BAD_REQUEST,
                        webRequest()
                );

        assert response != null;
        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        ProblemDetail problem =
                (ProblemDetail) response.getBody();

        assertNotNull(problem);

        assertEquals(
                400,
                problem.getStatus()
        );

        assertEquals(
                "Request body is missing or contains invalid JSON, "
                        + "field types, date format or enum values",
                problem.getDetail()
        );
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        ResponseEntity<ProblemDetail> response =
                handler.handleBadRequest(
                        new IllegalArgumentException(
                                "invalid"
                        )
                );

        assertProblem(
                response,
                HttpStatus.BAD_REQUEST,
                "Request contains invalid values"
        );
    }

    @Test
    void shouldHandleSecurityException() {
        ResponseEntity<ProblemDetail> response =
                handler.handleAuthentication(
                        new SecurityException(
                                "invalid credentials"
                        )
                );

        assertProblem(
                response,
                HttpStatus.UNAUTHORIZED,
                "Invalid credentials"
        );
    }


    @Test
    void shouldHandleNoSuchElementException() {
        ResponseEntity<ProblemDetail> response =
                handler.handleNotFound(
                        new NoSuchElementException(
                                "not found"
                        )
                );

        assertProblem(
                response,
                HttpStatus.NOT_FOUND,
                "Requested resource was not found"
        );
    }


    @Test
    void shouldHandleIllegalStateException() {
        ResponseEntity<ProblemDetail> response =
                handler.handleConflict(
                        new IllegalStateException(
                                "already active"
                        )
                );

        assertProblem(
                response,
                HttpStatus.CONFLICT,
                "Operation conflicts with the current resource state"
        );
    }


    @Test
    void shouldHandleUnexpectedException() {
        ResponseEntity<ProblemDetail> response =
                handler.handleUnexpected(
                        new RuntimeException(
                                "unexpected"
                        )
                );

        assertProblem(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred"
        );
    }



    @Test
    void shouldHandleHandlerMethodValidationExceptionInternally() {
        HandlerMethodValidationException exception =
                mock(HandlerMethodValidationException.class);

        ProblemDetail body =
                ProblemDetail.forStatus(
                        HttpStatus.BAD_REQUEST
                );

        ResponseEntity<Object> response =
                handler.handleExceptionInternal(
                        exception,
                        body,
                        new HttpHeaders(),
                        HttpStatus.BAD_REQUEST,
                        webRequest()
                );

        assert response != null;
        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertSame(
                body,
                response.getBody()
        );
    }

    @Test
    void shouldHandleKnownHttpStatusInternally() {
        Exception exception =
                new Exception("test");

        ProblemDetail body =
                ProblemDetail.forStatus(
                        HttpStatus.NOT_FOUND
                );

        ResponseEntity<Object> response =
                handler.handleExceptionInternal(
                        exception,
                        body,
                        new HttpHeaders(),
                        HttpStatus.NOT_FOUND,
                        webRequest()
                );

        assert response != null;
        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertSame(
                body,
                response.getBody()
        );
    }

    @Test
    void shouldHandleUnknownHttpStatusInternally() {
        Exception exception =
                new Exception("test");

        HttpStatusCode unknownStatus =
                HttpStatusCode.valueOf(499);

        ProblemDetail body =
                ProblemDetail.forStatus(
                        unknownStatus
                );

        ResponseEntity<Object> response =
                handler.handleExceptionInternal(
                        exception,
                        body,
                        new HttpHeaders(),
                        unknownStatus,
                        webRequest()
                );

        assert response != null;
        assertEquals(
                499,
                response.getStatusCode().value()
        );

        assertSame(
                body,
                response.getBody()
        );
    }

    @Test
    void shouldHandleServerErrorInternally() {
        Exception exception =
                new RuntimeException(
                        "server error"
                );

        ProblemDetail body =
                ProblemDetail.forStatus(
                        HttpStatus.INTERNAL_SERVER_ERROR
                );

        ResponseEntity<Object> response =
                handler.handleExceptionInternal(
                        exception,
                        body,
                        new HttpHeaders(),
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        webRequest()
                );

        assert response != null;
        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );
    }

    private WebRequest webRequest() {
        return new ServletWebRequest(
                new MockHttpServletRequest()
        );
    }

    private void assertProblem(
            ResponseEntity<ProblemDetail> response,
            HttpStatus expectedStatus,
            String expectedDetail
    ) {
        assertEquals(
                expectedStatus,
                response.getStatusCode()
        );

        assertNotNull(
                response.getBody()
        );

        assertEquals(
                expectedStatus.value(),
                response.getBody().getStatus()
        );

        assertEquals(
                expectedDetail,
                response.getBody().getDetail()
        );
    }
}