package com.company.claimx.controller;


import com.company.claimx.config.SecurityConfig;
import com.company.claimx.dto.request.CreateClaimRequest;
import com.company.claimx.dto.request.UpdateClaimRequest;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.enums.ClaimStatus;
import com.company.claimx.service.ClaimService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@Tag(name = "Employee claims", description = "claim management for the employees")
public class ClaimController {

    @Autowired
    private ClaimService claimService;

    @PostMapping
    public ResponseEntity<ClaimResponse> createClaim(@Valid @RequestBody CreateClaimRequest createClaimRequest){
        //to get authenticated email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email =authentication.getName();
        ClaimResponse claimResponse = claimService.createClaim(createClaimRequest, email);

        return new ResponseEntity<>(claimResponse, HttpStatus.CREATED);



    }

    @GetMapping("/{claimId}")
    public ResponseEntity<ClaimResponse> getClaimById(@PathVariable Long claimId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userEmail = authentication.getName();

        ClaimResponse response = claimService.getClaimById(claimId, userEmail);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/{claimId}/submit")
    public ResponseEntity<ClaimResponse> submitClaim(@PathVariable Long claimId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userEmail = authentication.getName();

        ClaimResponse claimResponse = claimService.submitClaim(claimId, userEmail);

        return ResponseEntity.ok(claimResponse);
    }
    //get all claims of employee
    @GetMapping("/my")
    public ResponseEntity<List<ClaimResponse>> getAllMyClaims() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();


        List<ClaimResponse> claims = claimService.getAllMyClaims(userEmail);


        return ResponseEntity.ok(claims);
    }

    // Get the claims by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ClaimResponse>> getMyClaimsByStatus(@PathVariable ClaimStatus status) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();


        List<ClaimResponse> claims = claimService.getAllMyClaimsByStatus(userEmail, status);


        return ResponseEntity.ok(claims);
    }

    @DeleteMapping("/delete/{claimId}")
    public ResponseEntity<Void> deleteClaim(@PathVariable Long claimId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        claimService.deleteClaim(claimId, userEmail);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{claimId}")
    public ResponseEntity<ClaimResponse> updateClaimTitle(@PathVariable Long claimId, @Valid @RequestBody UpdateClaimRequest updateClaimRequest){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userEmail = authentication.getName();

        ClaimResponse response = claimService.updateClaim(claimId, updateClaimRequest,userEmail);

        return ResponseEntity.ok(response);
    }

}
