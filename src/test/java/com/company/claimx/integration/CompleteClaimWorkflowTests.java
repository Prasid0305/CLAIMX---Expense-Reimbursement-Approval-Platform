package com.company.claimx.integration;


import com.company.claimx.dto.request.*;
import com.company.claimx.dto.response.*;
import com.company.claimx.entity.AuditLog;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.entity.ExpenseItem;
import com.company.claimx.enums.Category;
import com.company.claimx.enums.ClaimStatus;
import com.company.claimx.repository.AuditLogRepository;
import com.company.claimx.repository.ExpenseClaimRepository;
import com.company.claimx.repository.ExpenseItemRepository;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CompleteClaimWorkflowTests {


    private static final Logger logger = LoggerFactory.getLogger(CompleteClaimWorkflowTests.class);


    @Autowired
    private ExpenseClaimRepository expenseClaimRepository;

    @Autowired
    private ExpenseItemRepository expenseItemRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private static String employeeToken;
    private static String managerToken;

    private  static String financeToken;
    private static String adminToken;
    private static Long claimId;
    private static Long userId;
    private static String status;

    @Test
    @Order(1)
    void step1_LoginAsEmployee() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("prajwal.employee@claimx.com");
        loginRequest.setPassword("prajwal@123");

        ResponseEntity<ApiResponse<LoginResponse>> responseEntity = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ApiResponse<LoginResponse>>() {}
        );

        logger.info("Response Status:{} ", responseEntity.getStatusCode());
        logger.info("Response body: {}", responseEntity.getBody());

        ApiResponse<LoginResponse> loginResponseApiResponse = responseEntity.getBody();

        LoginResponse loginResponse = loginResponseApiResponse.getData();

        assertNotNull(loginResponseApiResponse, "Response should not be null");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
        employeeToken = loginResponse.getToken();

        assertNotNull(employeeToken);
        logger.info("employee token :{}", employeeToken);

    }

    @Test
    @Order(2)
    void step2_CreateClaimTest() {
        CreateClaimRequest request = new CreateClaimRequest();
        request.setTitle("Business Trip");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + employeeToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateClaimRequest> entity = new HttpEntity<>(request, headers);


        ResponseEntity<ApiResponse<ClaimResponse>> response = restTemplate.exchange(
                "/api/claims",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<ClaimResponse>>() {}
        );

        logger.info("Response Status: {}", response.getStatusCode());
        logger.info("Response Body: {}", response.getBody());

        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Status should be 201 Created");


        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Claim created successfully", response.getBody().getMessage());

        ClaimResponse claimResponse = response.getBody().getData();
        assertNotNull(claimResponse, "Response body should not be null");
        assertNotNull(claimResponse.getClaimId(), "Claim ID should not be null");
        assertNotNull(claimResponse.getClaimNumber(), "Claim number should not be null");
        assertEquals("Business Trip", claimResponse.getTitle());
        assertEquals(ClaimStatus.DRAFT, claimResponse.getStatus());
        assertEquals(BigDecimal.ZERO, claimResponse.getTotalAmount());
        assertEquals("Prajwal", claimResponse.getEmployeeName());
        assertEquals("Venkat", claimResponse.getManagerName());

        claimId = claimResponse.getClaimId();

        logger.info("   Claim ID: {}", claimId);
        logger.info("   Claim Number: {}", claimResponse.getClaimNumber());
        logger.info("   Status: {}", claimResponse.getStatus());
        logger.info("   Manager: {}", claimResponse.getManagerName());
    }

    @Test
    @Order(3)
    void step3_AddExpenseItemsTest() {
        AddExpenseItemRequest item1 = new AddExpenseItemRequest();
        item1.setCategory(Category.TRAVEL);
        item1.setDescription("Flight tickets Bangalore to Mumbai");
        item1.setAmount(new BigDecimal("1000"));
        item1.setExpenseDate(LocalDate.of(2026, 1, 15));

        AddExpenseItemRequest item2 = new AddExpenseItemRequest();
        item2.setCategory(Category.ACCOMMODATION);
        item2.setDescription("Hotel stay");
        item2.setAmount(new BigDecimal("1200"));
        item2.setExpenseDate(LocalDate.of(2026, 1, 16));

        AddMultipleItemRequest bulkRequest = new AddMultipleItemRequest();
        bulkRequest.setItems(Arrays.asList(item1, item2));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + employeeToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AddMultipleItemRequest> entity = new HttpEntity<>(bulkRequest, headers);


        ResponseEntity<ApiResponse<List<ExpenseItemResponse>>> response = restTemplate.exchange(
                "/api/claims/" + claimId + "/items/multipleItems",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<List<ExpenseItemResponse>>>() {}
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());


        List<ExpenseItemResponse> items = response.getBody().getData();
        assertEquals(2, items.size());

        logger.info("   Total items: {}", items.size());
        logger.info("   Item 1: {} - {}", item1.getDescription(), item1.getAmount());
        logger.info("   Item 2: {} - {}", item2.getDescription(), item2.getAmount());

        List<ExpenseItem> dbItems = expenseItemRepository.findByClaimClaimId(claimId);
        assertEquals(2, dbItems.size());

        ExpenseClaim claim = expenseClaimRepository.findById(claimId).get();
        assertEquals(new BigDecimal("2200.00"), claim.getTotalAmount());
    }


    @Test
    @Order(4)
    void step4_EmployeeSubmitsClaim() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + employeeToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);


        ResponseEntity<ApiResponse<ClaimResponse>> response = restTemplate.exchange(
                "/api/claims/" + claimId + "/submit",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<ClaimResponse>>() {
                }
        );


        assertEquals(HttpStatus.OK, response.getStatusCode());

        ClaimResponse claimResponse = response.getBody().getData();
        assertNotNull(claimResponse);
        assertEquals(ClaimStatus.SUBMITTED, claimResponse.getStatus());
        assertNotNull(claimResponse.getSubmittedAt());

        logger.info("Status: {}", claimResponse.getStatus());
        logger.info("Submitted At: {}", claimResponse.getSubmittedAt());

        ExpenseClaim claim = expenseClaimRepository.findById(claimId).get();
        assertEquals(ClaimStatus.SUBMITTED, claim.getStatus());
        assertNotNull(claim.getSubmittedAt());
    }

    @Test
    @Order(5)
    void step5_ManagerLoginTest(){


        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("venkat.manager@claimx.com");
        loginRequest.setPassword("venkat@123");

        ResponseEntity<ApiResponse<LoginResponse>> responseEntity = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ApiResponse<LoginResponse>>() {}
        );

        logger.info("Response Status:{} ", responseEntity.getStatusCode());
        logger.info("Response body: {}", responseEntity.getBody());

        ApiResponse<LoginResponse> loginResponseApiResponse = responseEntity.getBody();

        LoginResponse loginResponse = loginResponseApiResponse.getData();

        assertNotNull(loginResponseApiResponse, "Response should not be null");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
        managerToken = loginResponse.getToken();

        assertNotNull(managerToken);
        logger.info("manager token :{}", managerToken);


    }

    @Test
    @Order(6)
    void step6_ManagerApproveClaim(){

        ApproveClaimRequest approveClaimRequest = new ApproveClaimRequest();
        approveClaimRequest.setComment("the claim is approve and is sent to finance department");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + managerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ApproveClaimRequest> entity = new HttpEntity<>(approveClaimRequest, headers);

        ResponseEntity<ApiResponse<ClaimResponse>> response = restTemplate.exchange(
                "/api/manager/claims/pending/" + claimId + "/approve",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<ClaimResponse>>() {}
        );

        logger.info("Response Status: {}", response.getStatusCode());
        logger.info("Response Body: {}", response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be 200 OK");
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        ClaimResponse claimResponse = response.getBody().getData();
        assertEquals(ClaimStatus.APPROVED, claimResponse.getStatus());
        assertEquals("the claim is approve and is sent to finance department", claimResponse.getReviewComment());

        logger.info("Claim ID: {}", claimResponse.getClaimId());
        logger.info("Status:{}", claimResponse.getStatus());
        logger.info("Manager: {}", claimResponse.getManagerName());
        logger.info("review comment: {}", claimResponse.getReviewComment());

    }

    @Test
    @Order(7)
    void step7_FinanceLoginTest(){


        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("akash.finance@claimx.com");
        loginRequest.setPassword("akash@123");

        ResponseEntity<ApiResponse<LoginResponse>> responseEntity = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ApiResponse<LoginResponse>>() {}
        );

        logger.info("Response Status:{} ", responseEntity.getStatusCode());
        logger.info("Response body: {}", responseEntity.getBody());

        ApiResponse<LoginResponse> loginResponseApiResponse = responseEntity.getBody();

        LoginResponse loginResponse = loginResponseApiResponse.getData();

        assertNotNull(loginResponseApiResponse, "Response should not be null");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
        financeToken = loginResponse.getToken();

        assertNotNull(financeToken);
        logger.info("finance token :{}", financeToken);

    }


    @Test
    @Order(8)
    void step8_FinanceViewsApprovedClaims() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + financeToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ApiResponse<List<ClaimResponse>>> response = restTemplate.exchange(
                "/api/finance/claims/approved",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<List<ClaimResponse>>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<ClaimResponse> claims = response.getBody().getData();
        assertNotNull(claims);

        ClaimResponse ourClaim = claims.stream()
                .filter(c -> c.getClaimId().equals(claimId))
                .findFirst()
                .orElse(null);

        assertNotNull(ourClaim, "Our claim should be in approved list");
        assertEquals(ClaimStatus.APPROVED, ourClaim.getStatus());
        assertEquals(new BigDecimal("2200.00"), ourClaim.getTotalAmount());

        logger.info("Total approved: {}", claims.size());
        logger.info("Approved claim amount: {}", ourClaim.getTotalAmount());
    }

    @Test
    @Order(9)
    void step9_ProcessPaymentToClaim(){

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + financeToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);



        ResponseEntity<ApiResponse<ClaimResponse>> response = restTemplate.exchange(
                "/api/finance/claims/" + claimId + "/paid",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<ClaimResponse>>() {
                }
        );

        logger.info("Response Status: {}", response.getStatusCode());
        logger.info("Response Body: {}", response.getBody());


        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be 200 OK");

        ClaimResponse claimResponse = response.getBody().getData();
        assertEquals(ClaimStatus.PAID, claimResponse.getStatus());

        claimId = claimResponse.getClaimId();


        logger.info("Claim ID: {}", claimId);
        logger.info("Status:{}",claimResponse.getStatus());

    }

    @Test
    @Order(10)
    void step10_AdminLoginTest(){


        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@claimx.com");
        loginRequest.setPassword("admin@123");

        ResponseEntity<ApiResponse<LoginResponse>> responseEntity = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ApiResponse<LoginResponse>>() {}
        );

        logger.info("Response Status:{} ", responseEntity.getStatusCode());
        logger.info("Response body: {}", responseEntity.getBody());

        ApiResponse<LoginResponse> loginResponseApiResponse = responseEntity.getBody();

        LoginResponse loginResponse = loginResponseApiResponse.getData();

        assertNotNull(loginResponseApiResponse, "Response should not be null");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
        adminToken = loginResponse.getToken();

        assertNotNull(adminToken);
        logger.info("employee token :{}", adminToken);

    }

    @Test
    @Order(11)
    void step11_adminViewsAllTheAuditLogs(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ApiResponse<List<AuditLogResponse>>> response = restTemplate.exchange(
                "/api/admin/audit-log",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<List<AuditLogResponse>>>() {
                }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());


        List<AuditLogResponse> allLogs = response.getBody().getData();

        List<AuditLogResponse> ourClaimLogs = allLogs.stream()
                .filter(log -> log.getClaimId() != null && log.getClaimId().equals(claimId))
                .toList();


        for (AuditLogResponse log : ourClaimLogs) {
            logger.info("    {} | {} → {} | {}",
                    log.getAction(),
                    log.getOldStatus(),
                    log.getNewStatus(),
                    log.getPerformedBy());
        }
    }

    @Test
    @Order(12)
    void step12_EmployeeViewAllClaims(){

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + employeeToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ApiResponse<List<ClaimResponse>>> response = restTemplate.exchange(
                "/api/claims/my",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<List<ClaimResponse>>>() {
                }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<ClaimResponse> allClaims = response.getBody().getData();

        ClaimResponse ourClaim = allClaims.stream()
                .filter(c -> c.getClaimId().equals(claimId))
                .findFirst()
                .orElse(null);

        assertNotNull(ourClaim, "Our claim should be in approved list");
        assertEquals(new BigDecimal("2200.00"), ourClaim.getTotalAmount());

        logger.info("Total claim: {}", allClaims.size());

    }

    @Test
    @Order(13)
    void step13_FinanceGetAllPaidClaims() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + financeToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ApiResponse<List<ClaimResponse>>> response = restTemplate.exchange(
                "/api/finance/claims/paid",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<List<ClaimResponse>>>() {
                }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<ClaimResponse> allClaims = response.getBody().getData();

        ClaimResponse ourClaim = allClaims.stream()
                .filter(c -> c.getClaimId().equals(claimId))
                .findFirst()
                .orElse(null);

        assertNotNull(ourClaim, "Our claim should be in paid list");
        assertEquals(ClaimStatus.PAID, ourClaim.getStatus());

        logger.info("Total approved: {}", allClaims.size());
        logger.info("Approved claim amount: {}", ourClaim.getTotalAmount());
    }

    @Test
    @Order(14)
    void step14_AdminViewAllClaims() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ApiResponse<List<ClaimResponse>>> response = restTemplate.exchange(
                "/api/admin/claims",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<List<ClaimResponse>>>() {
                }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<ClaimResponse> allClaims = response.getBody().getData();

        ClaimResponse ourClaim = allClaims.stream()
                .filter(c -> c.getClaimId().equals(claimId))
                .findFirst()
                .orElse(null);

        assertNotNull(ourClaim);
        assertEquals(new BigDecimal("2200.00"), ourClaim.getTotalAmount());

        logger.info("Total claim: {}", allClaims.size());

    }









}
