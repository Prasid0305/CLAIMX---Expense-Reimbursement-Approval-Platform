package com.company.claimx.service;

import com.company.claimx.dto.request.ApproveClaimRequest;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.dto.response.ExpenseItemResponse;
import com.company.claimx.entity.EmployeeManager;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.entity.ExpenseItem;
import com.company.claimx.entity.User;
import com.company.claimx.enums.ClaimStatus;
import com.company.claimx.exception.ClaimNotFoundException;
import com.company.claimx.exception.InvalidClaimStatus;
import com.company.claimx.exception.UserNotFoundException;
import com.company.claimx.repository.EmployeeManagerRepository;
import com.company.claimx.repository.ExpenseClaimRepository;
import com.company.claimx.repository.ExpenseItemRepository;
import com.company.claimx.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinanceService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseClaimRepository expenseClaimRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private ExpenseItemRepository expenseItemRepository;

    @Autowired
    private EmployeeManagerRepository employeeManagerRepository;


    @Transactional
    public List<ClaimResponse> getApprovedClaim(String userEmail){
        User financeUser = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException("financeUser not found "));

        List<ExpenseClaim> claims = expenseClaimRepository.findByStatus(ClaimStatus.APPROVED);

        return claims.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }

    @Transactional
    private ClaimResponse mapToResponse(ExpenseClaim savedClaim) {

        // Get manager dynamically
        String managerName = null;
        Long managerId = null;

        if (savedClaim.getApprovedBy() != null) {
            managerName = savedClaim.getApprovedByName();
            managerId = savedClaim.getApprovedBy().getId();
        } else {
            EmployeeManager empMgr = employeeManagerRepository.findByEmployee(savedClaim.getEmployee())
                    .orElse(null);
            if (empMgr != null && empMgr.getManager() != null) {
                managerName = empMgr.getManager().getName();
                managerId = empMgr.getManager().getId();
            }
        }
        List<ExpenseItem> itemList = expenseItemRepository.findByClaimClaimId(savedClaim.getClaimId());

        List<ExpenseItemResponse> expenseItemResponseList = itemList.stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toUnmodifiableList());

        return ClaimResponse.builder()
                .claimId(savedClaim.getClaimId())
                .claimNumber(savedClaim.getClaimNumber())
                .title(savedClaim.getTitle())
                .totalAmount(savedClaim.getTotalAmount())
                .status(savedClaim.getStatus())
                .employeeId(savedClaim.getEmployee().getId())
                .employeeCode(savedClaim.getEmployee().getEmployeeCode())
                .employeeName(savedClaim.getEmployee().getName())
                .managerId(managerId)
                .managerName(managerName)
                .createdAt(savedClaim.getCreatedAt())
                .submittedAt(savedClaim.getSubmittedAt())
                .reviewedDate(savedClaim.getReviewedDate())
                .reviewComment(savedClaim.getReviewComment())
                .items(expenseItemResponseList)
                .build();
    }

    private ExpenseItemResponse mapItemToResponse(ExpenseItem expenseItem) {
        return ExpenseItemResponse.builder()
                .itemId(expenseItem.getItemId())
                .claimId(expenseItem.getClaim().getClaimId())
                .category(expenseItem.getCategory())
                .description(expenseItem.getDescription())
                .amount(expenseItem.getAmount())
                .expenseDate(expenseItem.getExpenseDate())
                .build();
    }

    @Transactional
    public ClaimResponse payClaims(Long claimId, String userEmail){
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException("claim not found with id: " +claimId));

        User financeUser = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException("financeUser not found "));



        validateApprovedStatus(claim);

        String oldStatus = claim.getStatus().name();
        claim.setStatus(ClaimStatus.PAID);


        ExpenseClaim savedClaim= expenseClaimRepository.save(claim);

        auditService.logClaimAction(savedClaim, financeUser, "CLAIM_PAID", oldStatus , "PAID", "PAYMENT PROCESSED");

        return mapToResponse(savedClaim);


    }

    private void validateApprovedStatus(ExpenseClaim claim) {
        if(claim.getStatus() != ClaimStatus.APPROVED){
            throw new InvalidClaimStatus("CLAIM can be paid only when the status is in APPROVED state, current state:" + claim.getStatus());
        }
    }

    @Transactional
    public List<ClaimResponse> getAllPaidClaim(String userEmail){
        User financeUser = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException("financeUser not found "));

        List<ExpenseClaim> claims = expenseClaimRepository.findByStatus(ClaimStatus.PAID);

        return claims.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }
}
