package com.linkedinsystem.userservice.Application.Exceptions;


import com.linkedinsystem.userservice.Application.Dto.Errors.ErrorResponse;
import com.linkedinsystem.userservice.Domain.Exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j // Add this annotation to enable logging
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Validation errors from Spring (ex: @NotNull, @NotBlank, @Size nos DTOs)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.error(
                "Request validation error : status={}, message={}, trace={}, datetime={}",
                ex.getStatusCode(),
                ex.getMessage(),
                ex.getStackTrace(),
                Instant.now());

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request - Validation Error",
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 2. Deal with generic exceptions from business app (ex: IllegalArgumentException)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {

        log.error(
                "Business violation: message={}, trace={}, datetime={}",
                ex.getMessage(),
                ex.getStackTrace(),
                Instant.now());


        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 3. Custom exception from your business rules (optional - adapt if you have custom classes like ResourceNotFoundException)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {

        log.error(
                "Runtime Exception: message={}, trace={}, datetime={}",
                ex.getMessage(),
                ex.getStackTrace(),
                Instant.now());


        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Business Error",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 4. Generic exception handler for unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error(
                "Internal error in server: message={}, trace={}, datetime={}",
                ex.getMessage(),
                ex.getStackTrace(),
                Instant.now());


        ErrorResponse response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected internal server error happened"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.warn(
                "Business error: status={}, message={}, issuer={}, datetime={}",
                ex.getStatus().value(),
                ex.getMessage(),
                ex.getIssuer(),
                Instant.now());

        ErrorResponse response = new ErrorResponse(
                ex.getStatus().value(),
                "Business Rule Error",
                ex.getMessage()
        );

        return ResponseEntity.status(ex.getStatus()).body(response);
    }
}
