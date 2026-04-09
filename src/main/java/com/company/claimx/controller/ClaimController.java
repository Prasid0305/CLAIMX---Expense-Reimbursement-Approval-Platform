package com.company.claimx.controller;


import com.company.claimx.annotation.Authenticated;
import com.company.claimx.config.SecurityConfig;
import com.company.claimx.context.AuthenticationContext;
import com.company.claimx.dto.request.CreateClaimRequest;
import com.company.claimx.dto.request.UpdateClaimRequest;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.enums.ClaimStatus;
import com.company.claimx.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
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

/**
 * controller class for claim operations
 * provides the endpoints to create the claim, retrieve the claims, get the claims by id, submit the claim, get the claim s by status, delete the claim, update the claim
 */
@RestController
@RequestMapping("/api/claims")
@Tag(name = "Employee claims", description = "Claim management for the employees")
public class ClaimController {

    @Autowired
    private ClaimService claimService;

    /**
     * end point to create the claim
     * @param createClaimRequest - claimRequest containing the title for creating the claim
     * @return - claim response which contains all the details of the claim such as id, employee, total amount, createdAt time, etc.
     */
    @Operation(summary = "Create claim",description = "User creates the claim, requires title and returns the claim body")
    @PostMapping
    @Authenticated
    public ResponseEntity<ClaimResponse> createClaim(@Valid @RequestBody CreateClaimRequest createClaimRequest){


        String email = AuthenticationContext.getUserEmail();
        ClaimResponse claimResponse = claimService.createClaim(createClaimRequest, email);

        return new ResponseEntity<>(claimResponse, HttpStatus.CREATED);



    }

    /**
     * endPoint ot get the claim by id
     * @param claimId - claim id
     * @return claim response with the details of the particular claim
     */
    @Operation(summary = "Retrieve claim by id",description = "User retrieve the claim with the claim id ")
    @GetMapping("/{claimId}")
    @Authenticated
    public ResponseEntity<ClaimResponse> getClaimById(@PathVariable Long claimId){

        String userEmail = AuthenticationContext.getUserEmail();

        ClaimResponse response = claimService.getClaimById(claimId, userEmail);

        return ResponseEntity.ok(response);
    }

    /**
     * endPoint to submit the claim
     * @param claimId - id of the submitting claim
     * @return - claim details for the submitted claim
     */
    @Operation(summary = "Submit claim",description = "User submits the claim")
    @PostMapping("/{claimId}/submit")
    @Authenticated
    public ResponseEntity<ClaimResponse> submitClaim(@PathVariable Long claimId){

        String userEmail = AuthenticationContext.getUserEmail();

        ClaimResponse claimResponse = claimService.submitClaim(claimId, userEmail);

        return ResponseEntity.ok(claimResponse);
    }

    /**
     * endpoint to get all the claims of a particular employee
     * @return list of claims
     */
    @Operation(summary = "Retrieve all the claims",description = "User retrieve all the claim of the user")
    @GetMapping("/my")
    @Authenticated
    public ResponseEntity<List<ClaimResponse>> getAllMyClaims() {

        String userEmail = AuthenticationContext.getUserEmail();


        List<ClaimResponse> claims = claimService.getAllMyClaims(userEmail);


        return ResponseEntity.ok(claims);
    }

    /**
     * endpoint to get the claims according to the status of the claim
     * @param status - status of the claim
     * @return - claims containing the requested status
     */
    @Operation(summary = "Retrieve claims filtered by status",description = "User retrieve claims filtered by status")
    @GetMapping("/status/{status}")
    @Authenticated
    public ResponseEntity<List<ClaimResponse>> getMyClaimsByStatus(@PathVariable ClaimStatus status) {


        String userEmail = AuthenticationContext.getUserEmail();


        List<ClaimResponse> claims = claimService.getAllMyClaimsByStatus(userEmail, status);


        return ResponseEntity.ok(claims);
    }

    /**
     * endpoint to delete the claim with id
     * @param claimId - id of the claim
     * @return - success status code
     */
    @Operation(summary = "Delete the claim",description = "User deletes the claim with the claim id ")
    @DeleteMapping("/{claimId}")
    @Authenticated
    public ResponseEntity<Void> deleteClaim(@PathVariable Long claimId){
        String userEmail = AuthenticationContext.getUserEmail();
        claimService.deleteClaim(claimId, userEmail);

        return ResponseEntity.noContent().build();
    }

    /**
     * endpoint to update the claim for the given claim Id
     * @param claimId - id of the claim
     * @param updateClaimRequest - claim request to updating the claim details
     * @return claim with the updated details
     */
    @Operation(summary = "Update the claim",description = "User updates the claim")
    @PutMapping("/{claimId}")
    @Authenticated
    public ResponseEntity<ClaimResponse> updateClaimTitle(@PathVariable Long claimId, @Valid @RequestBody UpdateClaimRequest updateClaimRequest){

        String userEmail = AuthenticationContext.getUserEmail();

        ClaimResponse response = claimService.updateClaim(claimId, updateClaimRequest,userEmail);

        return ResponseEntity.ok(response);
    }

}
