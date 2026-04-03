package com.company.claimx.exception;

/**
 * handles ItemNotFound exception
 */
public class ItemNotFound extends RuntimeException {
    public ItemNotFound(String message) {
        super(message);
    }
}
