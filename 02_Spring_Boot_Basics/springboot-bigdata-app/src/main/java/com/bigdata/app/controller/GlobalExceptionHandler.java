package com.bigdata.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the Big Data application
 * Demonstrates error handling best practices
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle validation errors for request body
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Validation failed");
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("fieldErrors", fieldErrors);
        
        logger.warn("Validation error: {}", fieldErrors);
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle constraint violation for path variables and request parameters
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        Map<String, String> violations = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            violations.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Constraint violation");
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("violations", violations);
        
        logger.warn("Constraint violation: {}", violations);
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle database access errors
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccessException(DataAccessException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Database operation failed");
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("message", "Please try again later");
        
        logger.error("Database error occurred", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid argument");
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("message", ex.getMessage());
        
        logger.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Internal server error");
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("message", "An unexpected error occurred");
        
        logger.error("Unexpected error occurred", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
