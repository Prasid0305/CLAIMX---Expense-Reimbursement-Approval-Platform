package com.company.claimx.service;

import com.company.claimx.constants.ErrorMessageConstants;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.dto.response.ExpenseItemResponse;
import com.company.claimx.entity.EmployeeManager;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.entity.ExpenseItem;
import com.company.claimx.entity.User;
import com.company.claimx.enums.AuditActions;
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

/**
 * service class contains all the business logic of financial user operations
 * a finance user can get all the approved claims, pay the claims and get all the paid claims
 */
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

    /**
     * get all the approved claims for the user
     * @param userEmail - finance user email
     * @return - list of all the claims which are in APPROVED status
     * @throws UserNotFoundException - if the finance user is not found
     */
    @Transactional
    public List<ClaimResponse> getApprovedClaim(String userEmail){
        User financeUser = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.FINANCE_USER_NOT_FOUND + userEmail));

        List<ExpenseClaim> claims = expenseClaimRepository.findByStatus(ClaimStatus.APPROVED);

        return claims.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }

    /**
     * Converts an ExpenseClaim entity into ClaimResponse.
     * Includes manager info and all expense items attached to the claim.
     * @param savedClaim - claim entity to convert
     * @return - mapped response DTO
     */
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

    /**
     *  Converts an ExpenseItem entity into a ExpenseItemResponse
     *  Includes manager info and all expense items attached to the claim.
     * @param expenseItem - claim entity to convert
     * @return mapped response DTO
     */
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

    /**
     * this method is used to pay the claims ie: to change the APPROVED status to PAID status
     * @param claimId - id of the claim
     * @param userEmail - finance user email
     * @return - claim response with the status changed to PAID
     * @throws ClaimNotFoundException - if the claim is not found
     * @throws UserNotFoundException - if the finance user is not found
     */
    @Transactional
    public ClaimResponse payClaims(Long claimId, String userEmail){
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException(ErrorMessageConstants.CLAIM_NOT_FOUND_WITH_ID +claimId));

        User financeUser = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.FINANCE_USER_NOT_FOUND+ userEmail));



        validateApprovedStatus(claim);

        String oldStatus = claim.getStatus().name();
        claim.setStatus(ClaimStatus.PAID);


        ExpenseClaim savedClaim= expenseClaimRepository.save(claim);

        auditService.logClaimAction(
                savedClaim,
                financeUser,
                AuditActions.CLAIM_PAID.getValue(),
                oldStatus ,
                String.valueOf(ClaimStatus.PAID));

        return mapToResponse(savedClaim);


    }

    /**
     * helper method used to check is the claim is in APPROVED status
     * @param claim - claim that is accessed
     * @throws InvalidClaimStatus - if the claim is not in APPROVED status
     */
    private void validateApprovedStatus(ExpenseClaim claim) {
        if(claim.getStatus() != ClaimStatus.APPROVED){
            throw new InvalidClaimStatus(ErrorMessageConstants.INVALID_CLAIM_STATUS_FOR_CLAIM_PAY + claim.getStatus());
        }
    }

    /**
     * get all the claims that are in PAID  status
     * @param userEmail - finance user email
     * @return - list of claim response with the PAID status
     */
    @Transactional
    public List<ClaimResponse> getAllPaidClaim(String userEmail){
        User financeUser = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.FINANCE_USER_NOT_FOUND + userEmail));

        List<ExpenseClaim> claims = expenseClaimRepository.findByStatus(ClaimStatus.PAID);

        return claims.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }
}
