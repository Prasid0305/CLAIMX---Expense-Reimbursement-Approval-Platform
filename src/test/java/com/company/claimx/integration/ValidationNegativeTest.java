package com.company.claimx.integration;

import com.company.claimx.dto.request.AddExpenseItemRequest;
import com.company.claimx.dto.request.AddMultipleItemRequest;
import com.company.claimx.dto.request.CreateClaimRequest;
import com.company.claimx.dto.request.LoginRequest;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.dto.response.ExpenseItemResponse;
import com.company.claimx.dto.response.LoginResponse;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.entity.ExpenseItem;
import com.company.claimx.enums.Category;
import com.company.claimx.enums.ClaimStatus;
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
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ValidationNegativeTest {
    private static final Logger logger = LoggerFactory.getLogger(ValidationNegativeTest.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ExpenseItemRepository expenseItemRepository;

    @Autowired
    private ExpenseClaimRepository expenseClaimRepository;

    private static String employeeToken;
    private static Long validClaimId;

    private static Long claimId;


    @Test
    @Order(1)
    void step1_LoginAsEmployee() {

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("prajwal.employee@claimx.com");
        loginRequest.setPassword("prajwal@123");


        ResponseEntity<LoginResponse> responseEntity = restTemplate.postForEntity(
                "/api/auth/login",
                loginRequest,
                LoginResponse.class
        );


        logger.info("Response Status:{} ", responseEntity.getStatusCode());
        logger.info("Response body: {}", responseEntity.getBody());


        assertNotNull(responseEntity, "Response should not be null");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        employeeToken = responseEntity.getBody().getToken();




        assertNotNull(employeeToken);
        logger.info("employee token :{}" ,employeeToken);


        assertNotNull(responseEntity.getBody(), "Login response body is null");
        assertNotNull(responseEntity.getBody().getToken(), "Token is null");
    }


    @Test
    @Order(2)
    void test_CreateClaim_EmptyTitle_ShouldFail(){

        CreateClaimRequest request = new CreateClaimRequest();
        request.setTitle("");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + employeeToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateClaimRequest> entity = new HttpEntity<>(request, headers);


        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/claims",
                entity,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        logger.info("title empty string - rejection  tested successfully");

    }

    @Test
    @Order(3)
    void test_CreateClaim_NullTitle_ShouldFail(){

        CreateClaimRequest request = new CreateClaimRequest();
        request.setTitle(null);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + employeeToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateClaimRequest> entity = new HttpEntity<>(request, headers);


        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/claims",
                entity,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        logger.info("title null - rejection tested successfully");

    }

    @Test
    @Order(4)
    void step2_CreateClaimTets(){



        CreateClaimRequest request = new CreateClaimRequest();
        request.setTitle("Business Trip");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + employeeToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateClaimRequest> entity = new HttpEntity<>(request, headers);


        ResponseEntity<ClaimResponse> response = restTemplate.postForEntity(
                "/api/claims",
                entity,
                ClaimResponse.class
        );

        logger.info("Response Status: {}", response.getStatusCode());
        logger.info("Response Body: {}", response.getBody());


        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Status should be 201 Created");

        ClaimResponse claimResponse = response.getBody();
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
    @Order(4)
    void test_AddItem_NullCategory_ShouldFail(){

        AddExpenseItemRequest request = new AddExpenseItemRequest();
        request.setCategory(null);
        request.setDescription("Valid description");
        request.setAmount(new BigDecimal("1000.00"));
        request.setExpenseDate(LocalDate.now());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + employeeToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AddExpenseItemRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/claims/" + validClaimId + "/items/MultipleItems",
                entity,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Should reject null category");
        logger.info("null category correctly rejected" );


    }

    @Test
    @Order(5)
    void test_AddItem_EmptyCategory_ShouldFail(){

        AddExpenseItemRequest request = new AddExpenseItemRequest();
        request.setCategory(Category.TRAVEL);
        request.setDescription("");
        request.setAmount(new BigDecimal("1000.00"));
        request.setExpenseDate(LocalDate.now());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + employeeToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AddExpenseItemRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/claims/" + validClaimId + "/items/multipleItems",
                entity,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Should reject empty");
        logger.info("empty description  correctly rejected" );


    }
    @Test
    @Order(6)
    void test8_AddItem_ZeroAmount_ShouldFail() {
        AddExpenseItemRequest request = new AddExpenseItemRequest();
        request.setCategory(Category.ACCOMMODATION);
        request.setDescription("Valid description");
        request.setAmount(BigDecimal.ZERO);  // Zero amount
        request.setExpenseDate(LocalDate.now());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + employeeToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AddExpenseItemRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/claims/" + validClaimId + "/items/multipleItems",
                entity,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Should reject zero amount");

        logger.info("Zero amount correctly rejected");
    }

    @Test
    @Order(5)
    void test_AddItem_WithFutureDate_ShouldFail(){

        AddExpenseItemRequest request = new AddExpenseItemRequest();
        request.setCategory(Category.TRAVEL);
        request.setDescription("valid description");
        request.setAmount(new BigDecimal("1000.00"));
        request.setExpenseDate(LocalDate.now().plusDays(10));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + employeeToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AddExpenseItemRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/claims/" + validClaimId + "/items/multipleItems",
                entity,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Should reject future date");
        logger.info("future date correctly rejected" );


    }

}
