package com.company.claimx.exception;

public class InvalidClaimStatus extends RuntimeException {
    public InvalidClaimStatus(String message) {
        super(message);
    }
}
