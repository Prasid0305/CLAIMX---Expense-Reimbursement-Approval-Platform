package com.company.claimx.controller;

import com.company.claimx.annotation.Authenticated;
import com.company.claimx.constants.MessageResponseConstants;
import com.company.claimx.context.AuthenticationContext;
import com.company.claimx.dto.request.ApproveClaimRequest;
import com.company.claimx.dto.request.RejectClaimRequest;
import com.company.claimx.dto.response.ApiResponse;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.service.ClaimService;
import com.company.claimx.service.ManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * controller class for the manager operation
 * provides endpoints for retrieving the SUBMITTED claims, APPROVE or REJECT the claims, get the claims by id
 */
@RestController
@RequestMapping("/api/manager/claims")
@Tag(name = "Manager approval", description = "Review and approve/reject claims")
public class ManagerController {

    @Autowired
    private ManagerService managerService;


    /**
     * endpoint to get all the submitted claim
     * @return - claim response list
     */
    @Operation(summary = "Get all the submitted claims",description = "User retrieves all the belonging claims.")
    @GetMapping("/pending")
    @Authenticated(roles = {"MANAGER"})
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> getPendingClaims(){


        String userEmail = AuthenticationContext.getUserEmail();

        List<ClaimResponse> claims = managerService.getPendingClaims(userEmail);
        return ResponseEntity.ok(ApiResponse.success(claims, MessageResponseConstants.SUBMITTED_CLAIMS_RETRIEVED));
    }

    /**
     * endpoint to get a submitted claim by  Id
     * @param claimId - id of the claim
     * @return
     */
    @Operation(summary = "Get all the submitted claims by id",description = "User retrieves the belonging claims  by id.")
    @GetMapping("/pending/{claimId}")
    @Authenticated(roles = {"MANAGER"})
    public ResponseEntity<ApiResponse<ClaimResponse>> getPendingClaimById(@PathVariable Long claimId){


        String userEmail = AuthenticationContext.getUserEmail();

        ClaimResponse response = managerService.getPendingClaimById(claimId, userEmail);

        return ResponseEntity.ok(ApiResponse.success(response,MessageResponseConstants.CLAIM_RETRIEVED));
    }

    /**
     * endpoint to approve a submitted claim
     * @param claimId - id of the claim
     * @param approveClaimRequest - request containing the comment for approving the claim request
     * @return - claim response to the approved claim
     */
    @Operation(summary = "Approve the submitted claim",description = "User approves the claims")
    @PostMapping("/pending/{claimId}/approve")
    @Authenticated(roles = {"MANAGER"})
    public ResponseEntity<ApiResponse<ClaimResponse>> approvePendingClaimById(@PathVariable Long claimId, @Valid @RequestBody ApproveClaimRequest approveClaimRequest){


        String userEmail = AuthenticationContext.getUserEmail();

        ClaimResponse response = managerService.approvePendingClaimById(claimId, approveClaimRequest, userEmail);

        return ResponseEntity.ok(ApiResponse.success(response, MessageResponseConstants.CLAIM_APPROVED));
    }

    /**
     * endpoint to reject a submitted claim
     * @param claimId - id of the claim
     * @param rejectClaimRequest - request containing the comment for rejecting the claim request
     * @return - claim response of the rejected claim
     */
    @Operation(summary = "Reject the submitted claim",description = "User reject the claims")
    @PostMapping("/pending/{claimId}/reject")
    @Authenticated(roles = {"MANAGER"})
    public ResponseEntity<ApiResponse<ClaimResponse>> rejectPendingClaimById(@PathVariable Long claimId, @Valid @RequestBody RejectClaimRequest rejectClaimRequest){

        String userEmail = AuthenticationContext.getUserEmail();

        ClaimResponse response = managerService.rejectPendingClaimById(claimId, rejectClaimRequest, userEmail);

        return ResponseEntity.ok(ApiResponse.success(response,MessageResponseConstants.CLAIM_REJECTED));
    }
}
