package com.company.claimx.integration;

import com.company.claimx.dto.request.*;
import com.company.claimx.dto.response.ApiResponse;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.dto.response.ExpenseItemResponse;
import com.company.claimx.dto.response.LoginResponse;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.entity.ExpenseItem;
import com.company.claimx.enums.Category;
import com.company.claimx.enums.ClaimStatus;
import com.company.claimx.repository.AuditLogRepository;
import com.company.claimx.repository.ExpenseClaimRepository;
import com.company.claimx.repository.ExpenseItemRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClaimRejectionTest {


    private static final Logger logger = LoggerFactory.getLogger(ClaimRejectionTest.class);


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
                new ParameterizedTypeReference<ApiResponse<LoginResponse>>(){}
        );


        logger.info("Response Status:{} ", responseEntity.getStatusCode());
        logger.info("Response body: {}", responseEntity.getBody());


        assertNotNull(responseEntity, "Response should not be null");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        employeeToken = responseEntity.getBody().getData().getToken();




        assertNotNull(employeeToken);
        logger.info("employee token :{}" ,employeeToken);


        assertNotNull(responseEntity.getBody(), "Login response body is null");
        assertNotNull(responseEntity.getBody().getData().getToken(), "Token is null");
    }


    @Test
    @Order(2)
    void step2_CreateClaimTets(){



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
                new ParameterizedTypeReference<ApiResponse<ClaimResponse>>() {
                }
        );

        logger.info("Response Status: {}", response.getStatusCode());
        logger.info("Response Body: {}", response.getBody());


        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Status should be 201 Created");

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
    void step3_AddExpenseItemsTest(){
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
                new ParameterizedTypeReference<ApiResponse<List<ExpenseItemResponse>>>() {
                }
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        List<ExpenseItemResponse> allItems = response.getBody().getData();

        logger.info("   Total items: {}", allItems.size());
        logger.info("   Item 1: {} - {}", item1.getDescription(), item1.getAmount());
        logger.info("   Item 2: {} - {}", item2.getDescription(), item2.getAmount());

        List<ExpenseItem> items = expenseItemRepository.findByClaimClaimId(claimId);
        assertEquals(2, items.size());

        ExpenseClaim itemAmount = expenseClaimRepository.findById(claimId).get();
        assertEquals(new BigDecimal("2200.00"), itemAmount.getTotalAmount());


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
                new ParameterizedTypeReference<ApiResponse<LoginResponse>>(){}
        );


        logger.info("Response Status:{} ", responseEntity.getStatusCode());
        logger.info("Response body: {}", responseEntity.getBody());


        assertNotNull(responseEntity, "Response should not be null");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        managerToken = responseEntity.getBody().getData().getToken();




        assertNotNull(managerToken);
        logger.info("manager token :{}" ,managerToken);


        assertNotNull(responseEntity.getBody(), "Login response body is null");
        assertNotNull(responseEntity.getBody().getData().getToken(), "Token is null");


    }

    @Test
    @Order(6)
    void step8_ManagerViewSubmittedClaims() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + managerToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ApiResponse<List<ClaimResponse>>> response = restTemplate.exchange(
                "/api/manager/claims/pending",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<List<ClaimResponse>>>() {
                }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<ClaimResponse> allCLaims = response.getBody().getData();

        ClaimResponse ourClaim = allCLaims.stream()
                .filter(c -> c.getClaimId().equals(claimId))
                .findFirst()
                .orElse(null);

        assertNotNull(ourClaim, "Our claim should be in submitted list");
        assertEquals(ClaimStatus.SUBMITTED, ourClaim.getStatus());

        logger.info("Total submitted: {}", allCLaims.size());
    }

    @Test
    @Order(7)
    void step8_ManagerViewSubmittedClaimsById() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + managerToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ApiResponse<ClaimResponse>> response = restTemplate.exchange(
                "/api/manager/claims/pending/" + claimId,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<ClaimResponse>>() {
                }
        );

        logger.info("Response Status: {}", response.getStatusCode());
        logger.info("Response Body: {}", response.getBody());

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be 200 OK");

        ClaimResponse claimResponse = response.getBody().getData();
        assertEquals(ClaimStatus.SUBMITTED, claimResponse.getStatus());

        claimId = claimResponse.getClaimId();


        logger.info("Claim ID: {}", claimId);
        logger.info("Status:{}",claimResponse.getStatus());
    }

    @Test
    @Order(8)
    void step6_ManagerRejectsClaim(){
        RejectClaimRequest rejectClaimRequest = new RejectClaimRequest();
        rejectClaimRequest.setComment("the claim is rejected");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + managerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RejectClaimRequest> entity = new HttpEntity<>(rejectClaimRequest, headers);


        ResponseEntity<ApiResponse<ClaimResponse>> response = restTemplate.exchange(
                "/api/manager/claims/pending/" + claimId + "/reject",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<ClaimResponse>>() {
                }
        );

        logger.info("Response Status: {}", response.getStatusCode());
        logger.info("Response Body: {}", response.getBody());


        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be 200 OK");

        ClaimResponse claimResponse = response.getBody().getData();
        assertEquals(ClaimStatus.REJECTED, claimResponse.getStatus());
        assertEquals("the claim is rejected", claimResponse.getReviewComment());

        claimId = claimResponse.getClaimId();


        logger.info("Claim ID: {}", claimId);
        logger.info("Status:{}",claimResponse.getStatus());
        logger.info("Manager: {}", claimResponse.getManagerName());
        logger.info("review comment: {}", claimResponse.getReviewComment());

    }
}
