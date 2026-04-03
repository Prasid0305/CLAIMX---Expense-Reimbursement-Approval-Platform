package com.company.claimx.dto.response;


import lombok.*;

/**
 * response dto to login response
 *
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String email;
    private String role;



}
