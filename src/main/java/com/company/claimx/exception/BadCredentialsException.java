package com.company.claimx.exception;

public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException(String message) {
        super(message);
    }
}
