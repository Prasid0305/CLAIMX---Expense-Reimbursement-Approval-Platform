package com.company.claimx.exception;
/**
 * handles UserNotFoundException exception
 */
public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String message) {
        super(message);
    }
}
