package com.company.claimx.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * login request dto
 * contains credentials
 */

@Data

public class LoginRequest {

    @NotBlank(message = "email is required")
    @Email(message = "valid email only")
    private String email;

    @NotBlank(message = "password is required")
    private String password;


    public LoginRequest() {
        this.email = email;
        this.password = password;
    }



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
