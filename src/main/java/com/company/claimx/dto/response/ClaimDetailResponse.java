package com.company.claimx.dto.response;

import com.company.claimx.enums.ClaimStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * response dto for claim
 * contains info such as clim id, employee, manager, etc.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClaimDetailResponse {

    private Long claimId;

    private String claimNumber;


    private Long employeeId;
    private String employeeCode;
    private String employeeName;



    private Long managerId;
    private String managerName;


    private String title;


    private BigDecimal totalAmount;


    private ClaimStatus status;



    private LocalDateTime reviewedDate;


    private String reviewComment;



    private LocalDateTime createdAt;



    private LocalDateTime submittedAt;

    private List<ExpenseItemResponse> items;
    private int itemCount;
}
