package com.company.claimx.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * request dto to update the claim
 * contains the info for updating the claim
 */

@Data
public class UpdateClaimRequest {

    @NotBlank(message = "title is required")
    private String title;

    public UpdateClaimRequest(String title) {
        this.title = title;
    }

    public UpdateClaimRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
