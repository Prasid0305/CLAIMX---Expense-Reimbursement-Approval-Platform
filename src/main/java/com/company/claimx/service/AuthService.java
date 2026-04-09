package com.company.claimx.service;

import com.company.claimx.constants.ErrorMessageConstants;
import com.company.claimx.dto.request.LoginRequest;
import com.company.claimx.dto.response.LoginResponse;
import com.company.claimx.entity.User;
import com.company.claimx.exception.UserInactiveException;
import com.company.claimx.exception.UserNotFoundException;
import com.company.claimx.repository.UserRepository;
import com.company.claimx.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for handling user authentication.
 * Provides login functionality using Spring Security and JWT token generation.
 */
@Service
public class AuthService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Authenticates a user based on the provided login credentials.
     * This method performs the following steps:
     * @param loginRequest the login request containing user email and password
     * @return LoginResponse containing the JWT token, email, and role
     * @throws UserNotFoundException   if no user is found with the provided email
     * @throws UserInactiveException   if the user account is inactive
     * @throws BadCredentialsException if the email or password is invalid
     */
    public LoginResponse login(LoginRequest loginRequest)  {

        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest
                                    .getEmail(),
                            loginRequest.
                                    getPassword())
            );

            User user = userRepository.findByEmail((loginRequest.getEmail()))
                    .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND));
            if(!user.getIsActive()){
                throw new UserInactiveException(ErrorMessageConstants.USER_INACTIVE);
            }
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

            return new LoginResponse(
                    token,
                    user.getEmail(),
                    user.getRole().name()
            );


        }catch (BadCredentialsException e){
            throw new BadCredentialsException(ErrorMessageConstants.INVALID_CREDIENTIALS);
        }
    }

}
