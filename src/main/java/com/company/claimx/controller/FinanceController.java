package com.company.claimx.controller;

import com.company.claimx.annotation.Authenticated;
import com.company.claimx.constants.MessageResponseConstants;
import com.company.claimx.context.AuthenticationContext;
import com.company.claimx.dto.response.ApiResponse;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * controller class for the finance user operation
 * provides endpoints for retrieving the approved claims, pay the claims, get the paid claims
 */
@RestController
@RequestMapping("/api/finance/claims")
@Tag(name = "Finance review and approvals", description = "Pay the approved claims, get all the paid claims")
public class FinanceController {

    @Autowired
    FinanceService financeService;

    /**
     * endpoint to get the approved claims
     * @return - claim response, list of all the approved claims
     */
    @Operation(summary = "Get all the approved claims",description = "User retrieves all the claims that are approved")
    @GetMapping("/approved")
    @Authenticated(roles = {"FINANCE"})
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> getApprovedClaim(){


        String financeUser = AuthenticationContext.getUserEmail();

        List<ClaimResponse> claimResponseList = financeService.getApprovedClaim(financeUser);

        return ResponseEntity.ok(ApiResponse.success(claimResponseList, MessageResponseConstants.APPROVED_CLAIMS_RETRIEVED));
    }

    /**
     * endpoint to pay the claims
     * @param claimId - id of the claim
     * @return - claim response of the paid claim
     */
    @Operation(summary = "Pay the approved claim",description = "User pays the approved claim")
    @PostMapping("/{claimId}/paid")
    @Authenticated(roles = {"FINANCE"})
    public ResponseEntity<ApiResponse<ClaimResponse>> payClaims(@PathVariable Long claimId){

        String financeUser = AuthenticationContext.getUserEmail();

        ClaimResponse claimResponse  = financeService.payClaims(claimId, financeUser );

        return ResponseEntity.ok(ApiResponse.success(claimResponse, MessageResponseConstants.CLAIM_PAID));
    }

    /**
     * endpoint to get all the paid claim
     * @return - claim response list of the paid claim
     */
    @Operation(summary = "Get all the paid claims",description = "User retrieves all the claims that are paid.")
    @GetMapping("/paid")
    @Authenticated(roles = {"FINANCE"})
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> getAllPaidClaim(){


        String financeUser = AuthenticationContext.getUserEmail();

        List<ClaimResponse> claimResponseList = financeService.getAllPaidClaim(financeUser);

        return ResponseEntity.ok(ApiResponse.success(claimResponseList, MessageResponseConstants.PAID_CLAIMS_RETRIEVED));
    }


}
