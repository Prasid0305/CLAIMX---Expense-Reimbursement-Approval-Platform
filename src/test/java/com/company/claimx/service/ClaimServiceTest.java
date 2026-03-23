package com.company.claimx.service;

import com.company.claimx.dto.request.CreateClaimRequest;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.entity.EmployeeManager;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.entity.User;
import com.company.claimx.enums.ClaimStatus;
import com.company.claimx.enums.UserRole;
import com.company.claimx.exception.UserNotFoundException;
import com.company.claimx.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest
@AutoConfigureMockMvc
public class ClaimServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ClaimServiceTest.class);

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
    private ClaimService claimService;

    @Mock
    private AuditService auditService;

    private User employee;
    private User manager;
    private ExpenseClaim claim;
    private CreateClaimRequest createClaimRequest;
    private EmployeeManager employeeManager;

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
        manager.setEmail("flick.employee@claimx.com");
        manager.setEmployeeCode("MGR001");
        manager.setName("Flick");
        manager.setRole(UserRole.MANAGER);
        manager.setIsActive(true);
        manager.setCreatedAt(LocalDateTime.now());


        employeeManager = new EmployeeManager();
        employeeManager.setEmployee(employee);
        employeeManager.setManager(manager);

        createClaimRequest = new CreateClaimRequest();
        createClaimRequest.setTitle("Test Claim");



        claim = new ExpenseClaim();
        claim.setClaimId(1L);
        claim.setClaimNumber("CLM-2026-01012");
        claim.setTitle("BusinessTrip");
        claim.setEmployee(employee);

        claim.setTotalAmount(BigDecimal.ZERO);
        claim.setStatus(ClaimStatus.DRAFT);
        claim.setCreatedAt(LocalDateTime.now());

    }


    @Test
    public void testCreateClaimSuccess(){

        when(userRepository.findByEmail("pedri.employee@claimx.com")).thenReturn(Optional.of(employee));

        when(employeeManagerRepository.findByEmployee(employee)).thenReturn(Optional.of(employeeManager));

        when(expenseClaimRepository.getNextClaimSequence()).thenReturn(1012L);

        when(expenseClaimRepository.save(any(ExpenseClaim.class)))
                .thenReturn(claim);

        when(expenseItemRepository.findByClaimClaimId(1L))
                .thenReturn(java.util.Collections.emptyList());

        ClaimResponse claimResponse = claimService.createClaim(createClaimRequest, "pedri.employee@claimx.com");

        assertNotNull(claimResponse,"response must not be null");

        assertEquals(1L,claimResponse.getClaimId());

        assertEquals("CLM-2026-01012",claimResponse.getClaimNumber());
        assertEquals("BusinessTrip",claimResponse.getTitle());
        assertEquals("Pedri",claimResponse.getEmployeeName());
        assertEquals("Flick", claimResponse.getManagerName());
        assertEquals(BigDecimal.ZERO,claimResponse.getTotalAmount());
        assertEquals(ClaimStatus.DRAFT,claimResponse.getStatus());

        verify(userRepository, times(1)).findByEmail("pedri.employee@claimx.com");
        verify(employeeManagerRepository, times(2)).findByEmployee(employee);
        verify(expenseClaimRepository, times(1)).save(any(ExpenseClaim.class));
        verify(auditService, times(1)).logClaimAction(any(ExpenseClaim.class), eq(employee), eq("created claim"), eq(null), eq("DRAFT"), eq("claim created"));

        logger.info(" claim created , test successful");
    }

    @Test
    void createClaim_USerNotFound_throwException(){
        when(userRepository.findByEmail("emptyemail@claimx.com"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, ()->{
            claimService.createClaim(new CreateClaimRequest(),"emptyemail@claimx.com");
        });

        verify(userRepository,times(1)).findByEmail("emptyemail@claimx.com");
        verify(expenseClaimRepository,never()).save(any(ExpenseClaim.class));
    }

    @Test
    void createClaim_NoManagerAssigned_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail("test.employee@claimx.com"))
                .thenReturn(Optional.of(employee));
        when(employeeManagerRepository.findByEmployee(employee))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            claimService.createClaim(createClaimRequest, "test.employee@claimx.com");
        });

        assertEquals("Employee's manager not found", exception.getMessage());
        verify(expenseClaimRepository, never()).save(any(ExpenseClaim.class));
    }

    @Test
    void getClaimById_Success() {
        // Arrange
        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(claim));
        when(userRepository.findByEmail("pedri.employee@claimx.com"))
                .thenReturn(Optional.of(employee));
        when(employeeManagerRepository.findByEmployee(employee))
                .thenReturn(Optional.of(employeeManager));
        when(expenseItemRepository.findByClaimClaimId(1L))
                .thenReturn(java.util.Collections.emptyList());

        // Act
        ClaimResponse response = claimService.getClaimById(1L, "pedri.employee@claimx.com");

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getClaimId());
        assertEquals("BusinessTrip", response.getTitle());

        verify(expenseClaimRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail("pedri.employee@claimx.com");
    }


    @Test
    void submitClaim_Success() {
        // Arrange
        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(claim));
        when(userRepository.findByEmail("pedri.employee@claimx.com"))
                .thenReturn(Optional.of(employee));
        when(expenseItemRepository.countByClaimClaimId(1L))
                .thenReturn(3L); // Has items
        when(expenseClaimRepository.save(any(ExpenseClaim.class)))
                .thenReturn(claim);
        when(employeeManagerRepository.findByEmployee(employee))
                .thenReturn(Optional.of(employeeManager));
        when(expenseItemRepository.findByClaimClaimId(1L))
                .thenReturn(java.util.Collections.emptyList());

        // Act
        ClaimResponse response = claimService.submitClaim(1L, "pedri.employee@claimx.com");

        // Assert
        assertNotNull(response);
        assertEquals(ClaimStatus.SUBMITTED, claim.getStatus());
        assertNotNull(claim.getSubmittedAt());

        verify(expenseClaimRepository, times(1)).save(claim);
        verify(auditService, times(1)).logClaimAction(
                eq(claim),
                eq(employee),
                eq("claim submitted"),
                any(),
                eq("Submitted"),
                eq("claim submitted")
        );

    }








}
