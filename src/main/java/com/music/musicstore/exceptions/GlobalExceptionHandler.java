package com.music.musicstore.exceptions;

import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the Music Store application
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        logger.error("Resource not found: {}", ex.getMessage(), ex);
        return createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, ex.getErrorCode());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(
            AuthenticationException ex, WebRequest request) {
        logger.error("Authentication failed: {}", ex.getMessage(), ex);
        return createErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, ex.getErrorCode());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(
            UnauthorizedException ex, WebRequest request) {
        logger.error("Unauthorized access: {}", ex.getMessage(), ex);
        return createErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN, ex.getErrorCode());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            ValidationException ex, WebRequest request) {
        logger.error("Validation error: {}", ex.getMessage(), ex);
        return createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, ex.getErrorCode());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRule(
            BusinessRuleException ex, WebRequest request) {
        logger.error("Business rule violation: {}", ex.getMessage(), ex);
        return createErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, ex.getErrorCode());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFound(
            UsernameNotFoundException ex, WebRequest request) {
        logger.error("Username not found: {}", ex.getMessage(), ex);
        return createErrorResponse("User not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        logger.error("Invalid argument: {}", ex.getMessage(), ex);
        return createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT");
    }

    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbort(ClientAbortException ex, WebRequest request, HttpServletResponse response) {
        logger.info("Client aborted connection: {}", ex.getMessage());
        // Do not write any response body
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(
            Exception ex, WebRequest request, HttpServletResponse response) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        // Avoid writing JSON if Content-Type is not JSON
        String contentType = response.getContentType();
        if (contentType != null && !contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            try {
                response.resetBuffer();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("");
                response.flushBuffer();
            } catch (IOException ignored) {}
            return null;
        }
        return createErrorResponse("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(
            String message, HttpStatus status, String errorCode) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("errorCode", errorCode);

        return new ResponseEntity<>(errorResponse, status);
    }
}
