package com.company.claimx.controller;

import com.company.claimx.dto.request.LoginRequest;
import com.company.claimx.dto.response.LoginResponse;
import com.company.claimx.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication endpoints")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "user login",description = "authenticate the user and get jwt token to access the endpoints")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) throws IllegalAccessException {
        LoginResponse response = authService.login(loginRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
