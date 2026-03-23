package com.company.claimx.exception;

public class ClaimNotFoundException extends RuntimeException {
    public ClaimNotFoundException(String message) {
        super(message);
    }
}
