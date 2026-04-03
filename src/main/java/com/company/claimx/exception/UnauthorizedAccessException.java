package com.company.claimx.exception;
/**
 * handles UnauthorizedAccessException exception
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
