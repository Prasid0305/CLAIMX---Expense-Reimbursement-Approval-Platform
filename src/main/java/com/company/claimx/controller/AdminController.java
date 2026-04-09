package com.company.claimx.controller;

import com.company.claimx.annotation.Authenticated;
import com.company.claimx.context.AuthenticationContext;
import com.company.claimx.dto.response.AuditLogResponse;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.dto.response.UserResponse;
import com.company.claimx.dto.response.UserWithClaimResponse;
import com.company.claimx.entity.User;
import com.company.claimx.service.AdminServices;
import io.swagger.v3.oas.annotations.Operation;
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

/**
 * Controller class for admin operations.
 * Provides endpoints to manage users, claims, and audit logs.
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin operations", description = "Admin operation endpoints. To get all the users, claims, audit logs, and user info along with the claim information.")

public class AdminController {

    @Autowired
    AdminServices adminServices;

    /**
     * endpoint to get all users
     *
     * @return list of all users
     */
    @Operation(summary = "Retriever all the users",description = "Admin gets all the users with their details")
    @GetMapping("/users")
    @Authenticated(roles = {"ADMIN"})
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = AuthenticationContext.getUserEmail();

        List<UserResponse> claimResponseList = adminServices.getAllUsers(userEmail);

        return ResponseEntity.ok(claimResponseList);
    }

    /**
     * endpoint to get all claims of all users
     *
     * @return list of all claims
     */
    @Operation(summary = "Retriever all the claims of the users",description = "Admin gets all the users with their details")
    @GetMapping("/claims")
    @Authenticated(roles = {"ADMIN"})
    public ResponseEntity<List<ClaimResponse>> getAllClaims(){


        String userEmail = AuthenticationContext.getUserEmail();

        List<ClaimResponse> claimResponseList = adminServices.getAllClaims(userEmail);

        return ResponseEntity.ok(claimResponseList);

    }

    /**
     * endpoint to get all audit logs
     *
     * @return list of audit logs
     */
    @Operation(summary = "Retriever all the audit logs",description = "Admin gets all the log trails details")
    @GetMapping("/audit-log")
    @Authenticated(roles = {"ADMIN"})

    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(){


        String userEmail = AuthenticationContext.getUserEmail();

        List<AuditLogResponse> auditLogResponsesList = adminServices.getAllAuditLogs(userEmail);
        return ResponseEntity.ok(auditLogResponsesList);

    }

    /**
     * endpoint to get a particular user's details along with their claims
     *
     * @param userId - id of the user
     * @return user details with claims
     */
    @Operation(summary = "Retriever all the user with the claims",description = "Admin gets the users with all claims which belongs to the particular user")
    @GetMapping("/users/{userId}")
    @Authenticated(roles = {"ADMIN"})
    public ResponseEntity<UserWithClaimResponse> getUserWithClaims(@Parameter @PathVariable  Long userId){

        String userEmail = AuthenticationContext.getUserEmail();

        UserWithClaimResponse response = adminServices.getUserWithClaims(userId);
        return ResponseEntity.ok(response);
    }
}
