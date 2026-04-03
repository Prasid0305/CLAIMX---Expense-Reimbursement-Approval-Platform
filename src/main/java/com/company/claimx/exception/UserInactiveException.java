package com.company.claimx.exception;
/**
 * handles UserInactiveException exception
 */
public class UserInactiveException extends RuntimeException{
    public UserInactiveException(String message) {
        super(message);
    }
}
