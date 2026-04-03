package com.company.claimx.exception;

/**
 * handles BadCredentialsException
 */
public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException(String message) {
        super(message);
    }
}
