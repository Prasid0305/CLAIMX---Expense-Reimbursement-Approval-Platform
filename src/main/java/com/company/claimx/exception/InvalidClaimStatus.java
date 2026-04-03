package com.company.claimx.exception;
/**
 * handles InvalidClaimStatus exception
 */
public class InvalidClaimStatus extends RuntimeException {
    public InvalidClaimStatus(String message) {
        super(message);
    }
}
