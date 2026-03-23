package com.company.claimx.controller;

import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.service.FinanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/claims")
@Tag(name = "finance approvals", description = "pay the approved claims")
public class FinanceController {

    @Autowired
    FinanceService financeService;

    @GetMapping("/approved")
    public ResponseEntity<List<ClaimResponse>> getApprovedClaim(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String financeUser = authentication.getName();

        List<ClaimResponse> claimResponseList = financeService.getApprovedClaim(financeUser);

        return ResponseEntity.ok(claimResponseList);
    }

    @PostMapping("/{claimId}/paid")
    public ResponseEntity<ClaimResponse> payClaims(@PathVariable Long claimId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String financeUser = authentication.getName();

        ClaimResponse claimResponse  = financeService.payClaims(claimId, financeUser );

        return ResponseEntity.ok(claimResponse);
    }

    @GetMapping("/paid")
    public ResponseEntity<List<ClaimResponse>> getAllPaidClaim(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String financeUser = authentication.getName();

        List<ClaimResponse> claimResponseList = financeService.getAllPaidClaim(financeUser);

        return ResponseEntity.ok(claimResponseList);
    }


}
