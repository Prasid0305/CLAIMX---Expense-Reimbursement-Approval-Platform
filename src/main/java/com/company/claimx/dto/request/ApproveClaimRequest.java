package com.company.claimx.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * request dto to approve the claim
 * contains comment that will eb added to the claim
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApproveClaimRequest {
    private String comment;
}
