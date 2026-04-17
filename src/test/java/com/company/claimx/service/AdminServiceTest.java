package com.company.claimx.service;

import com.company.claimx.dto.response.AuditLogResponse;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.dto.response.UserResponse;
import com.company.claimx.dto.response.UserWithClaimResponse;
import com.company.claimx.entity.*;
import com.company.claimx.enums.Category;
import com.company.claimx.enums.ClaimStatus;
import com.company.claimx.enums.UserRole;
import com.company.claimx.exception.UserNotFoundException;
import com.company.claimx.mapper.ClaimMapper;
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
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

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

    @Mock
    private ClaimMapper claimMapper;

    @InjectMocks
    private AdminServices adminServices;

    @Mock
    private AuditService auditService;


    private User employee;
    private User manager;
    private User adminUser;
    private User anotherManager;
    private ExpenseClaim submittedClaim;

    private EmployeeManager employeeManager;
    private ExpenseItem expenseItem;

    @BeforeEach
    public void setUp(){

        adminUser = new User();
        adminUser.setId(100L);
        adminUser.setEmail("admin@claimx.com");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setName("Admin");



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


    }

    @Test
    void testGetAllUsers_Success(){
        User user2 = new User();
        user2.setId(6L);
        user2.setEmail("testUser2@claimx.com");
        user2.setEmployeeCode("EMP006");
        user2.setName("testUser2");
        user2.setRole(UserRole.EMPLOYEE);
        user2.setIsActive(true);
        user2.setCreatedAt(LocalDateTime.now());

        List<User> users = Arrays.asList(employee, user2, manager, anotherManager);

        List<UserResponse> userResponses = users.stream()
                        .map(user -> UserResponse.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .employeeCode(user.getEmployeeCode())
                                .role(user.getRole())
                                .isActive(user.getIsActive())
                                .createdAt(user.getCreatedAt())
                                .build())
                                .collect(Collectors.toList());

        when(userRepository.findByEmail("admin@claimx.com"))
                .thenReturn(Optional.of(adminUser));
        when(userRepository.findAll())
                .thenReturn(users);
        when(claimMapper.userResponsesList(users))
                .thenReturn(userResponses);




        List<UserResponse> responses = adminServices.getAllUsers("admin@claimx.com");


        assertNotNull(responses);
        assertEquals(4, responses.size());
        assertEquals("Pedri", responses.get(0).getName());
        assertEquals(1L, responses.get(0).getId());


        verify(userRepository, times(1)).findAll();
        verify(claimMapper, times(1)).userResponsesList(users);

        logger.info("get all users test successful");

    }

    @Test
    void testGetAllUsers_AdminNotFound_ThrowsException(){
        when(userRepository.findByEmail("wrongEmail@claimx.com"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, ()->{
            adminServices.getAllUsers("wrongEmail@claimx.com");
        });
        verify(userRepository, never()).findAll();

        logger.info("get all users admin not found test successful");
    }




    @Test
    void testGetUserWithClaims_Success() {

        ExpenseClaim claim2 = new ExpenseClaim();
        claim2.setClaimId(2L);
        claim2.setClaimNumber("CLM-2026-00002");
        claim2.setEmployee(employee);
        claim2.setStatus(ClaimStatus.DRAFT);
        claim2.setTotalAmount(BigDecimal.ZERO);

        ExpenseClaim claim3 = new ExpenseClaim();
        claim3.setClaimId(3L);
        claim3.setClaimNumber("CLM-2026-00003");
        claim3.setEmployee(employee);
        claim3.setStatus(ClaimStatus.APPROVED);
        claim3.setApprovedBy(manager);
        claim3.setApprovedByName("flick");
        claim3.setTotalAmount(new BigDecimal("30000.00"));

        List<ExpenseClaim> claims = Arrays.asList(submittedClaim, claim2, claim3);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(employee));
        when(expenseClaimRepository.findByEmployee(employee))
                .thenReturn(claims);


        List<ClaimResponse> mockClaimResponses = claims.stream()
                .map(claim -> ClaimResponse.builder()
                        .claimId(claim.getClaimId())
                        .claimNumber(claim.getClaimNumber())
                        .title(claim.getTitle())
                        .status(claim.getStatus())
                        .totalAmount(claim.getTotalAmount())
                        .employeeId(claim.getEmployee().getId())
                        .employeeCode(claim.getEmployee().getEmployeeCode())
                        .employeeName(claim.getEmployee().getName())
                        .managerId(manager.getId())
                        .managerName(manager.getName())
                        .createdAt(claim.getCreatedAt())
                        .submittedAt(claim.getSubmittedAt())
                        .reviewedDate(claim.getReviewedDate())
                        .reviewComment(claim.getReviewComment())
                        .items(Collections.emptyList())
                        .build())
                .collect(Collectors.toList());

        when(claimMapper.toClaimResponseList(claims))
                .thenReturn(mockClaimResponses);


        UserWithClaimResponse response = adminServices.getUserWithClaims(1L);


        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("Pedri", response.getName());
        assertEquals("EMP001", response.getEmployeeCode());
        assertEquals(3, response.getClaims().size());



        verify(userRepository, times(1)).findById(1L);
        verify(expenseClaimRepository, times(1)).findByEmployee(employee);
        verify(claimMapper, times(1)).toClaimResponseList(claims);

        logger.info("Get user with claims test successful");
    }

    @Test
    void testGetUserWithClaims_UserNotFound_ThrowsException() {

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());


        assertThrows(UserNotFoundException.class, () -> {
            adminServices.getUserWithClaims(999L);
        });

        verify(expenseClaimRepository, never()).findByEmployee(any());

        logger.info("Get user with claims - user not found test successful");
    }

    @Test
    void testGetAllClaims_Success(){

        ExpenseClaim claim2 = new ExpenseClaim();
        claim2.setClaimId(2L);
        claim2.setClaimNumber("CLM-2026-00002");
        claim2.setEmployee(employee);
        claim2.setStatus(ClaimStatus.DRAFT);
        claim2.setTotalAmount(BigDecimal.ZERO);

        ExpenseClaim claim3 = new ExpenseClaim();
        claim3.setClaimId(3L);
        claim3.setClaimNumber("CLM-2026-00003");
        claim3.setEmployee(employee);
        claim3.setStatus(ClaimStatus.APPROVED);
        claim3.setApprovedBy(manager);
        claim3.setApprovedByName("flick");
        claim3.setTotalAmount(new BigDecimal("30000.00"));

        List<ExpenseClaim> allClaims = Arrays.asList(submittedClaim, claim2, claim3);


        when(userRepository.findByEmail("admin@claimx.com"))
                .thenReturn(Optional.of(adminUser));
        when(expenseClaimRepository.findAll())
                .thenReturn(allClaims);





        List<ClaimResponse> mockClaimResponses = allClaims.stream()
                .map(claim -> ClaimResponse.builder()
                        .claimId(claim.getClaimId())
                        .claimNumber(claim.getClaimNumber())
                        .title(claim.getTitle())
                        .status(claim.getStatus())
                        .totalAmount(claim.getTotalAmount())
                        .employeeId(claim.getEmployee().getId())
                        .employeeCode(claim.getEmployee().getEmployeeCode())
                        .employeeName(claim.getEmployee().getName())
                        .managerId(manager.getId())
                        .managerName(manager.getName())
                        .createdAt(claim.getCreatedAt())
                        .submittedAt(claim.getSubmittedAt())
                        .reviewedDate(claim.getReviewedDate())
                        .reviewComment(claim.getReviewComment())
                        .items(Collections.emptyList())
                        .build())
                .collect(Collectors.toList());

        when(claimMapper.toClaimResponseList(allClaims))
                .thenReturn(mockClaimResponses);

        List<ClaimResponse> responses = adminServices.getAllClaims("admin@claimx.com");

        assertNotNull(responses);
        assertEquals(3, responses.size());
        assertEquals(ClaimStatus.SUBMITTED, responses.get(0).getStatus());
        assertEquals(ClaimStatus.DRAFT, responses.get(1).getStatus());
        assertEquals(ClaimStatus.APPROVED, responses.get(2).getStatus());




        verify(expenseClaimRepository, times(1)).findAll();
        verify(claimMapper, times(1)).toClaimResponseList(allClaims);

        logger.info("get all claims test successful");

    }

    @Test
    void testGetAllAuditLogs_Success(){
        AuditLog log1 = new AuditLog();
        log1.setLogId(1L);
        log1.setClaim(submittedClaim);
        log1.setPerformedBy(employee);
        log1.setAction("CLAIM_CREATED");
        log1.setOldStatus(null);
        log1.setNewStatus("DRAFT");
        log1.setTimestamp(LocalDateTime.now());

        AuditLog log2 = new AuditLog();
        log2.setLogId(2L);
        log2.setClaim(submittedClaim);
        log2.setPerformedBy(manager);
        log2.setAction("CLAIM_APPROVED");
        log2.setOldStatus("SUBMITTED");
        log2.setNewStatus("APPROVED");
        log2.setTimestamp(LocalDateTime.now());

        List<AuditLog> logs =  Arrays.asList(log1, log2);

        when(userRepository.findByEmail("admin@claimx.com"))
                .thenReturn(Optional.of(adminUser));
        when(auditService.getAllLogs()).thenReturn(logs);

        List<AuditLogResponse> mockAuditResponses = logs.stream()
                .map(log -> AuditLogResponse.builder()
                        .logId(log.getLogId())
                        .claimId(log.getClaim().getClaimId())
                        .claimNumber(log.getClaim().getClaimNumber())
                        .performedBy(log.getPerformedBy().getEmail())
                        .action(log.getAction())
                        .oldStatus(log.getOldStatus())
                        .newStatus(log.getNewStatus())
                        .timestamp(log.getTimestamp())
                        .build())
                .collect(Collectors.toList());

        when(claimMapper.auditLogResponseList(logs))
                .thenReturn(mockAuditResponses);


        List<AuditLogResponse> responses = adminServices.getAllAuditLogs("admin@claimx.com");

        assertNotNull(responses);
        assertEquals(2,responses.size());

        assertEquals("CLAIM_CREATED", responses.get(0).getAction());
        assertEquals("CLAIM_APPROVED", responses.get(1).getAction());
        assertEquals("DRAFT", responses.get(0).getNewStatus());
        assertEquals("APPROVED", responses.get(1).getNewStatus());

        verify(auditService, times(1)).getAllLogs();
        verify(claimMapper, times(1)).auditLogResponseList(logs);

        logger.info("get all logs test successful");
    }
}
