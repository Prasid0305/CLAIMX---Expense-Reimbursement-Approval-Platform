package com.company.claimx.exception;

import com.company.claimx.constants.ErrorMessageConstants;
import com.company.claimx.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(UserNotFoundException userNotFoundException){

        ApiResponse<Void> errorResponse = ApiResponse.error(userNotFoundException.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles UserInactiveException and returns HTTP 403 -forbidden.
     * @param userInactiveException the thrown exception
     * @return a response body with error details and HTTP 403 status
     */
    @ExceptionHandler(UserInactiveException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserInactive(UserInactiveException userInactiveException){

        ApiResponse<Void> errorResponse = ApiResponse.error(userInactiveException.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles BadCredentialsException and returns HTTP 403 forbidden.
     * @param badCredentialsException the thrown exception
     * @return a response body with error details and HTTP 403 status
     */

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> badCredentialsException(BadCredentialsException badCredentialsException){


        ApiResponse<Void> errorResponse = ApiResponse.error(badCredentialsException.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }


    /**
     * Handles UnauthorizedAccessException and returns HTTP 403 forbidden
     * @param unauthorizedAccessException the thrown exception
     * @return a response body with error details and HTTP 403 status
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<Void>> unauthorizedAccessException(UnauthorizedAccessException unauthorizedAccessException){

        ApiResponse<Void> errorResponse = ApiResponse.error(unauthorizedAccessException.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }



    /**
     * Handles ClaimNotFoundException and returns HTTP 404 - not found
     * @param claimNotFoundException the thrown exception
     * @return a response body with error details and HTTP 404 status
     */
    @ExceptionHandler(ClaimNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> claimNotFoundException(ClaimNotFoundException claimNotFoundException){

        ApiResponse<Void> errorResponse = ApiResponse.error(claimNotFoundException.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles InvalidClaimStatus and returns HTTP 400 - bad request.
     * @param invalidClaimStatus the thrown exception
     * @return a response body with error details and HTTP 400 status
     */
    @ExceptionHandler(InvalidClaimStatus.class)
    public ResponseEntity<ApiResponse<Void>> invalidClaimStatus(InvalidClaimStatus invalidClaimStatus){


        ApiResponse<Void> errorResponse = ApiResponse.error(invalidClaimStatus.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles ItemNotFound and returns HTTP 404 - not found.
     * @param itemNotFound the thrown exception
     * @return a response body with error details and HTTP 404 status
     */
    @ExceptionHandler(ItemNotFound.class)
    public ResponseEntity<ApiResponse<Void>> itemNotFound(ItemNotFound itemNotFound){


        ApiResponse<Void> errorResponse = ApiResponse.error(itemNotFound.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


    /**
     * Handles ItemDoesNotBelongToClaim} and returns HTTP 404 not found
     * @param itemDoesNotBelongToClaim the thrown exception
     * @return a response body with error details and HTTP 404 status
     */
    @ExceptionHandler(ItemDoesNotBelongToClaim.class)
    public ResponseEntity<ApiResponse<Void>> itemDoesNotBelongToClaim(ItemDoesNotBelongToClaim itemDoesNotBelongToClaim){


        ApiResponse<Void> errorResponse = ApiResponse.error(itemDoesNotBelongToClaim.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value"
                ));

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Validation failed")
                .data(fieldErrors)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotAuthenticated.class)
    public ResponseEntity<ApiResponse<Void>> userNotAuthenticated(UserNotAuthenticated userNotAuthenticated){

        ApiResponse<Void> errorResponse = ApiResponse.error(ErrorMessageConstants.USER_NOT_AUTHENTICATED);

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

}
