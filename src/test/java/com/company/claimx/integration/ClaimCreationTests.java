package com.company.claimx.integration;


import com.company.claimx.dto.request.CreateClaimRequest;
import com.company.claimx.dto.request.LoginRequest;
import com.company.claimx.dto.response.ApiResponse;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.dto.response.LoginResponse;
import com.company.claimx.enums.ClaimStatus;
import com.company.claimx.repository.ExpenseClaimRepository;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClaimCreationTests {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationTests.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ExpenseClaimRepository expenseClaimRepository;

    private static String employeeToken;
    private static Long createdClaimId;

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
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        LoginResponse loginResponse = responseEntity.getBody().getData();
        assertNotNull(loginResponse.getToken(), "Token should not be null");
        assertFalse(loginResponse.getToken().isEmpty(), "Token should not be empty");
        Assertions.assertEquals("EMPLOYEE", loginResponse.getRole().toString());

        logger.info("Token received: {}", loginResponse.getToken().toString());

        employeeToken = responseEntity.getBody().getData().getToken();
    }

    @Test
    @Order(2)
    void step2_CreateClaimWithToken() {

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


        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Status should be 201 Created");

        ClaimResponse claimResponse = response.getBody().getData();
        assertNotNull(claimResponse, "Response body should not be null");
        assertNotNull(claimResponse.getClaimId(), "Claim ID should not be null");
        assertNotNull(claimResponse.getClaimNumber(), "Claim number should not be null");
        Assertions.assertEquals("Business Trip", claimResponse.getTitle());
        Assertions.assertEquals(ClaimStatus.DRAFT, claimResponse.getStatus());
        Assertions.assertEquals(BigDecimal.ZERO, claimResponse.getTotalAmount());
        Assertions.assertEquals("Prajwal", claimResponse.getEmployeeName());
        Assertions.assertEquals("Venkat", claimResponse.getManagerName());

        createdClaimId = claimResponse.getClaimId();

        logger.info("ID: {}, Number: {}", createdClaimId, claimResponse.getClaimNumber());
    }

    @Test
    @Order(3)
    void step3_GetClaimById() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + employeeToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        logger.info("Sending GET /api/claims/{}", createdClaimId);

        ResponseEntity<ApiResponse<ClaimResponse>> response = restTemplate.exchange(
                "/api/claims/" + createdClaimId,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<ClaimResponse>>() {
                }
        );

        logger.info("Response Status: {}", response.getStatusCode());


        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        ClaimResponse claimResponse = response.getBody().getData();
        assertNotNull(claimResponse);
        Assertions.assertEquals(createdClaimId, claimResponse.getClaimId());
        Assertions.assertEquals("Business Trip", claimResponse.getTitle());
        Assertions.assertEquals("Venkat", claimResponse.getManagerName()); // Dynamic!

    }

}
