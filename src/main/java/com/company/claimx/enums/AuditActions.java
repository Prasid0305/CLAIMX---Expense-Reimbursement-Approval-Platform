package com.company.claimx.enums;

public enum AuditActions {
    CLAIM_CREATED("claim created"),
    CLAIM_SUBMITTED("claim submitted"),
    CLAIM_REJECTED("claim rejected"),
    CLAIM_APPROVED("claim approved"),
    CLAIM_UPDATED("claim updated"),
    CLAIM_DELETED("claim deleted"),
    CLAIM_PAID("claim paid");

    private final String value;

    AuditActions(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
