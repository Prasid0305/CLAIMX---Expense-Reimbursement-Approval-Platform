package com.company.claimx.exception;

public class ItemDoesNotBelongToClaim extends RuntimeException {
    public ItemDoesNotBelongToClaim(String message) {
        super(message);
    }
}
