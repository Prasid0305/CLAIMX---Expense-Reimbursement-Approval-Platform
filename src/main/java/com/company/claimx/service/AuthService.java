package com.company.claimx.service;

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

@Service
public class AuthService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

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
                    .orElseThrow(()->new UserNotFoundException("user not found"));
            if(!user.getIsActive()){
                throw new UserInactiveException("user inactive");
            }
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

            return new LoginResponse(
                    token,
                    user.getEmail(),
                    user.getRole().name()
            );


        }catch (BadCredentialsException e){
            throw new BadCredentialsException("invalid email or password ");  //i will add a custom exception
        }


    }

}
