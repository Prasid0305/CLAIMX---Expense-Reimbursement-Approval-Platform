package com.company.claimx.service;

import com.company.claimx.constants.ErrorMessageConstants;
import com.company.claimx.dto.response.*;
import com.company.claimx.entity.*;
import com.company.claimx.enums.UserRole;
import com.company.claimx.exception.UserNotFoundException;
import com.company.claimx.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * service class to define all the business logic of admin services
 * To get all the users, get all the claims, get particular user's details along with the claim, get all the audit logs
 */
@Service
public class AdminServices {



    @Autowired
    UserRepository userRepository;

    @Autowired
    ExpenseClaimRepository expenseClaimRepository;

    @Autowired
    AuditLogRepository auditLogRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private ExpenseItemRepository expenseItemRepository;

    @Autowired
    private EmployeeManagerRepository employeeManagerRepository;

    /**
     * to get all the users for the admin
     * @param userEmail - admin email
     * @return - list of all the users
     * @throws UserNotFoundException - if the admin user is not found
     */
    @Transactional
    public List<UserResponse> getAllUsers(String userEmail){
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND));
        List<User> users = userRepository.findAll();
        
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * to map the user details
     * @param user - user for the userResponse.
     * @return - details of the user
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .employeeCode(user.getEmployeeCode())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * to get all the claim of all the users
     * @param userEmail - admin user email
     * @return - list of all the claims
     * @throws UserNotFoundException
     */
    @Transactional
    public List<ClaimResponse> getAllClaims(String userEmail){

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND));


        List<ExpenseClaim> claims = expenseClaimRepository.findAll();
        return claims.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * to map all the claims
     * @param savedClaim -
     * @return claim response
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
     * map the item response
     * @param expenseItem - item with all the details
     * @return - expense item with the details
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
     * to get all the audit logs
     * @param userEmail - to authorize the admin user
     * @return - the list of all the audit logs
     */
    @Transactional
    public List<AuditLogResponse> getAllAuditLogs(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.ADMIN_NOT_FOUND));


        List<AuditLog> logs = auditService.getAllLogs();

        return logs.stream()
                .map(this::mapToAuditLogResponse)
                .collect(Collectors.toList());
    }


    private AuditLogResponse mapToAuditLogResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .logId(auditLog.getLogId())
                .claimId(auditLog.getClaim().getClaimId())
                .claimNumber(auditLog.getClaim().getClaimNumber())
                .performedBy(auditLog.getPerformedBy().getEmail())
                .action(auditLog.getAction())
                .oldStatus(auditLog.getOldStatus())
                .newStatus(auditLog.getNewStatus())
                .timestamp(auditLog.getTimestamp())
                .build();
    }

    /**
     * to get all the user details with all his claims
     * @param userId - get user id inorder to retrieve the user details and the claims
     * @return - user response with the claims
     * @throws UserNotFoundException - if the user is not found
     */
    @Transactional
    public UserWithClaimResponse getUserWithClaims(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND_WITH_ID +userId));


        List<ExpenseClaim> claims = expenseClaimRepository.findByEmployee(user);

        List<ClaimResponse> claimResponseList = claims.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());


        return UserWithClaimResponse.builder()
                .userId(user.getId())
                .employeeCode(user.getEmployeeCode())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .claims(claimResponseList)
                .build();
    }
}
