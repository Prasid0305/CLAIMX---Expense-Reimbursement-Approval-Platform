package com.company.claimx.service;

import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.entity.EmployeeManager;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.entity.ExpenseItem;
import com.company.claimx.entity.User;
import com.company.claimx.enums.AuditActions;
import com.company.claimx.enums.ClaimStatus;
import com.company.claimx.enums.UserRole;
import com.company.claimx.exception.ClaimNotFoundException;
import com.company.claimx.exception.InvalidClaimStatus;
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
public class FinanceServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(FinanceServiceTest.class);

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
    private FinanceService financeService;

    @Mock
    private AuditService auditService;

    private User employee;
    private User manager;
    private User financeUser;
    private ExpenseClaim approvedClaim;

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

        financeUser= new User();
        financeUser.setId(20L);
        financeUser.setEmail("testFinance@claimx.com");
        financeUser.setEmployeeCode("FIN001");
        financeUser.setName("testFinance");
        financeUser.setRole(UserRole.FINANCE);
        financeUser.setIsActive(true);
        financeUser.setCreatedAt(LocalDateTime.now());


        employeeManager = new EmployeeManager();
        employeeManager.setEmployee(employee);
        employeeManager.setManager(manager);






        approvedClaim = new ExpenseClaim();
        approvedClaim.setTitle("Test Claim");
        approvedClaim.setClaimId(1L);
        approvedClaim.setClaimNumber("CLM-2026-00001");
        approvedClaim.setTitle("BusinessTrip");
        approvedClaim.setEmployee(employee);

        approvedClaim.setTotalAmount(new BigDecimal("50000.00"));
        approvedClaim.setStatus(ClaimStatus.APPROVED);
        approvedClaim.setCreatedAt(LocalDateTime.now());


    }

    @Test
    void testGetApprovedClaims_Success() {

        ExpenseClaim claim2 = new ExpenseClaim();
        claim2.setClaimId(2L);
        claim2.setClaimNumber("CLM-2026-00002");
        claim2.setEmployee(employee);
        claim2.setApprovedBy(manager);
        claim2.setApprovedByName("testManager");
        claim2.setStatus(ClaimStatus.APPROVED);
        claim2.setTotalAmount(new BigDecimal("30000.00"));

        List<ExpenseClaim> approvedClaims = Arrays.asList(approvedClaim, claim2);

        when(userRepository.findByEmail("finance@claimx.com"))
                .thenReturn(Optional.of(financeUser));
        when(expenseClaimRepository.findByStatus(ClaimStatus.APPROVED))
                .thenReturn(approvedClaims);
        when(employeeManagerRepository.findByEmployee(employee))
                .thenReturn(Optional.of(employeeManager));
        when(expenseItemRepository.findByClaimClaimId(1L))
                .thenReturn(Collections.emptyList());


        List<ClaimResponse> responses = financeService.getApprovedClaim("finance@claimx.com");


        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(ClaimStatus.APPROVED, responses.get(0).getStatus());
        assertEquals(new BigDecimal("50000.00"), responses.get(0).getTotalAmount());

        verify(expenseClaimRepository, times(1)).findByStatus(ClaimStatus.APPROVED);

        logger.info("Get approved claims test successful");
    }

    @Test
    void testGetApprovedClaims_UserNotFound_ThrowsException() {

        when(userRepository.findByEmail("nonexistent@claimx.com"))
                .thenReturn(Optional.empty());


        assertThrows(UserNotFoundException.class, () -> {
            financeService.getApprovedClaim("nonexistent@claimx.com");
        });

        verify(expenseClaimRepository, never()).findByStatus(any());

        logger.info("Get approved claims - user not found test successful");
    }

    @Test
    void testPayClaim_Success() {

        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(approvedClaim));
        when(userRepository.findByEmail("finance@claimx.com"))
                .thenReturn(Optional.of(financeUser));
        when(expenseClaimRepository.save(any(ExpenseClaim.class)))
                .thenReturn(approvedClaim);
        when(employeeManagerRepository.findByEmployee(employee))
                .thenReturn(Optional.of(employeeManager));
        when(expenseItemRepository.findByClaimClaimId(1L))
                .thenReturn(Collections.emptyList());


        ClaimResponse response = financeService.payClaims(1L, "finance@claimx.com");


        assertNotNull(response);
        assertEquals(ClaimStatus.PAID, approvedClaim.getStatus());

        verify(expenseClaimRepository, times(1)).save(approvedClaim);
        verify(auditService, times(1)).logClaimAction(
                eq(approvedClaim),
                eq(financeUser),
                eq(AuditActions.CLAIM_PAID.getValue()),
                eq("APPROVED"),
                eq("PAID")
        );

        logger.info("Pay claim test successful");
    }

    @Test
    void testPayClaim_ClaimNotFound_ThrowsException() {

        when(expenseClaimRepository.findById(999L))
                .thenReturn(Optional.empty());


        assertThrows(ClaimNotFoundException.class, () -> {
            financeService.payClaims(999L, "finance@claimx.com");
        });

        verify(expenseClaimRepository, never()).save(any());

        logger.info("Pay claim - claim not found test successful");
    }

    @Test
    void testPayClaim_NotApprovedStatus_ThrowsException() {

        approvedClaim.setStatus(ClaimStatus.SUBMITTED);

        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(approvedClaim));
        when(userRepository.findByEmail("finance@claimx.com"))
                .thenReturn(Optional.of(financeUser));


        InvalidClaimStatus exception = assertThrows(
                InvalidClaimStatus.class,
                () -> financeService.payClaims(1L, "finance@claimx.com")
        );

        assertTrue(exception.getMessage().contains("APPROVED"));
        verify(expenseClaimRepository, never()).save(any());

        logger.info("Pay claim - not approved status test successful");
    }

    @Test
    void testGetAllPaidClaims_Success() {

        approvedClaim.setStatus(ClaimStatus.PAID);

        ExpenseClaim paidClaim2 = new ExpenseClaim();
        paidClaim2.setClaimId(2L);
        paidClaim2.setClaimNumber("CLM-2026-00002");
        paidClaim2.setEmployee(employee);
        paidClaim2.setApprovedBy(manager);
        paidClaim2.setApprovedByName("Test Manager");
        paidClaim2.setStatus(ClaimStatus.PAID);
        paidClaim2.setTotalAmount(new BigDecimal("25000.00"));

        List<ExpenseClaim> paidClaims = Arrays.asList(approvedClaim, paidClaim2);

        when(userRepository.findByEmail("finance@claimx.com"))
                .thenReturn(Optional.of(financeUser));
        when(expenseClaimRepository.findByStatus(ClaimStatus.PAID))
                .thenReturn(paidClaims);
        when(employeeManagerRepository.findByEmployee(employee))
                .thenReturn(Optional.of(employeeManager));
        when(expenseItemRepository.findByClaimClaimId(anyLong()))
                .thenReturn(Collections.emptyList());


        List<ClaimResponse> responses = financeService.getAllPaidClaim("finance@claimx.com");


        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(ClaimStatus.PAID, responses.get(0).getStatus());
        assertEquals(ClaimStatus.PAID, responses.get(1).getStatus());

        verify(expenseClaimRepository, times(1)).findByStatus(ClaimStatus.PAID);

        logger.info("Get all paid claims test successful");
    }



}


