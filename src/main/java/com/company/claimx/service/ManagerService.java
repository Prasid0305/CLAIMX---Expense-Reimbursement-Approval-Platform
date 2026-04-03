package com.company.claimx.service;

import com.company.claimx.constants.ErrorMessageConstants;
import com.company.claimx.dto.request.ApproveClaimRequest;
import com.company.claimx.dto.request.RejectClaimRequest;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.dto.response.ExpenseItemResponse;
import com.company.claimx.entity.EmployeeManager;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.entity.ExpenseItem;
import com.company.claimx.entity.User;
import com.company.claimx.enums.AuditActions;
import com.company.claimx.enums.ClaimStatus;
import com.company.claimx.enums.UserRole;
import com.company.claimx.exception.ClaimNotFoundException;
import com.company.claimx.exception.InvalidClaimStatus;
import com.company.claimx.exception.UnauthorizedAccessException;
import com.company.claimx.exception.UserNotFoundException;
import com.company.claimx.repository.EmployeeManagerRepository;
import com.company.claimx.repository.ExpenseClaimRepository;
import com.company.claimx.repository.ExpenseItemRepository;
import com.company.claimx.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ths is a service class that contains business logic for the manager operations
 * this class contains logic for retrieving all the pending claims belonging to the manager, particular claims,  approve or reject the pending claims
 */
