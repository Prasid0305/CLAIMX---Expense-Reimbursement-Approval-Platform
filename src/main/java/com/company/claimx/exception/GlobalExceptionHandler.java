package com.company.claimx.exception;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * Converts custom application exceptions into consistent HTTP responses
 * containing status, error, and message fields.
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles UserNotFoundException and returns HTTP 404 - not found
     * @param userNotFoundException the thrown exception
     * @return a response body with error details and HTTP 404 status
     */

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(UserNotFoundException userNotFoundException){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "User not found");
        errorResponse.put("message", userNotFoundException.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles UserInactiveException and returns HTTP 403 -forbidden.
     * @param userInactiveException the thrown exception
     * @return a response body with error details and HTTP 403 status
     */
    @ExceptionHandler(UserInactiveException.class)
    public ResponseEntity<Map<String, Object>> handleUserInactive(UserInactiveException userInactiveException){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("error", "User inactive");
        errorResponse.put("message", userInactiveException.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles BadCredentialsException and returns HTTP 403 forbidden.
     * @param badCredentialsException the thrown exception
     * @return a response body with error details and HTTP 403 status
     */

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> badCredentialsException(BadCredentialsException badCredentialsException){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("error", "Bad credentials");
        errorResponse.put("message", badCredentialsException.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }


    /**
     * Handles UnauthorizedAccessException and returns HTTP 403 forbidden
     * @param unauthorizedAccessException the thrown exception
     * @return a response body with error details and HTTP 403 status
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Map<String, Object>> unauthorizedAccessException(UnauthorizedAccessException unauthorizedAccessException){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("error", "unauthorized Access");
        errorResponse.put("message", unauthorizedAccessException.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }



    /**
     * Handles ClaimNotFoundException and returns HTTP 404 - not found
     * @param claimNotFoundException the thrown exception
     * @return a response body with error details and HTTP 404 status
     */
    @ExceptionHandler(ClaimNotFoundException.class)
    public ResponseEntity<Map<String, Object>> claimNotFoundException(ClaimNotFoundException claimNotFoundException){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Claim not found");
        errorResponse.put("message", claimNotFoundException.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles InvalidClaimStatus and returns HTTP 400 - bad request.
     * @param invalidClaimStatus the thrown exception
     * @return a response body with error details and HTTP 400 status
     */
    @ExceptionHandler(InvalidClaimStatus.class)
    public ResponseEntity<Map<String, Object>> invalidClaimStatus(InvalidClaimStatus invalidClaimStatus){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.BAD_REQUEST);
        errorResponse.put("error", "invalid claim status");
        errorResponse.put("message", invalidClaimStatus.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles ItemNotFound and returns HTTP 404 - not found.
     * @param itemNotFound the thrown exception
     * @return a response body with error details and HTTP 404 status
     */
    @ExceptionHandler(ItemNotFound.class)
    public ResponseEntity<Map<String, Object>> itemNotFound(ItemNotFound itemNotFound){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "item not found");
        errorResponse.put("message", itemNotFound.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


    /**
     * Handles ItemDoesNotBelongToClaim} and returns HTTP 404 not found
     * @param itemDoesNotBelongToClaim the thrown exception
     * @return a response body with error details and HTTP 404 status
     */
    @ExceptionHandler(ItemDoesNotBelongToClaim.class)
    public ResponseEntity<Map<String, Object>> itemDoesNotBelongToClaim(ItemDoesNotBelongToClaim itemDoesNotBelongToClaim){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "item does not belong to claim");
        errorResponse.put("message", itemDoesNotBelongToClaim.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, Object> errors = new HashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("message", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotAuthenticated.class)
    public ResponseEntity<Map<String, Object>> userNotAuthenticated(UserNotAuthenticated userNotAuthenticated){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("error", "User is not authenticated");
        errorResponse.put("message", userNotAuthenticated.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

}
