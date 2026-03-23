package com.company.claimx.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor@AllArgsConstructor
public class RejectClaimRequest {

    @NotBlank(message="rejection comments are required")
    private String comment ;
}
