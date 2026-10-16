package com.fooddelivery.menu.exception;

import java.time.Instant;
import com.fooddelivery.menu.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
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
    @ExceptionHandler(MenuCategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> categoryNotFound(MenuCategoryNotFoundException exception,
            HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), "MENU_CATEGORY_NOT_FOUND", request);
    }

    @ExceptionHandler(MenuItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> itemNotFound(MenuItemNotFoundException exception,
            HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), "MENU_ITEM_NOT_FOUND", request);
    }

    @ExceptionHandler(MenuItemUnavailableException.class)
    public ResponseEntity<ErrorResponse> itemUnavailable(MenuItemUnavailableException exception,
            HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), "MENU_ITEM_UNAVAILABLE", request);
    }

    @ExceptionHandler(RestaurantNotFoundException.class)
    public ResponseEntity<ErrorResponse> restaurantNotFound(RestaurantNotFoundException exception,
            HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), "RESTAURANT_NOT_FOUND", request);
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    public ResponseEntity<ErrorResponse> forbidden(UnauthorizedOperationException exception,
            HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, exception.getMessage(), "UNAUTHORIZED_OPERATION", request);
    }

    @ExceptionHandler(RestaurantServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> restaurantServiceUnavailable(RestaurantServiceUnavailableException exception,
            HttpServletRequest request) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), "RESTAURANT_SERVICE_UNAVAILABLE", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        FieldError field = exception.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = field == null ? "Request validation failed" : field.getField() + ": " + field.getDefaultMessage();
        return error(HttpStatus.BAD_REQUEST, message, "VALIDATION_FAILED", request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> constraint(ConstraintViolationException exception,
            HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), "VALIDATION_FAILED", request);
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> badRequest(Exception exception, HttpServletRequest request) {
        String message = exception instanceof IllegalArgumentException ? exception.getMessage() : "Request body is invalid";
        return error(HttpStatus.BAD_REQUEST, message, "BAD_REQUEST", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> conflict(DataIntegrityViolationException exception,
            HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "Menu data conflicts with a database constraint", "DATA_CONFLICT", request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> concurrentUpdate(ObjectOptimisticLockingFailureException exception,
            HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "Menu data changed during this request; reload and retry",
                "CONCURRENT_UPDATE", request);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, String code,
            HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorResponse(false, message, code, Instant.now(),
                request.getRequestURI()));
    }
}
