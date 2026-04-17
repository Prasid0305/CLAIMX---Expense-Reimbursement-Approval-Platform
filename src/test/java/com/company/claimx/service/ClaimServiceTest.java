package com.company.claimx.service;

import com.company.claimx.constants.ErrorMessageConstants;
import com.company.claimx.dto.request.CreateClaimRequest;
import com.company.claimx.dto.request.UpdateClaimRequest;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.entity.EmployeeManager;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.entity.User;
import com.company.claimx.enums.AuditActions;
import com.company.claimx.enums.ClaimStatus;
import com.company.claimx.enums.UserRole;
import com.company.claimx.exception.InvalidClaimStatus;
import com.company.claimx.exception.UnauthorizedAccessException;
import com.company.claimx.exception.UserNotFoundException;
import com.company.claimx.mapper.ClaimMapper;
import com.company.claimx.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Mock
    private ClaimMapper claimMapper;

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

    public ClaimResponse createMockClaimResponse(ExpenseClaim claim) {
        return ClaimResponse.builder()
                .claimId(claim.getClaimId())
                .claimNumber(claim.getClaimNumber())
                .title(claim.getTitle())
                .status(claim.getStatus())
                .totalAmount(claim.getTotalAmount())
                .employeeId(claim.getEmployee().getId())
                .employeeCode(claim.getEmployee().getEmployeeCode())
                .employeeName(claim.getEmployee().getName())
                .managerId(manager != null ? manager.getId() : null)
                .managerName(manager != null ? manager.getName() : null)
                .items(Collections.emptyList())
                .build();
    }

    public List<ClaimResponse> claimResponseList(List<ExpenseClaim> claims) {
        return claims.stream()
                .map(claim -> ClaimResponse.builder()
                        .claimId(claim.getClaimId())
                        .claimNumber(claim.getClaimNumber())
                        .title(claim.getTitle())
                        .status(claim.getStatus())
                        .totalAmount(claim.getTotalAmount())
                        .employeeId(claim.getEmployee().getId())
                        .employeeCode(claim.getEmployee().getEmployeeCode())
                        .employeeName(claim.getEmployee().getName())
                        .managerId(manager != null ? manager.getId() : null)
                        .managerName(manager != null ? manager.getName() : null)
                        .items(Collections.emptyList())
                        .build())
                .collect(Collectors.toList());
    }



    @Test
    public void testCreateClaimSuccess(){

        when(userRepository.findByEmail("pedri.employee@claimx.com")).thenReturn(Optional.of(employee));

        when(employeeManagerRepository.existsByEmployee(employee)).thenReturn(true);



        when(expenseClaimRepository.save(any(ExpenseClaim.class)))
                .thenReturn(claim);

        when(expenseItemRepository.findByClaimClaimId(1L))
                .thenReturn(java.util.Collections.emptyList());

        ClaimResponse mockResponse = createMockClaimResponse(claim);

        when(claimMapper.toClaimResponse(claim)).thenReturn(mockResponse);


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
        verify(employeeManagerRepository, times(1)).existsByEmployee(employee);
        verify(expenseClaimRepository, times(1)).save(any(ExpenseClaim.class));
        verify(claimMapper, times(1)).toClaimResponse(any(ExpenseClaim.class));
        verify(auditService, times(1)).logClaimAction(
                any(ExpenseClaim.class),
                eq(employee),
                eq(AuditActions.CLAIM_CREATED.getValue()),
                eq(null),
                eq("DRAFT"));

        logger.info(" claim created , test successful");
    }

    @Test
    void createClaim_UserNotFound_throwException(){
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

        when(userRepository.findByEmail("test.employee@claimx.com"))
                .thenReturn(Optional.of(employee));
        when(employeeManagerRepository.findByEmployee(employee))
                .thenReturn(Optional.empty());


        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            claimService.createClaim(createClaimRequest, "test.employee@claimx.com");
        });

        assertEquals(ErrorMessageConstants.EMPLOYEE_MANAGER_NOT_FOUND, exception.getMessage());
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

        ClaimResponse mockResponse = createMockClaimResponse(claim);

        when(claimMapper.toClaimResponse(claim)).thenReturn(mockResponse);


        ClaimResponse response = claimService.getClaimById(1L, "pedri.employee@claimx.com");


        assertNotNull(response);
        assertEquals(1L, response.getClaimId());
        assertEquals("BusinessTrip", response.getTitle());

        verify(expenseClaimRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail("pedri.employee@claimx.com");
        verify(claimMapper, times(1)).toClaimResponse(any(ExpenseClaim.class));


    }


    @Test
    void submitClaim_Success() {

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

        ClaimResponse mockResponse = createMockClaimResponse(claim);

        when(claimMapper.toClaimResponse(claim)).thenReturn(mockResponse);


        ClaimResponse response = claimService.submitClaim(1L, "pedri.employee@claimx.com");


        assertNotNull(response);
        assertEquals(ClaimStatus.SUBMITTED, claim.getStatus());
        assertNotNull(claim.getSubmittedAt());

        verify(expenseClaimRepository, times(1)).save(claim);
        verify(claimMapper, times(1)).toClaimResponse(any(ExpenseClaim.class));
        verify(auditService, times(1)).logClaimAction(
                eq(claim),
                eq(employee),
                eq(AuditActions.CLAIM_SUBMITTED.getValue()),
                eq("DRAFT"),
                eq("SUBMITTED")
        );

    }

    @Test
    public void testGetAllMyClaims() {

        ExpenseClaim claim2 = new ExpenseClaim();
        claim2.setClaimId(2L);
        claim2.setClaimNumber("CLM-2026-01013");
        claim2.setTitle("Office Supplies");
        claim2.setEmployee(employee);
        claim2.setStatus(ClaimStatus.SUBMITTED);
        claim2.setTotalAmount(new BigDecimal("5000.00"));
        claim2.setCreatedAt(LocalDateTime.now());

        ExpenseClaim claim3 = new ExpenseClaim();
        claim3.setClaimId(3L);
        claim3.setClaimNumber("CLM-2026-01014");
        claim3.setTitle("Client Meeting");
        claim3.setEmployee(employee);
        claim3.setStatus(ClaimStatus.APPROVED);
        claim3.setTotalAmount(new BigDecimal("8000.00"));
        claim3.setCreatedAt(LocalDateTime.now());

        List<ExpenseClaim> claims = Arrays.asList(claim, claim2, claim3);

        when(userRepository.findByEmail("pedri.employee@claimx.com")).thenReturn(Optional.of(employee));
        when(employeeManagerRepository.existsByEmployee(employee)).thenReturn(true);
        when(expenseClaimRepository.findByEmployee(employee)).thenReturn(claims);
        when(expenseItemRepository.findByClaimClaimId(anyLong())).thenReturn(Collections.emptyList());

        List<ClaimResponse> mockResponses = claimResponseList(claims);

        when(claimMapper.toClaimResponseList(claims))
                .thenReturn(mockResponses);


        List<ClaimResponse> responses = claimService.getAllMyClaims("pedri.employee@claimx.com");


        assertNotNull(responses, "Response list must not be null");
        assertEquals(3, responses.size(), "Should return 3 claims");

        assertEquals(1L, responses.get(0).getClaimId());
        assertEquals("BusinessTrip", responses.get(0).getTitle());
        assertEquals(ClaimStatus.DRAFT, responses.get(0).getStatus());

        assertEquals(2L, responses.get(1).getClaimId());
        assertEquals("Office Supplies", responses.get(1).getTitle());
        assertEquals(ClaimStatus.SUBMITTED, responses.get(1).getStatus());

        assertEquals(3L, responses.get(2).getClaimId());
        assertEquals("Client Meeting", responses.get(2).getTitle());
        assertEquals(ClaimStatus.APPROVED, responses.get(2).getStatus());

        verify(userRepository, times(1)).findByEmail("pedri.employee@claimx.com");
        verify(expenseClaimRepository, times(1)).findByEmployee(employee);
        verify(claimMapper, times(1)).toClaimResponseList(anyList());

        logger.info("Get all my claims test successful");
    }

    @Test
    public void testGetAllMyClaimsByStatus() {

        ExpenseClaim draftClaim2 = new ExpenseClaim();
        draftClaim2.setClaimId(2L);
        draftClaim2.setClaimNumber("CLM-2026-01013");
        draftClaim2.setTitle("Another Draft");
        draftClaim2.setEmployee(employee);
        draftClaim2.setStatus(ClaimStatus.DRAFT);
        draftClaim2.setTotalAmount(BigDecimal.ZERO);
        draftClaim2.setCreatedAt(LocalDateTime.now());

        List<ExpenseClaim> draftClaims = Arrays.asList(claim, draftClaim2);

        when(userRepository.findByEmail("pedri.employee@claimx.com"))
                .thenReturn(Optional.of(employee));
        when(employeeManagerRepository.existsByEmployee(employee)).thenReturn(true);
        when(expenseClaimRepository.findByEmployeeAndStatus(employee, ClaimStatus.DRAFT))
                .thenReturn(draftClaims);
        when(employeeManagerRepository.findByEmployee(employee))
                .thenReturn(Optional.of(employeeManager));
        when(expenseItemRepository.findByClaimClaimId(anyLong()))
                .thenReturn(Collections.emptyList());

        List<ClaimResponse> mockResponses = claimResponseList(draftClaims);

        when(claimMapper.toClaimResponseList(draftClaims))
                .thenReturn(mockResponses);

        List<ClaimResponse> responses = claimService.getAllMyClaimsByStatus(
                "pedri.employee@claimx.com",
                ClaimStatus.DRAFT
        );


        assertNotNull(responses, "Response list must not be null");
        assertEquals(2, responses.size(), "Should return 2 DRAFT claims");

        assertEquals(ClaimStatus.DRAFT, responses.get(0).getStatus());
        assertEquals(ClaimStatus.DRAFT, responses.get(1).getStatus());

        assertEquals("BusinessTrip", responses.get(0).getTitle());
        assertEquals("Another Draft", responses.get(1).getTitle());

        verify(userRepository, times(1)).findByEmail("pedri.employee@claimx.com");
        verify(expenseClaimRepository, times(1)).findByEmployeeAndStatus(employee, ClaimStatus.DRAFT);
        verify(claimMapper, times(1)).toClaimResponseList(anyList());

        logger.info("Get claims by status test successful");
    }

    @Test
    public void testDeleteClaimSuccess(){
        when(userRepository.findByEmail("pedri@claimx.com")).thenReturn(Optional.of(employee));

        when(expenseClaimRepository.findById(1L)).thenReturn(Optional.of(claim));

        claimService.deleteClaim(1L,"pedri@claimx.com");

        verify(userRepository, times(1)).findByEmail("pedri@claimx.com");
        verify(expenseClaimRepository, times(1)).findById(1L);
        verify(auditLogRepository, times(1)).deleteByClaimClaimId(1L);
        verify(expenseClaimRepository,times(1)).delete(claim);
        verify(expenseItemRepository,times(1)).deleteByClaimClaimId(1L);

        logger.info("Delet claim test successful");
    }

    @Test
    public void testDeleteClaim_UnauthorizedOwner_ThrowsException() {

        User anotherUser = new User();
        anotherUser.setId(99L);
        anotherUser.setEmail("another.employee@claimx.com");
        anotherUser.setName("Another Employee");

        when(userRepository.findByEmail("another.employee@claimx.com"))
                .thenReturn(Optional.of(anotherUser));
        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(claim));


        UnauthorizedAccessException exception = assertThrows(
                UnauthorizedAccessException.class,
                () -> claimService.deleteClaim(1L, "another.employee@claimx.com")
        );

        assertEquals("You can only delete your own claims", exception.getMessage());

        verify(userRepository, times(1)).findByEmail("another.employee@claimx.com");
        verify(expenseClaimRepository, times(1)).findById(1L);
        verify(expenseClaimRepository, never()).delete(any(ExpenseClaim.class));

        logger.info("Delete claim, Unauthorized owner test successful");
    }

    @Test
    public void testDeleteClaim_NotDraftStatus_ThrowsException() {

        claim.setStatus(ClaimStatus.SUBMITTED);

        when(userRepository.findByEmail("pedri.employee@claimx.com"))
                .thenReturn(Optional.of(employee));
        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(claim));


        InvalidClaimStatus exception = assertThrows(
                InvalidClaimStatus.class,
                () -> claimService.deleteClaim(1L, "pedri.employee@claimx.com")
        );

        assertEquals(ErrorMessageConstants.INVALID_CLAIM_STATUS_FOR_CLAIM_DELETION, exception.getMessage());

        verify(expenseClaimRepository, never()).delete(any(ExpenseClaim.class));

        logger.info("Delete claim, not draft status test successful");
    }

    @Test
    public void testUpdateClaimSuccess() {

        UpdateClaimRequest updateRequest = new UpdateClaimRequest();
        updateRequest.setTitle("Updated Business Trip");

        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(claim));
        when(userRepository.findByEmail("pedri.employee@claimx.com"))
                .thenReturn(Optional.of(employee));

        ExpenseClaim updatedClaim = new ExpenseClaim();
        updatedClaim.setClaimId(1L);
        updatedClaim.setClaimNumber("CLM-2026-01012");
        updatedClaim.setTitle("Updated Business Trip");
        updatedClaim.setEmployee(employee);
        updatedClaim.setStatus(ClaimStatus.DRAFT);
        updatedClaim.setTotalAmount(BigDecimal.ZERO);
        updatedClaim.setCreatedAt(LocalDateTime.now());

        when(expenseClaimRepository.save(any(ExpenseClaim.class)))
                .thenReturn(updatedClaim);
        when(employeeManagerRepository.findByEmployee(employee))
                .thenReturn(Optional.of(employeeManager));
        when(expenseItemRepository.findByClaimClaimId(1L))
                .thenReturn(Collections.emptyList());

        ClaimResponse mockResponse = createMockClaimResponse(updatedClaim);

        when(claimMapper.toClaimResponse(updatedClaim)).thenReturn(mockResponse);

        ClaimResponse response = claimService.updateClaim(1L, updateRequest, "pedri.employee@claimx.com");


        assertNotNull(response, "Response must not be null");
        assertEquals(1L, response.getClaimId());
        assertEquals("Updated Business Trip", response.getTitle());
        assertEquals(ClaimStatus.DRAFT, response.getStatus());

        verify(expenseClaimRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail("pedri.employee@claimx.com");
        verify(claimMapper, times(1)).toClaimResponse(any(ExpenseClaim.class));
        verify(expenseClaimRepository, times(1)).save(any(ExpenseClaim.class));

        logger.info("Update claim test successful");
    }

    @Test
    public void testUpdateClaim_UnauthorizedOwner_ThrowsException() {

        User anotherUser = new User();
        anotherUser.setId(99L);
        anotherUser.setEmail("another.employee@claimx.com");

        UpdateClaimRequest updateRequest = new UpdateClaimRequest();
        updateRequest.setTitle("Updated Title");

        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(claim));
        when(userRepository.findByEmail("another.employee@claimx.com"))
                .thenReturn(Optional.of(anotherUser));


        UnauthorizedAccessException exception = assertThrows(
                UnauthorizedAccessException.class,
                () -> claimService.updateClaim(1L, updateRequest, "another.employee@claimx.com")
        );

        assertEquals(ErrorMessageConstants.UNAUTHORIZED_CLAIM_UPDATING, exception.getMessage());

        verify(expenseClaimRepository, never()).save(any(ExpenseClaim.class));

        logger.info("Update claim Unauthorized test successful");
    }

    @Test
    public void testUpdateClaim_NotDraftStatus_ThrowsException() {

        claim.setStatus(ClaimStatus.SUBMITTED);

        UpdateClaimRequest updateRequest = new UpdateClaimRequest();
        updateRequest.setTitle("Updated Title");

        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(claim));
        when(userRepository.findByEmail("pedri.employee@claimx.com"))
                .thenReturn(Optional.of(employee));


        InvalidClaimStatus exception = assertThrows(
                InvalidClaimStatus.class,
                () -> claimService.updateClaim(1L, updateRequest, "pedri.employee@claimx.com")
        );

        assertTrue(exception.getMessage().contains("DRAFT"));

        verify(expenseClaimRepository, never()).save(any(ExpenseClaim.class));

        logger.info("Update claim, not draft status test successful");
    }








}
