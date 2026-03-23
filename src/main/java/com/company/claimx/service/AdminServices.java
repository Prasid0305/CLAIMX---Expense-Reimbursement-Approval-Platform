package com.company.claimx.service;

import com.company.claimx.dto.response.*;
import com.company.claimx.entity.*;
import com.company.claimx.enums.UserRole;
import com.company.claimx.exception.UserNotFoundException;
import com.company.claimx.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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


    public List<UserResponse> getAllUsers(String userEmail){
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException("user not found "));
        List<User> users = userRepository.findAll();
        
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toUnmodifiableList());
    }

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

    public List<ClaimResponse> getAllClaims(String userEmail){

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException("user not found "));


        List<ExpenseClaim> claims = expenseClaimRepository.findAll();
        return claims.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
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


    public List<AuditLogResponse> getAllAuditLogs(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException("user not found "));


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
                .comments(auditLog.getComments())
                .timestamp(auditLog.getTimestamp())
                .build();
    }

    public UserWithClaimResponse getUserWithClaims(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException("user not found "));


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
