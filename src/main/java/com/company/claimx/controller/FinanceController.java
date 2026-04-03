package com.company.claimx.controller;

import com.company.claimx.annotation.Authenticated;
import com.company.claimx.context.AuthenticationContext;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.service.FinanceService;
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
@Tag(name = "finance approvals", description = "pay the approved claims")
public class FinanceController {

    @Autowired
    FinanceService financeService;

    /**
     * endpoint to get the approved claims
     * @return - claim response, list of all the approved claims
     */
    @GetMapping("/approved")
    @Authenticated(roles = {"FINANCE"})
    public ResponseEntity<List<ClaimResponse>> getApprovedClaim(){


        String financeUser = AuthenticationContext.getUserEmail();

        List<ClaimResponse> claimResponseList = financeService.getApprovedClaim(financeUser);

        return ResponseEntity.ok(claimResponseList);
    }

    /**
     * endpoint to pay the claims
     * @param claimId - id of the claim
     * @return - claim response of the paid claim
     */
    @PostMapping("/{claimId}/paid")
    @Authenticated(roles = {"FINANCE"})
    public ResponseEntity<ClaimResponse> payClaims(@PathVariable Long claimId){

        String financeUser = AuthenticationContext.getUserEmail();

        ClaimResponse claimResponse  = financeService.payClaims(claimId, financeUser );

        return ResponseEntity.ok(claimResponse);
    }

    /**
     * endpoint to get all the paid claim
     * @return - claim response list of the paid claim
     */
    @GetMapping("/paid")
    @Authenticated(roles = {"FINANCE"})
    public ResponseEntity<List<ClaimResponse>> getAllPaidClaim(){


        String financeUser = AuthenticationContext.getUserEmail();

        List<ClaimResponse> claimResponseList = financeService.getAllPaidClaim(financeUser);

        return ResponseEntity.ok(claimResponseList);
    }


}
