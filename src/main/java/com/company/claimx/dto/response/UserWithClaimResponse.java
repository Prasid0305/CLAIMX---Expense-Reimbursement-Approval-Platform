package com.company.claimx.dto.response;

import com.company.claimx.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * response dto for user with claim response
 * contains user details and also the claims that belongs to the claim
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserWithClaimResponse {

    private Long userId;
    private String employeeCode;
    private String name;
    private String email;
    private UserRole role;
    private Boolean isActive;
    private LocalDateTime createdAt;

    private List<ClaimResponse> claims;


}
