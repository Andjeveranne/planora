package com.isi4.userservice.exception;

/**
 * Exception levée lors d'erreurs de validation métier
 */
public class ValidationException extends RuntimeException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}