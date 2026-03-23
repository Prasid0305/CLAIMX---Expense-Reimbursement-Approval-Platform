package com.company.claimx.controller;

import com.company.claimx.dto.request.ApproveClaimRequest;
import com.company.claimx.dto.request.RejectClaimRequest;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.service.ClaimService;
import com.company.claimx.service.ManagerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager/claims")
@Tag(name = "manager approval", description = "Review and approve/reject claims")
public class ManagerController {

    @Autowired
    private ManagerService managerService;



    @GetMapping("/pending")
    public ResponseEntity<List<ClaimResponse>> getPendingClaims(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userEmail = authentication.getName();

        List<ClaimResponse> claims = managerService.getPendingClaims(userEmail);
        return ResponseEntity.ok(claims);
    }
    @GetMapping("/pending/{claimId}")
    public ResponseEntity<ClaimResponse> getPendingClaimById(@PathVariable Long claimId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userEmail = authentication.getName();

        ClaimResponse response = managerService.getPendingClaimById(claimId, userEmail);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/pending/{claimId}/approve")
    public ResponseEntity<ClaimResponse> approvePendingClaimById(@PathVariable Long claimId, @Valid @RequestBody ApproveClaimRequest approveClaimRequest){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userEmail = authentication.getName();

        ClaimResponse response = managerService.approvePendingClaimById(claimId, approveClaimRequest, userEmail);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/pending/{claimId}/reject")
    public ResponseEntity<ClaimResponse> rejectPendingClaimById(@PathVariable Long claimId, @Valid @RequestBody RejectClaimRequest rejectClaimRequest){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userEmail = authentication.getName();

        ClaimResponse response = managerService.rejectPendingClaimById(claimId, rejectClaimRequest, userEmail);

        return ResponseEntity.ok(response);
    }
}
