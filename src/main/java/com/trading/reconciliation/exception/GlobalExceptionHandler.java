package com.trading.reconciliation.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST API error responses
 * 
 * This class implements centralized exception handling for all controllers in the application.
 * It ensures consistent error responses are returned to clients and provides proper logging.
 * 
 * The handler uses Spring's @ControllerAdvice to intercept exceptions from all controllers
 * and transforms them into standardized error responses with appropriate HTTP status codes.
 * 
 * Each error response includes:
 * - HTTP status code
 * - Error type
 * - Detailed error message
 * - Timestamp of when the error occurred
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Handle validation errors from request data
     * 
     * This method handles exceptions thrown when request data fails validation
     * constraints. It extracts the field-specific validation errors and returns
     * them in a structured format.
     * 
     * @param ex The validation exception containing field errors
     * @return ResponseEntity with HTTP 400 (Bad Request) and error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation error",
                errors.toString(),
                LocalDateTime.now()
        );
        
        log.error("Validation error: {}", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Handle general exceptions not handled by specific handlers
     * 
     * This method acts as a catch-all for any exceptions not handled by
     * more specific exception handlers. It returns a generic error response
     * with HTTP 500 (Internal Server Error).
     * 
     * In a production environment, this should be enhanced to:
     * - Avoid exposing internal error details to clients
     * - Send notifications for critical errors
     * - Provide a stable error response for the client
     * 
     * @param ex The exception to handle
     * @return ResponseEntity with HTTP 500 (Internal Server Error) and error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralExceptions(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                ex.getMessage(),
                LocalDateTime.now()
        );
        
        log.error("Internal server error", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Error response DTO for API error responses
     * 
     * This class represents the structure of error responses sent to API clients.
     * It includes all necessary information for clients to understand and handle the error.
     */
    public static class ErrorResponse {
        private final int status;
        private final String error;
        private final String message;
        private final LocalDateTime timestamp;
        
        public ErrorResponse(int status, String error, String message, LocalDateTime timestamp) {
            this.status = status;
            this.error = error;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        public int getStatus() {
            return status;
        }
        
        public String getError() {
            return error;
        }
        
        public String getMessage() {
            return message;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
} 