@Service
public class ManagerService {

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
     * to get all the pending claims belonging to the manager
     * the claims must be in the SUBMITTED state
     * @param userEmail - to check the authorization
     * @return all the claims along with the items which are in SUBMITTED status
     * @throws UserNotFoundException - if the user is not found
     */
    @Transactional
    public List<ClaimResponse> getPendingClaims(String userEmail){
        User manager = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.MANAGER_NOT_FOUND + userEmail));

        List<ExpenseClaim> allSubmitted = expenseClaimRepository.findByStatus(ClaimStatus.SUBMITTED);


        List<ExpenseClaim> claims = allSubmitted.stream()
                .filter(claim -> {
                    EmployeeManager empMgr = employeeManagerRepository.findByEmployee(claim.getEmployee())
                            .orElse(null);
                    return empMgr != null && empMgr.getManager().getId().equals(manager.getId());
                })
                .collect(Collectors.toList());


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

    private ClaimResponse mapToResponse(ExpenseClaim savedClaim) {

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
     * to get the pending claim of a manager by id
     * @param claimId - to acccess the particular claim
     * @param userEmail - to authorize the email.
     * @return - claim response for the requested claim
     * @throws ClaimNotFoundException - if the claim is not found for the particular claim
     * @throws UserNotFoundException - if the user is not found
     */
    @Transactional
    public ClaimResponse getPendingClaimById(Long claimId, String userEmail){
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException(ErrorMessageConstants.CLAIM_NOT_FOUND_WITH_ID + claimId));


        User manager = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.MANAGER_NOT_FOUND + userEmail));

        validateClaimAccess(claim, manager);

        return mapToResponse(claim);

    }

    /**
     * Checks whether the given user is allowed to access the claim.
     * Claim owner, current manager, ADMIN, FINANCE are allowed to access the claim
     * @param claim - the claim being accessed
     * @param user - the user requesting access
     * @throws  UnauthorizedAccessException -  if access is not allowed
     */
    private void validateClaimAccess(ExpenseClaim claim, User user){

        if(claim.getEmployee().getId().equals(user.getId())){
            return;
        }


        EmployeeManager empMgr = employeeManagerRepository.findByEmployee(claim.getEmployee())
                .orElse(null);
        if (empMgr != null && empMgr.getManager().getId().equals(user.getId())){
            return;
        }

        if(user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.FINANCE){
            return;
        }
        throw new UnauthorizedAccessException(ErrorMessageConstants.UNAUTHORIZED_ACCESS);

    }

    /**
     * approve the submitted claims
     * @param claimId - id of the claim to approve
     * @param approveClaimRequest - request containing the claim approval comment
     * @param userEmail- email of the manager approving the claim
     * @return - updated claim response
     * @throws ClaimNotFoundException - if the claim does not exist
     * @throws UserNotFoundException - if manager user does not exist
     */
    @Transactional
    public ClaimResponse approvePendingClaimById(Long claimId, ApproveClaimRequest approveClaimRequest, String userEmail){
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException(ErrorMessageConstants.CLAIM_NOT_FOUND_WITH_ID +claimId));

        User manager = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.MANAGER_NOT_FOUND + userEmail));

        validateManagerAssigned(claim, manager);

        validateSubmittedStatus(claim);

        String oldStatus = claim.getStatus().name();

        updateClaimStatusAndReviewDate(claim);

        claim.setApprovedBy(manager);
        claim.setApprovedByName(manager.getName());

        claim.setReviewComment(approveClaimRequest.getComment());


        ExpenseClaim savedClaim= expenseClaimRepository.save(claim);

        auditService.logClaimAction(
                savedClaim,
                manager,
                AuditActions.CLAIM_APPROVED.getValue(),
                oldStatus,
                String.valueOf(ClaimStatus.APPROVED));

        return mapToResponse(savedClaim);


    }

    /**
     * reject all the submitted claims
     * @param claimId - id of the claim to approve
     * @param rejectClaimRequest - request containing the claim approval comment
     * @param userEmail- email of the manager approving the claim
     * @return - updated claim response
     * @throws ClaimNotFoundException - if the claim does not exist
     * @throws UserNotFoundException - if manager user does not exist
     */
    @Transactional
    public ClaimResponse rejectPendingClaimById(Long claimId, RejectClaimRequest rejectClaimRequest, String userEmail){
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException(ErrorMessageConstants.CLAIM_NOT_FOUND_WITH_ID +claimId));

        User manager = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.MANAGER_NOT_FOUND + claimId));

        validateManagerAssigned(claim, manager);

        validateSubmittedStatus(claim);



        updateClaimStatusAndReviewDateForRejection(claim);

        claim.setApprovedBy(manager);
        claim.setApprovedByName(manager.getName());

        claim.setReviewComment(rejectClaimRequest.getComment());

        //claim.setReviewComment("Claim Rejected");


        ExpenseClaim savedClaim= expenseClaimRepository.save(claim);

        auditService.logClaimAction(
                savedClaim,
                manager,
                AuditActions.CLAIM_REJECTED.getValue(),
                claim.getStatus().name(),
                String.valueOf(ClaimStatus.REJECTED));

        return mapToResponse(savedClaim);


    }

    /**
     * Helper method used during rejection to set REJECTED status and review date.
     * @param claim - claim that is to be rejected
     */
    private void updateClaimStatusAndReviewDateForRejection(ExpenseClaim claim) {
        claim.setStatus(ClaimStatus.REJECTED);
        claim.setReviewedDate(LocalDateTime.now());
    }

    /**
     * helper method used during approval to set APPROVED status and review date
     * @param claim- claim that is to be approved
     */
    private void updateClaimStatusAndReviewDate(ExpenseClaim claim) {
        claim.setStatus(ClaimStatus.APPROVED);
        claim.setReviewedDate(LocalDateTime.now());


    }

    /**
     * helper method used during approval or rejection to check is the claim is in SUBMITTED status
     * @param claim - claim that is accessed
     */
    private void validateSubmittedStatus(ExpenseClaim claim) {
        if(claim.getStatus() != ClaimStatus.SUBMITTED){
            throw new InvalidClaimStatus(ErrorMessageConstants.INVALID_CLAIM_STATUS_FOR_CLAIM_APPROVAL+ claim.getStatus());
        }
    }

    /**
     * helper method to validate if the manager is assigned and to get the correct manager
     * @param claim - expense claim that is accessed
     * @param manager - to check the access
     */
    private void validateManagerAssigned(ExpenseClaim claim, User manager) {
        EmployeeManager empMgr = employeeManagerRepository.findByEmployee(claim.getEmployee())
                .orElseThrow(() -> new UserNotFoundException(ErrorMessageConstants.EMPLOYEE_MANAGER_NOT_FOUND));

        if (!empMgr.getManager().getId().equals(manager.getId())) {
            throw new UnauthorizedAccessException(ErrorMessageConstants.UNAUTHORIZED_CLAIM_APPROVAL);
        }
    }
}
