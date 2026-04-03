package com.company.claimx.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * response dto for audit log
 * contains log info like logId, claim id, performed by, action, old and new status, comments
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {

    private Long logId;
    private Long claimId;
    private String claimNumber;
    private String performedBy;
    private String action;
    private String oldStatus;
    private String newStatus;
//    private String comments;
    private LocalDateTime timestamp;

}
