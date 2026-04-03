package com.company.claimx.exception;
/**
 * handles ItemDoesNotBelongToClaim exception
 */
public class ItemDoesNotBelongToClaim extends RuntimeException {
    public ItemDoesNotBelongToClaim(String message) {
        super(message);
    }
}
