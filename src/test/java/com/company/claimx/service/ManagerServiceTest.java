package com.company.claimx.service;

import com.company.claimx.constants.ErrorMessageConstants;
import com.company.claimx.dto.request.ApproveClaimRequest;
import com.company.claimx.dto.request.RejectClaimRequest;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.entity.EmployeeManager;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.entity.ExpenseItem;
import com.company.claimx.entity.User;
import com.company.claimx.enums.AuditActions;
import com.company.claimx.enums.Category;
import com.company.claimx.enums.ClaimStatus;
import com.company.claimx.enums.UserRole;
import com.company.claimx.exception.InvalidClaimStatus;
import com.company.claimx.exception.UnauthorizedAccessException;
import com.company.claimx.exception.UserNotFoundException;
import com.company.claimx.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManagerServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ManagerServiceTest.class);

    @Mock
    private ExpenseClaimRepository expenseClaimRepository;

    @Mock
    private ExpenseItemRepository expenseItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeManagerRepository employeeManagerRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private ManagerService managerService;

    @Mock
    private AuditService auditService;

    private User employee;
    private User manager;
    private User anotherManager;
    private ExpenseClaim submittedClaim;

    private EmployeeManager employeeManager;
    private ExpenseItem expenseItem;

    @BeforeEach
    public void setUp(){
        employee= new User();
        employee.setId(1L);
        employee.setEmail("pedri.employee@claimx.com");
        employee.setEmployeeCode("EMP001");
        employee.setName("Pedri");
        employee.setRole(UserRole.EMPLOYEE);
        employee.setIsActive(true);
        employee.setCreatedAt(LocalDateTime.now());


        manager= new User();
        manager.setId(2L);
        manager.setEmail("flick.manager@claimx.com");
        manager.setEmployeeCode("MGR001");
        manager.setName("Flick");
        manager.setRole(UserRole.MANAGER);
        manager.setIsActive(true);
        manager.setCreatedAt(LocalDateTime.now());

        anotherManager= new User();
        anotherManager.setId(10L);
        anotherManager.setEmail("testmanager2@claimx.com");
        anotherManager.setEmployeeCode("MGR002");
        anotherManager.setName("testManager2");
        anotherManager.setRole(UserRole.MANAGER);
        anotherManager.setIsActive(true);
        anotherManager.setCreatedAt(LocalDateTime.now());


        employeeManager = new EmployeeManager();
        employeeManager.setEmployee(employee);
        employeeManager.setManager(manager);






        submittedClaim = new ExpenseClaim();
        submittedClaim.setTitle("Test Claim");
        submittedClaim.setClaimId(1L);
        submittedClaim.setClaimNumber("CLM-2026-01012");
        submittedClaim.setTitle("BusinessTrip");
        submittedClaim.setEmployee(employee);

        submittedClaim.setTotalAmount(BigDecimal.ZERO);
        submittedClaim.setStatus(ClaimStatus.SUBMITTED);
        submittedClaim.setCreatedAt(LocalDateTime.now());

        expenseItem = new ExpenseItem();
        expenseItem.setCategory(Category.TRAVEL);
        expenseItem.setDescription("testing description");
        expenseItem.setAmount(new BigDecimal("1500.00"));
        expenseItem.setItemId(10L);
        expenseItem.setClaim(submittedClaim);

    }

    @Test
    void testGetPendingClaims_Success(){

        ExpenseClaim claim = new ExpenseClaim();
        claim.setClaimId(2l);
        claim.setClaimNumber("CLM-2026-00002");
        claim.setEmployee(employee);
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setTotalAmount(new BigDecimal("1500.00"));
        claim.setCreatedAt(LocalDateTime.now());

        List<ExpenseClaim> allSubmittedClaims = Arrays.asList(submittedClaim,claim);

        List<ExpenseItem> allExpenseItems = Arrays.asList(expenseItem);

        when(userRepository.findByEmail("flick.manager@claimx.com"))
                .thenReturn(Optional.of(manager));
        when(expenseClaimRepository.findByStatus(ClaimStatus.SUBMITTED))
                .thenReturn(allSubmittedClaims);
        when(employeeManagerRepository.findByEmployee(employee))
                .thenReturn(Optional.of(employeeManager));
        when(expenseItemRepository.findByClaimClaimId(1L))
                .thenReturn(allExpenseItems);

        List<ClaimResponse> responses = managerService.getPendingClaims("flick.manager@claimx.com");

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("BusinessTrip", responses.get(0).getTitle());
        assertEquals(ClaimStatus.SUBMITTED, responses.get(0).getStatus());

        verify(userRepository, times(1)).findByEmail("flick.manager@claimx.com");
        verify(expenseClaimRepository, times(1)).findByStatus(ClaimStatus.SUBMITTED);
        logger.info("Get pending claims test successful");

    }

    @Test
    public void testGetPendingClaimsById_Success() {


        when(userRepository.findByEmail("flick.manager@claimx.com"))
                .thenReturn(Optional.of(employee));
        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(submittedClaim));
        when(employeeManagerRepository.findByEmployee(employee))
                .thenReturn(Optional.of(employeeManager));
        when(expenseItemRepository.findByClaimClaimId(anyLong()))
                .thenReturn(Collections.emptyList());


        ClaimResponse responses = managerService.getPendingClaimById(
                1L,
                "flick.manager@claimx.com"
        );


        assertNotNull(responses, "Response list must not be null");




        verify(userRepository, times(1)).findByEmail("flick.manager@claimx.com");
