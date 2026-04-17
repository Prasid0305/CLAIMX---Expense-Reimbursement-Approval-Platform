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
import com.company.claimx.mapper.ClaimMapper;
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

    @Autowired
    private ClaimMapper claimMapper;

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
        logger.info("Creating claim for user: {}",userEmail);
        User employee = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.EMPLOYEE_NOT_FOUND));

        if (!employeeManagerRepository.existsByEmployee(employee)) {
            logger.error("Manager not found for employee: {}", userEmail);
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


        logger.info("Claim created ");
        return claimMapper.toClaimResponse(savedClaim);
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
        logger.info("Retrieving claim by id: {}",claimId);
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException(ErrorMessageConstants.CLAIM_NOT_FOUND));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND_WITH_EMAIL));

        validateClaimAccess(claim, user);


        logger.info("Claim retrieved");
        return claimMapper.toClaimResponse(claim);

    }

    /**
     * to validate whether the user has the access to or not
     * Access is allowed for: the claim owner, the employee's manager, ADMIN, or FINANCE roles
     * @param claim - expenseClaim with the details
     * @param user - the user requesting access
     * @throws UnauthorizedAccessException if the user is not authorized
     */
    private void validateClaimAccess(ExpenseClaim claim, User user){

        logger.info("Validating claim access.");

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

        logger.info("Submitting claim with the id: {}",claimId);

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



        logger.info("Claim submitted");
        return claimMapper.toClaimResponse(savedClaim);


    }

    /**
     * to get all the claims that belongs to the requesting user
     * @param userEmail - to validate the user and get the claim that belongs to him.
     * @return all the claims
     * @throws UserNotFoundException - if the user is not found.
     */
    public List<ClaimResponse> getAllMyClaims(String userEmail) {

        logger.info("Retrieving all the claims of the user: {} ",userEmail);
        User employee = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND_WITH_EMAIL));

        List<ExpenseClaim> claims = expenseClaimRepository.findByEmployee(employee);



        logger.info("All the claims retrieved successfully.");

        return claimMapper.toClaimResponseList(claims);


    }

    /**
     * get all the claims based on the status of the claim.
     * @param userEmail - to validate the user and get the claims that belongs to him based on the requesting status
     * @param status - status of the claim (DRAFT, SUBMITTED, ACCEPTED, etc.)
     * @return the claim with the requested status
     * @throws UserNotFoundException if the user is not found
     */
    public List<ClaimResponse> getAllMyClaimsByStatus(String userEmail, ClaimStatus status){

        logger.info("Retrieving the claims by status:{}",status);

        User employee = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND_WITH_EMAIL));

        List<ExpenseClaim> claims = expenseClaimRepository.findByEmployeeAndStatus(employee, status);



        return claimMapper.toClaimResponseList(claims);
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

        logger.info("Deleting the claim with id:{}",claimId);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()-> new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND_WITH_EMAIL));

        ExpenseClaim expenseClaim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()-> new ClaimNotFoundException(ErrorMessageConstants.CLAIM_NOT_FOUND_WITH_ID +claimId));

        if (!expenseClaim.getEmployee().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException(ErrorMessageConstants.UNAUTHORIZED_CLAIM_DELETION);
        }

        if(expenseClaim.getStatus()!= ClaimStatus.DRAFT){
            throw new InvalidClaimStatus(ErrorMessageConstants.INVALID_CLAIM_STATUS_FOR_CLAIM_DELETION + expenseClaim.getStatus());
        }

        auditLogRepository.deleteByClaimClaimId(claimId);

        expenseItemRepository.deleteByClaimClaimId(claimId);

        expenseClaimRepository.delete(expenseClaim);

        logger.info("The claim with id {} deleted successfully",claimId);

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

        logger.info("Updating the claim with the id:{}", claimId);

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



        logger.info("Claim with the id {} updated sucessfully",claimId);

        return claimMapper.toClaimResponse(updatedClaim);
    }


}
