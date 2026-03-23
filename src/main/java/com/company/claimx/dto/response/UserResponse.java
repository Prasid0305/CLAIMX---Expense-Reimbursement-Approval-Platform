package com.company.claimx.dto.response;

import com.company.claimx.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String employeeCode;
    private String name;
    private String email;
    private UserRole role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
