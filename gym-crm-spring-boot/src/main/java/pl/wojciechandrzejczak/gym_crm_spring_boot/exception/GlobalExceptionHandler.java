package org.example.web.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage() != null
                                ? error.getDefaultMessage()
                                : "Invalid value"
                )
        );

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                status,
                "Request validation failed"
        );
        problem.setProperty("errors", errors);

        return handleExceptionInternal(
                ex, problem, headers, status, request
        );
    }

    @Override
    public ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                status,
                "Request body is missing or contains invalid JSON, "
                        + "field types, date format or enum values"
        );

        return handleExceptionInternal(
                ex, problem, headers, status, request
        );
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ProblemDetail> handleBadRequest(Exception ex) {
        return error(
                HttpStatus.BAD_REQUEST,
                "Request contains invalid values"
        );
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ProblemDetail> handleAuthentication(
            SecurityException ex
    ) {
        return error(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(
            NoSuchElementException ex
    ) {
        return error(
                HttpStatus.NOT_FOUND,
                "Requested resource was not found"
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleConflict(
            IllegalStateException ex
    ) {
        return error(
                HttpStatus.CONFLICT,
                "Operation conflicts with the current resource state"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
        log.error("Unhandled REST exception: {}", ex.getClass().getName());

        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred"
        );
    }

    private ResponseEntity<ProblemDetail> error(
            HttpStatus status,
            String detail
    ) {
        logError(status.value(), detail);
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(status, detail);

        return ResponseEntity.status(status).body(problem);
    }

    @Override
    public ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        logError(
                statusCode.value(),
                mvcErrorDescription(ex, statusCode)
        );

        return super.handleExceptionInternal(
                ex,
                body,
                headers,
                statusCode,
                request
        );
    }

    private String mvcErrorDescription(
            Exception ex,
            HttpStatusCode status
    ) {
        if (ex instanceof MethodArgumentNotValidException
                || ex instanceof HandlerMethodValidationException) {
            return "Request validation failed";
        }

        if (ex instanceof HttpMessageNotReadableException) {
            return "Request body is missing or invalid";
        }

        HttpStatus knownStatus = HttpStatus.resolve(status.value());

        return knownStatus != null
                ? knownStatus.getReasonPhrase()
                : "HTTP request processing failed";
    }

    private void logError(int status, String description) {
        if (status >= 500) {
            log.error(
                    "REST error status={} description={}",
                    status,
                    description
            );
        } else {
            log.warn(
                    "REST error status={} description={}",
                    status,
                    description
            );
        }
    }
}