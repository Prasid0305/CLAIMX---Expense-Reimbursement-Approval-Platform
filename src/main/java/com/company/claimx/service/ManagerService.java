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
import com.company.claimx.mapper.ClaimMapper;
import com.company.claimx.repository.EmployeeManagerRepository;
import com.company.claimx.repository.ExpenseClaimRepository;
import com.company.claimx.repository.ExpenseItemRepository;
import com.company.claimx.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ManagerService.class);
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

    @Autowired
    private ClaimMapper claimMapper;

    /**
     * to get all the pending claims belonging to the manager
     * the claims must be in the SUBMITTED state
     * @param userEmail - to check the authorization
     * @return all the claims along with the items which are in SUBMITTED status
     * @throws UserNotFoundException - if the user is not found
     */
    @Transactional
    public List<ClaimResponse> getPendingClaims(String userEmail){
        logger.info("Retrieving submitted claims");
        try {
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




            logger.info("Submitted claims retrieved successfully ");
            return claimMapper.toClaimResponseList(claims);
        } catch (UserNotFoundException e) {
            logger.error("Could not retrieve the submitted claims,{}",e.getMessage());
            throw e;
        }

    }



    /**
     * to get the pending claim of a manager by id
     * @param claimId - to access the particular claim
     * @param userEmail - to authorize the email.
     * @return - claim response for the requested claim
     * @throws ClaimNotFoundException - if the claim is not found for the particular claim
     * @throws UserNotFoundException - if the user is not found
     */
    @Transactional
    public ClaimResponse getPendingClaimById(Long claimId, String userEmail){

        logger.info("Retrieve the claim with id:{}",claimId);
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException(ErrorMessageConstants.CLAIM_NOT_FOUND_WITH_ID + claimId));


        User manager = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.MANAGER_NOT_FOUND + userEmail));

        validateClaimAccess(claim, manager);

        validateSubmittedStatus(claim);




        logger.info("Claim with id:{} retrieved successfully.",claimId);

        return claimMapper.toClaimResponse(claim);

    }

    /**
     * Checks whether the given user is allowed to access the claim.
     * Claim's owner, current manager, ADMIN, FINANCE are allowed to access the claim
     * @param claim - the claim being accessed
     * @param user - the user requesting access
     * @throws  UnauthorizedAccessException -  if access is not allowed
     */
    private void validateClaimAccess(ExpenseClaim claim, User user){

        logger.info("Validating the claim access for the user:{}",user.getName());
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
        logger.info("Approving the submitted claim:{}",claimId);

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



        logger.info("Claim {} approved successfully",claimId);
        return claimMapper.toClaimResponse(savedClaim);


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

        logger.info("Rejecting the submitted claim {}",claimId);

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




        ExpenseClaim savedClaim= expenseClaimRepository.save(claim);

        auditService.logClaimAction(
                savedClaim,
                manager,
                AuditActions.CLAIM_REJECTED.getValue(),
                claim.getStatus().name(),
                String.valueOf(ClaimStatus.REJECTED));


        logger.info("The claim {} is rejected",claimId);
        return claimMapper.toClaimResponse(savedClaim);


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
        logger.info("Updating claim status and review date ");
        claim.setStatus(ClaimStatus.APPROVED);
        claim.setReviewedDate(LocalDateTime.now());


    }

    /**
     * helper method used during approval or rejection to check is the claim is in SUBMITTED status
     * @param claim - claim that is accessed
     */
    private void validateSubmittedStatus(ExpenseClaim claim) {
        logger.info("Validating the status of the claim");
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
        logger.info("Validating the assigned manager for the claim {}",claim.getClaimId());
        EmployeeManager empMgr = employeeManagerRepository.findByEmployee(claim.getEmployee())
                .orElseThrow(() -> new UserNotFoundException(ErrorMessageConstants.EMPLOYEE_MANAGER_NOT_FOUND));

        if (!empMgr.getManager().getId().equals(manager.getId())) {
            throw new UnauthorizedAccessException(ErrorMessageConstants.UNAUTHORIZED_CLAIM_APPROVAL);
        }
    }
}
