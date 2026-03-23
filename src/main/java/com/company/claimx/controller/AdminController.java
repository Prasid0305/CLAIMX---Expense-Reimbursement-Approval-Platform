package com.company.claimx.controller;

import com.company.claimx.dto.response.AuditLogResponse;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.dto.response.UserResponse;
import com.company.claimx.dto.response.UserWithClaimResponse;
import com.company.claimx.entity.User;
import com.company.claimx.service.AdminServices;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin operations", description = "Admin operation endpoints")

public class AdminController {

    @Autowired
    AdminServices adminServices;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        List<UserResponse> claimResponseList = adminServices.getAllUsers(userEmail);

        return ResponseEntity.ok(claimResponseList);
    }

    @GetMapping("/claims")
    public ResponseEntity<List<ClaimResponse>> getAllClaims(){

        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        List<ClaimResponse> claimResponseList = adminServices.getAllClaims(userEmail);

        return ResponseEntity.ok(claimResponseList);

    }

    @GetMapping("/audit-log")

    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(){

        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        List<AuditLogResponse> auditLogResponsesList = adminServices.getAllAuditLogs(userEmail);
        return ResponseEntity.ok(auditLogResponsesList);

    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserWithClaimResponse> getUserWithClaims(@Parameter @PathVariable  Long userId){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        UserWithClaimResponse response = adminServices.getUserWithClaims(userId);
        return ResponseEntity.ok(response);
    }
}
