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

import java.util.List;
import java.util.stream.Collectors;

/**
 * service class contains all the business logic of financial user operations
 * a finance user can get all the approved claims, pay the claims and get all the paid claims
 */
@Service
public class FinanceService {

    private static final Logger logger = LoggerFactory.getLogger(FinanceService.class);
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
     * get all the approved claims for the user
     * @param userEmail - finance user email
     * @return - list of all the claims which are in APPROVED status
     * @throws UserNotFoundException - if the finance user is not found
     */
    @Transactional
    public List<ClaimResponse> getApprovedClaim(String userEmail){

        logger.info("Retrieving all the approved claims");
        User financeUser = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.FINANCE_USER_NOT_FOUND + userEmail));

        List<ExpenseClaim> claims = expenseClaimRepository.findByStatus(ClaimStatus.APPROVED);




        logger.info("Approved claims retrieved successfully");
        return claimMapper.toClaimResponseList(claims);

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

        logger.info("Pay the claim with the id:{}",claimId);

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




        logger.info("Claim paid successfully.");
        return claimMapper.toClaimResponse(savedClaim);


    }

    /**
     * helper method used to check is the claim is in APPROVED status
     * @param claim - claim that is accessed
     * @throws InvalidClaimStatus - if the claim is not in APPROVED status
     */
    private void validateApprovedStatus(ExpenseClaim claim) {

        logger.info("Validating approved status.");

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

        logger.info("Get all the paid claims");
        User financeUser = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.FINANCE_USER_NOT_FOUND + userEmail));

        List<ExpenseClaim> claims = expenseClaimRepository.findByStatus(ClaimStatus.PAID);



        return claimMapper.toClaimResponseList(claims);

    }
}
