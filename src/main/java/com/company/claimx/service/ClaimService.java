package com.company.claimx.service;

import com.company.claimx.dto.request.CreateClaimRequest;
import com.company.claimx.dto.request.UpdateClaimRequest;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.dto.response.ExpenseItemResponse;
import com.company.claimx.entity.EmployeeManager;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.entity.ExpenseItem;
import com.company.claimx.entity.User;
import com.company.claimx.enums.ClaimStatus;
import com.company.claimx.exception.ClaimNotFoundException;
import com.company.claimx.exception.InvalidClaimStatus;
import com.company.claimx.exception.UnauthorizedAccessException;
import com.company.claimx.exception.UserNotFoundException;
import com.company.claimx.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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



    @Transactional
    public ClaimResponse createClaim(CreateClaimRequest createClaimRequest, String userEmail){
        User employee = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException("employee not found"));

        EmployeeManager relationship = employeeManagerRepository.findByEmployee(employee)
                .orElseThrow(()->new RuntimeException("Employee's manager not found"));

        User manager = relationship.getManager();



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

        auditService.logClaimAction(savedClaim, employee, "created claim", null, "DRAFT", "claim created");

        return mapToResponse(savedClaim);
    }

    private ClaimResponse mapToResponse(ExpenseClaim savedClaim) {


        String managerName = null;
        Long managerId = null;

        // If savedClaim is approved/rejected, use the approver's name
        if (savedClaim.getApprovedBy() != null) {
            managerName = savedClaim.getApprovedByName();
            managerId = savedClaim.getApprovedBy().getId();
        } else {
            // Otherwise, get current manager from employee_manager table
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

    private String generateClaimNumber() {
        Long sequence = expenseClaimRepository.getNextClaimSequence();
        int year = LocalDate.now().getYear();

            return String.format("CLM-%d-%05d",year,sequence);
    }

    @Transactional
    public ClaimResponse getClaimById(Long claimId, String userEmail){
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new RuntimeException("claim not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException("user not found"));

        validateClaimAccess(claim, user);

        return mapToResponse(claim);

    }
    private void validateClaimAccess(ExpenseClaim claim, User user){
        // Employee can see their own claims
        if(claim.getEmployee().getId().equals(user.getId())){
            return;
        }

        // Check if user is the current manager
        EmployeeManager empMgr = employeeManagerRepository.findByEmployee(claim.getEmployee())
                .orElse(null);
        if (empMgr != null && empMgr.getManager().getId().equals(user.getId())){
            return;
        }
        if(user.getRole().name().equals("ADMIN") || user.getRole().name().equals("FINANCE")){
            return;
        }
        throw new UnauthorizedAccessException("user not authorized");

    }

    @Transactional
    public ClaimResponse submitClaim(Long claimId, String userEmail){

        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException("claim not found with id: " +claimId));

        // finding user email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException("user not found "));

        // validate user is the owner
        if (!claim.getEmployee().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You can only submit your own claims");
        }
        //validate claim is in DRAFT status
        if(claim.getStatus() != ClaimStatus.DRAFT){
            throw new InvalidClaimStatus("sumit allowed only when in DRAFT state, current state:" + claim.getStatus());
        }
        //at least one item
        long itemCount = expenseItemRepository.countByClaimClaimId(claimId);
        if (itemCount == 0){
            throw new InvalidClaimStatus("cant submit claim without expense item");

        }

        String oldStatus = claim.getStatus().name();

        //update claim status and submittedAt
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setSubmittedAt(LocalDateTime.now());

        //save
        ExpenseClaim savedClaim = expenseClaimRepository.save(claim);

        auditService.logClaimAction(savedClaim, user, "claim submitted", claim.getStatus().name(),"Submitted","claim submitted");


        return mapToResponse(savedClaim);


    }

    public List<ClaimResponse> getAllMyClaims(String userEmail) {
        User employee = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException("user not found "));

        List<ExpenseClaim> claims = expenseClaimRepository.findByEmployee(employee);

        return claims.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());


    }

    public List<ClaimResponse> getAllMyClaimsByStatus(String userEmail, ClaimStatus status){
        User employee = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException("user not found "));

        List<ExpenseClaim> claims = expenseClaimRepository.findByEmployeeAndStatus(employee, status);

        return claims.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteClaim(Long claimId, String userEmail){
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()-> new UserNotFoundException("user not found"));

        ExpenseClaim expenseClaim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()-> new ClaimNotFoundException("claim not found with the id:" +claimId));

        if (!expenseClaim.getEmployee().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You can only delete your own claims");
        }

        if(expenseClaim.getStatus()!= ClaimStatus.DRAFT){
            throw new InvalidClaimStatus("claim can only be deleted in draft state");
        }

        auditLogRepository.deleteByClaimClaimId(claimId);

        expenseItemRepository.deleteByClaimClaimId(claimId);

        expenseClaimRepository.delete(expenseClaim);

        expenseClaimRepository.flush();
    }



    @Transactional
    public ClaimResponse updateClaim(Long claimId, UpdateClaimRequest updateClaimRequest, String userEmail){

        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException("claim not found with id: " +claimId));

        // finding user email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException("user not found "));

        // validate user is the owner
        if (!claim.getEmployee().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You can only submit your own claims");
        }
        //validate claim is in DRAFT status
        if(claim.getStatus() != ClaimStatus.DRAFT){
            throw new InvalidClaimStatus("sumit allowed only when in DRAFT state, current state:" + claim.getStatus());
        }

        claim.setTitle(updateClaimRequest.getTitle());

        ExpenseClaim updatedClaim = expenseClaimRepository.save(claim);

        return mapToResponse(updatedClaim);
    }


}
