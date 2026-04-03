package com.company.claimx.exception;
/**
 * handles ClaimNotFoundException
 */
public class ClaimNotFoundException extends RuntimeException {
    public ClaimNotFoundException(String message) {
        super(message);
    }
}
