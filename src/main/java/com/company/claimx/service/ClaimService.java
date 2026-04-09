package com.company.claimx.service;

import com.company.claimx.constants.ErrorMessageConstants;
import com.company.claimx.dto.request.CreateClaimRequest;
import com.company.claimx.dto.request.UpdateClaimRequest;
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
import com.company.claimx.repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * service class for handling business logic
 * this class handles creation of claim, getting claim by Id, submitting claim, update claim, and delete claim
 */
@Service
public class ClaimService {
    @Autowired
    private ExpenseClaimRepository expenseClaimRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeManagerRepository employeeManagerRepository;
    @Autowired
    private ExpenseItemRepository expenseItemRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private static final Logger logger = LoggerFactory.getLogger(ClaimService.class);


    /**
     * creates a new claim in the DRAFT status
     * @param createClaimRequest - the request containing claim title
     * @param userEmail - email of the user that is creating the claim
     * @return Claim Response which contains all the details of the claim such as title, employee, total amount, etc.
     * @throws UserNotFoundException if the employee is not found and if the employee's manager is not found
     */
    @Transactional
    public ClaimResponse createClaim(CreateClaimRequest createClaimRequest, String userEmail){
        User employee = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.EMPLOYEE_NOT_FOUND));

        if (!employeeManagerRepository.existsByEmployee(employee)) {
            throw new UserNotFoundException(ErrorMessageConstants.EMPLOYEE_MANAGER_NOT_FOUND);
        }



        String claimNumber = generateClaimNumber();

        ExpenseClaim claim = new ExpenseClaim() ;
                claim.setClaimNumber(claimNumber);
                claim.setTitle(createClaimRequest.getTitle());
                claim.setEmployee(employee);
                claim.setStatus(ClaimStatus.DRAFT);
                claim.setTotalAmount(BigDecimal.ZERO);
                claim.setCreatedAt(LocalDateTime.now());
                claim.setUpdatedAt(LocalDateTime.now());


        ExpenseClaim savedClaim = expenseClaimRepository.save(claim);

        auditService.logClaimAction(
                savedClaim,
                employee,
                AuditActions.CLAIM_CREATED.getValue(),
                null,
                String.valueOf(ClaimStatus.DRAFT));

        return mapToResponse(savedClaim);
    }

    /**
     * maps the ExpenseClaim to a ClaimResponse Dto
     * If savedClaim is approved/rejected, use the approver's name otherwise, get current manager from employee_manager table
     * @param savedClaim - the claim that is mapped
     * @return the mapped ClaimResponse
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
     * maps the expenseItem entity to an expenseItemResponse Dto
     *
     * @param expenseItem - contains details of the item
     * @return the mapped ExpenseItemResponse
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
     * generates a unique claim number in the formate CLM-{YEAR}-{SEQUENCE}.
     * ex: CLM-2026-00001
     *
     * @return the generated claim number string
     */
    private String generateClaimNumber() {
        Long sequence = expenseClaimRepository.getNextClaimSequence();
        int year = LocalDate.now().getYear();

            return String.format("CLM-%d-%05d",year,sequence);
    }

    /**
     * retrieves a claim by its ID after validating user access.
     *
     * @param claimId - the ID of the claim
     * @param userEmail - email of the requesting user for validating
     * @return the claim as a claimResponse
     * @throws ClaimNotFoundException if the claim is not there for the given ID
     * @throws UnauthorizedAccessException if the user is not authorized to view the claim
     */
    @Transactional
    public ClaimResponse getClaimById(Long claimId, String userEmail){
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException(ErrorMessageConstants.CLAIM_NOT_FOUND));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND_WITH_EMAIL));

        validateClaimAccess(claim, user);

        return mapToResponse(claim);

    }

    /**
     * to validate whether the user has the access to or not
     * Access is allowed for: the claim owner, the employee's manager, ADMIN, or FINANCE roles
     * @param claim - expenseClaim with the details
     * @param user - the user requesting access
     * @throws UnauthorizedAccessException if the user is not authorized
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
     * submitting the claim by the claim owner - the status is changed from DRAFT to SUBMITTED
     * the claim can only be submitted when the status is DRAFT
     *
     * @param claimId - to identify the submitting claim
     * @param userEmail - to authorize the claim owner
     * @return ClaimResponse for the submitted claim
     * @throws ClaimNotFoundException - if there is no claim for the ID.
     * @throws UserNotFoundException - if user is not found.
     * @throws InvalidClaimStatus - if the claim is in any other status other than DRAFT
     */
    @Transactional
    public ClaimResponse submitClaim(Long claimId, String userEmail){

        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException(ErrorMessageConstants.CLAIM_NOT_FOUND_WITH_ID + claimId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND_WITH_EMAIL + userEmail));

        if (!claim.getEmployee().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException(ErrorMessageConstants.UNAUTHORIZED_CLAIM_SUBMISSION);
        }
        if(claim.getStatus() != ClaimStatus.DRAFT){
            throw new InvalidClaimStatus(ErrorMessageConstants.INVALID_CLAIM_STATUS_FOR_CLAIM_SUBMISSION + claim.getStatus());
        }
        long itemCount = expenseItemRepository.countByClaimClaimId(claimId);
        if (itemCount == 0){
            throw new InvalidClaimStatus(ErrorMessageConstants.INVALID_CLAIM_STATUS_EMPTY_EXPENSE_ITEMS);

        }

        String oldStatus = claim.getStatus().name();
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setSubmittedAt(LocalDateTime.now());

        ExpenseClaim savedClaim = expenseClaimRepository.save(claim);

        auditService.logClaimAction(
                savedClaim,
                user,
                AuditActions.CLAIM_SUBMITTED.getValue(),
                oldStatus,
                String.valueOf(ClaimStatus.SUBMITTED));


        return mapToResponse(savedClaim);


    }

    /**
     * to get all the claims that belongs to the requesting user
     * @param userEmail - to validate the user and get the claim that belongs to him.
     * @return all the claims
     * @throws UserNotFoundException - if the user is not found.
     */
    public List<ClaimResponse> getAllMyClaims(String userEmail) {
        User employee = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND_WITH_EMAIL));

        List<ExpenseClaim> claims = expenseClaimRepository.findByEmployee(employee);

        return claims.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());


    }

    /**
     * get all the claims based on the status of the claim.
     * @param userEmail - to validate the user and get the claims that belongs to him based on the requesting status
     * @param status - status of the claim (DRAFT, SUBMITTED, ACCEPTED, etc.)
     * @return the claim with the requested status
     * @throws UserNotFoundException if the user is not found
     */
    public List<ClaimResponse> getAllMyClaimsByStatus(String userEmail, ClaimStatus status){
        User employee = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND_WITH_EMAIL));

        List<ExpenseClaim> claims = expenseClaimRepository.findByEmployeeAndStatus(employee, status);

        return claims.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * delete the claim if its only in the draft status
     * @param claimId - id of the claim
     * @param userEmail - to authorize the user
     * @throws UserNotFoundException - if the user is not found.
     * @throws ClaimNotFoundException - if the claim is not found.
     * @throws UnauthorizedAccessException - only authorized user is allowed to access the claim of deletion.
     * @throws InvalidClaimStatus - if the claim is in any other status other than DRAFT
     */
    @Transactional
    public void deleteClaim(Long claimId, String userEmail){
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()-> new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND_WITH_EMAIL));

        ExpenseClaim expenseClaim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()-> new ClaimNotFoundException(ErrorMessageConstants.CLAIM_NOT_FOUND_WITH_ID +claimId));

        if (!expenseClaim.getEmployee().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException(ErrorMessageConstants.UNAUTHORIZED_CLAIM_DELETION);
        }

        if(expenseClaim.getStatus()!= ClaimStatus.DRAFT){
            throw new InvalidClaimStatus(ErrorMessageConstants.INVALID_CLAIM_STATUS_FOR_CLAIM_DELETION);
        }

        auditLogRepository.deleteByClaimClaimId(claimId);

        expenseItemRepository.deleteByClaimClaimId(claimId);

        expenseClaimRepository.delete(expenseClaim);

        expenseClaimRepository.flush();
    }


    /**
     * update the claim with the claim Id
     * @param claimId - to access the claim for updating
     * @param updateClaimRequest - to get the claim updating data
     * @param userEmail - to check the authorization
     * @return the ClaimResponse with the updated data
     * @throws ClaimNotFoundException - if the claim is not found with id.
     * @throws UserNotFoundException - if the user is not found.
     * @throws UnauthorizedAccessException - if the user is not authorized to update
     * @throws InvalidClaimStatus - if the claim not in DRAFT status.
     *
     */

    @Transactional
    public ClaimResponse updateClaim(Long claimId, UpdateClaimRequest updateClaimRequest, String userEmail){

        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException(ErrorMessageConstants.CLAIM_NOT_FOUND_WITH_ID + claimId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND_WITH_EMAIL));

        if (!claim.getEmployee().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException(ErrorMessageConstants.UNAUTHORIZED_CLAIM_UPDATING);
        }


        if(claim.getStatus() != ClaimStatus.DRAFT){
            throw new InvalidClaimStatus(ErrorMessageConstants.INVALID_CLAIM_STATUS_FOR_CLAIM_UPDATE + claim.getStatus());
        }

        claim.setTitle(updateClaimRequest.getTitle());

        ExpenseClaim updatedClaim = expenseClaimRepository.save(claim);

        return mapToResponse(updatedClaim);
    }


}
