package com.company.claimx.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(UserNotFoundException userNotFoundException){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "User not found");
        errorResponse.put("message", userNotFoundException.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserInactiveException.class)
    public ResponseEntity<Map<String, Object>> handleUserInactive(UserInactiveException userInactiveException){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("error", "User inactive");
        errorResponse.put("message", userInactiveException.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> badCredentialsException(BadCredentialsException badCredentialsException){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("error", "bad credentials");
        errorResponse.put("message", badCredentialsException.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Map<String, Object>> unauthorizedAccessException(UnauthorizedAccessException unauthorizedAccessException){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("error", "unauthorized Access");
        errorResponse.put("message", unauthorizedAccessException.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ClaimNotFoundException.class)
    public ResponseEntity<Map<String, Object>> claimNotFoundException(ClaimNotFoundException claimNotFoundException){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "claim not found");
        errorResponse.put("message", claimNotFoundException.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(InvalidClaimStatus.class)
    public ResponseEntity<Map<String, Object>> invalidClaimStatus(InvalidClaimStatus invalidClaimStatus){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.BAD_REQUEST);
        errorResponse.put("error", "invalid claim status");
        errorResponse.put("message", invalidClaimStatus.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ItemNotFound.class)
    public ResponseEntity<Map<String, Object>> itemNotFound(ItemNotFound itemNotFound){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "item not found");
        errorResponse.put("message", itemNotFound.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(ItemDoesNotBelongToClaim.class)
    public ResponseEntity<Map<String, Object>> itemDoesNotBelongToClaim(ItemDoesNotBelongToClaim itemDoesNotBelongToClaim){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "item does not belong to claim");
        errorResponse.put("message", itemDoesNotBelongToClaim.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


}
