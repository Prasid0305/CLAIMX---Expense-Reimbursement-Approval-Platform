package com.company.claimx.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateClaimRequest {

    @NotBlank(message="title is required")
    private String title;

    public CreateClaimRequest(String title) {
        this.title = title;
    }

    public CreateClaimRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
