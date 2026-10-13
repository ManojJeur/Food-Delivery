package com.fooddelivery.restaurant.exception;

import java.time.Instant;

import com.fooddelivery.restaurant.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RestaurantNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(RestaurantNotFoundException exception,
            HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), "RESTAURANT_NOT_FOUND", request);
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedOperationException exception,
            HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, exception.getMessage(), "UNAUTHORIZED_OPERATION", request);
    }

    @ExceptionHandler(InvalidRestaurantStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus(InvalidRestaurantStatusException exception,
            HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, exception.getMessage(), "INVALID_RESTAURANT_STATUS", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        FieldError fieldError = exception.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = fieldError == null ? "Request validation failed"
                : fieldError.getField() + ": " + fieldError.getDefaultMessage();
        return error(HttpStatus.BAD_REQUEST, message, "VALIDATION_FAILED", request);
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        String message = exception instanceof IllegalArgumentException ? exception.getMessage() : "Request body is invalid";
        return error(HttpStatus.BAD_REQUEST, message, "BAD_REQUEST", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DataIntegrityViolationException exception,
            HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "Restaurant data conflicts with a database constraint", "DATA_CONFLICT", request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleConcurrentUpdate(ObjectOptimisticLockingFailureException exception,
            HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "Restaurant changed while this request was being processed; reload and retry",
                "CONCURRENT_UPDATE", request);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, String code,
            HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(false, message, code, Instant.now(), request.getRequestURI()));
    }
}
