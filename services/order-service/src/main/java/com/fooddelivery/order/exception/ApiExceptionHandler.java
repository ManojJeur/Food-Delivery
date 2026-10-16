package com.fooddelivery.order.exception;
import java.time.Instant;
import com.fooddelivery.order.dto.ErrorResponse;
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
    @ExceptionHandler({OrderNotFoundException.class, RestaurantNotFoundException.class, CartNotFoundException.class})
    public ResponseEntity<ErrorResponse> notFound(RuntimeException ex, HttpServletRequest req) {
        String code = ex instanceof OrderNotFoundException ? "ORDER_NOT_FOUND"
                : ex instanceof RestaurantNotFoundException ? "RESTAURANT_NOT_FOUND" : "CART_ITEM_NOT_FOUND";
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), code, req);
    }
    @ExceptionHandler(UnauthorizedOperationException.class)
    public ResponseEntity<ErrorResponse> forbidden(UnauthorizedOperationException ex, HttpServletRequest req) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage(), "UNAUTHORIZED_OPERATION", req);
    }
    @ExceptionHandler(UpstreamServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> upstream(UpstreamServiceUnavailableException ex, HttpServletRequest req) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), "UPSTREAM_SERVICE_UNAVAILABLE", req);
    }
    @ExceptionHandler({CartRestaurantMismatchException.class, EmptyCartException.class, CartChangedException.class,
            IdempotencyKeyConflictException.class, MenuItemsUnavailableException.class, RestaurantUnavailableException.class,
            InvalidOrderTransitionException.class})
    public ResponseEntity<ErrorResponse> businessConflict(RuntimeException ex, HttpServletRequest req) {
        String code = ex instanceof CartRestaurantMismatchException ? "CART_RESTAURANT_MISMATCH"
                : ex instanceof EmptyCartException ? "EMPTY_CART"
                : ex instanceof CartChangedException ? "CART_CHANGED"
                : ex instanceof IdempotencyKeyConflictException ? "IDEMPOTENCY_KEY_REUSED"
                : ex instanceof MenuItemsUnavailableException ? "MENU_ITEM_UNAVAILABLE"
                : ex instanceof RestaurantUnavailableException ? "RESTAURANT_CLOSED" : "INVALID_ORDER_TRANSITION";
        return error(HttpStatus.CONFLICT, ex.getMessage(), code, req);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        FieldError field = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = field == null ? "Request validation failed" : field.getField() + ": " + field.getDefaultMessage();
        return error(HttpStatus.BAD_REQUEST, message, "VALIDATION_FAILED", req);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> constraint(ConstraintViolationException ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), "VALIDATION_FAILED", req);
    }
    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> badRequest(Exception ex, HttpServletRequest req) {
        String message = ex instanceof IllegalArgumentException ? ex.getMessage() : "Request body is invalid";
        return error(HttpStatus.BAD_REQUEST, message, "BAD_REQUEST", req);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> dataConflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, "Order data conflicts with a database constraint", "DATA_CONFLICT", req);
    }
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> concurrentUpdate(ObjectOptimisticLockingFailureException ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, "Order changed while this request was processing; reload and retry", "CONCURRENT_UPDATE", req);
    }
    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, String code, HttpServletRequest req) {
        return ResponseEntity.status(status).body(new ErrorResponse(false, message, code, Instant.now(), req.getRequestURI()));
    }
}