//

        verify(expenseItemRepository, times(1)).findByClaimClaimId(anyLong());

        logger.info("Get claims by Id test successful");
    }

    @Test
    void testGetPendingClaims_ManagerNotFound_ThrowsException() {

        when(userRepository.findByEmail("nonexistent@claimx.com"))
                .thenReturn(Optional.empty());


        assertThrows(UserNotFoundException.class, () -> {
            managerService.getPendingClaims("nonexistent@claimx.com");
        });

        verify(expenseClaimRepository, never()).findByStatus(any());

        logger.info("Get pending claims - manager not found test successful");
    }

    @Test
    void testApproveClaim_Success() {

        ApproveClaimRequest approveRequest = new ApproveClaimRequest();
        approveRequest.setComment("All expenses verified");

        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(submittedClaim));
        when(userRepository.findByEmail("flick.manager@claimx.com"))
                .thenReturn(Optional.of(manager));
        when(employeeManagerRepository.findByEmployee(employee))
                .thenReturn(Optional.of(employeeManager));
        when(expenseClaimRepository.save(any(ExpenseClaim.class)))
                .thenReturn(submittedClaim);
        when(expenseItemRepository.findByClaimClaimId(1L))
                .thenReturn(Collections.emptyList());


        ClaimResponse response = managerService.approvePendingClaimById(
                1L,
                approveRequest,
                "flick.manager@claimx.com"
        );


        assertNotNull(response);
        assertEquals(ClaimStatus.APPROVED, submittedClaim.getStatus());
        assertNotNull(submittedClaim.getReviewedDate());
        assertEquals(manager, submittedClaim.getApprovedBy());
        assertEquals("Flick", submittedClaim.getApprovedByName());

        verify(expenseClaimRepository, times(1)).save(submittedClaim);
        verify(auditService, times(1)).logClaimAction(
                eq(submittedClaim),
                eq(manager),
                eq(AuditActions.CLAIM_APPROVED.getValue()),
                eq("SUBMITTED"),
                eq("APPROVED")
        );

        logger.info("Approve claim test successful");
    }

    @Test
    void testApproveClaim_ManagerNotAssigned_ThrowsException() {

        ApproveClaimRequest approveRequest = new ApproveClaimRequest();

        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(submittedClaim));
        when(userRepository.findByEmail("testManager2@claimx.com"))
                .thenReturn(Optional.of(anotherManager));
        when(employeeManagerRepository.findByEmployee(employee))
                .thenReturn(Optional.of(employeeManager)); // Returns John, not Another


        UnauthorizedAccessException exception = assertThrows(
                UnauthorizedAccessException.class,
                () -> managerService.approvePendingClaimById(1L, approveRequest, "testManager2@claimx.com")
        );

        assertEquals(ErrorMessageConstants.UNAUTHORIZED_CLAIM_APPROVAL, exception.getMessage());
        verify(expenseClaimRepository, never()).save(any());

        logger.info("Approve claim not assigned manager test successful");
    }

    @Test
    void testApproveClaim_NotSubmittedStatus_ThrowsException() {

        submittedClaim.setStatus(ClaimStatus.DRAFT);
        ApproveClaimRequest approveRequest = new ApproveClaimRequest();

        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(submittedClaim));
        when(userRepository.findByEmail("flick.manager@claimx.com"))
                .thenReturn(Optional.of(manager));
        when(employeeManagerRepository.findByEmployee(employee))
                .thenReturn(Optional.of(employeeManager));


        InvalidClaimStatus exception = assertThrows(
                InvalidClaimStatus.class,
                () -> managerService.approvePendingClaimById(1L, approveRequest, "flick.manager@claimx.com")
        );

        assertTrue(exception.getMessage().contains("SUBMITTED"));
        verify(expenseClaimRepository, never()).save(any());

        logger.info("Approve claim wrong status test successful");
    }

    @Test
    void testRejectClaim_Success() {

        RejectClaimRequest rejectRequest = new RejectClaimRequest();
        rejectRequest.setComment("cant reimburse according to new policy");

        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(submittedClaim));
        when(userRepository.findByEmail("flick.manager@claimx.com"))
                .thenReturn(Optional.of(manager));
        when(employeeManagerRepository.findByEmployee(employee))
                .thenReturn(Optional.of(employeeManager));
        when(expenseClaimRepository.save(any(ExpenseClaim.class)))
                .thenReturn(submittedClaim);
        when(expenseItemRepository.findByClaimClaimId(1L))
                .thenReturn(Collections.emptyList());


        ClaimResponse response = managerService.rejectPendingClaimById(
                1L,
                rejectRequest,
                "flick.manager@claimx.com"
        );

        assertNotNull(response);
        assertEquals(ClaimStatus.REJECTED, submittedClaim.getStatus());
        assertNotNull(submittedClaim.getReviewedDate());
        assertEquals("cant reimburse according to new policy", submittedClaim.getReviewComment());
        assertEquals(manager, submittedClaim.getApprovedBy());
        assertEquals("Flick", submittedClaim.getApprovedByName());

        verify(expenseClaimRepository, times(1)).save(submittedClaim);
        verify(auditService, times(1)).logClaimAction(
                eq(submittedClaim),
                eq(manager),
                eq(AuditActions.CLAIM_REJECTED.getValue()),
                anyString(),
                eq("REJECTED")
        );

        logger.info("Reject claim test successful");
    }

    @Test
    void testRejectClaim_NotSubmittedStatus_ThrowsException() {

        submittedClaim.setStatus(ClaimStatus.DRAFT);
        RejectClaimRequest rejectClaimRequest = new RejectClaimRequest();

        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(submittedClaim));
        when(userRepository.findByEmail("flick.manager@claimx.com"))
                .thenReturn(Optional.of(manager));
        when(employeeManagerRepository.findByEmployee(employee))
                .thenReturn(Optional.of(employeeManager));


        InvalidClaimStatus exception = assertThrows(
                InvalidClaimStatus.class,
                () -> managerService.rejectPendingClaimById(1L, rejectClaimRequest, "flick.manager@claimx.com")
        );

        assertTrue(exception.getMessage().contains("SUBMITTED"));
        verify(expenseClaimRepository, never()).save(any());

        logger.info("Reject claim wrong status test successful");
    }

    @Test
    void testRejectClaim_ManagerNotAssigned_ThrowsException() {

        RejectClaimRequest rejectClaimRequest = new RejectClaimRequest();

        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(submittedClaim));
        when(userRepository.findByEmail("testManager2@claimx.com"))
                .thenReturn(Optional.of(anotherManager));
        when(employeeManagerRepository.findByEmployee(employee))
                .thenReturn(Optional.of(employeeManager)); // Returns John, not Another


        UnauthorizedAccessException exception = assertThrows(
                UnauthorizedAccessException.class,
                () -> managerService.rejectPendingClaimById(1L, rejectClaimRequest, "testManager2@claimx.com")
        );

        assertEquals(ErrorMessageConstants.UNAUTHORIZED_CLAIM_APPROVAL, exception.getMessage());
        verify(expenseClaimRepository, never()).save(any());

        logger.info("Reject claim not assigned manager test successful");
    }





}
