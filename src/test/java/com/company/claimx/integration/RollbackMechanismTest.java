package com.company.claimx.integration;

import com.company.claimx.dto.request.AddExpenseItemRequest;
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
import com.company.claimx.service.ClaimService;
import com.company.claimx.service.ExpenseItemService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RollbackMechanismTest {
    private final Logger logger = LoggerFactory.getLogger(RollbackMechanismTest.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ExpenseItemRepository expenseItemRepository;

    @Autowired
    private ExpenseClaimRepository expenseClaimRepository;

    @Autowired
    private ClaimService claimService;

    @Autowired
    private ExpenseItemService expenseItemService;


    private static String employeeToken;
    private static String managerToken;
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
    @Order(3)
    void testSubmitClaim_RollBackWhen_NoItems(){


        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + employeeToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);


        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/claims/" + claimId + "/submit",
                entity,
                String.class
        );



        ExpenseClaim claim = expenseClaimRepository.findById(claimId).get();


        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ClaimStatus.DRAFT, claim.getStatus(), "Claim should remain DRAFT after failure");

        assertNull(claim.getSubmittedAt());





    }


    @Test
    @Order(4)
    void transactionRollBack_OnDeletingSubmittedClaim(){

        CreateClaimRequest request = new CreateClaimRequest();
        request.setTitle("Test the delete rollback");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + employeeToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateClaimRequest> entity = new HttpEntity<>(request, headers);


        ResponseEntity<ClaimResponse> response = restTemplate.postForEntity(
                "/api/claims",
                entity,
                ClaimResponse.class
        );

        Long claimId = response.getBody().getClaimId();


        AddExpenseItemRequest itemRequest = new AddExpenseItemRequest();
        itemRequest.setCategory(Category.FOOD);
        itemRequest.setDescription("Lunch");
        itemRequest.setAmount(new BigDecimal("500.00"));
        itemRequest.setExpenseDate(LocalDate.now());

        HttpEntity<AddExpenseItemRequest> itemEntity = new HttpEntity<>(itemRequest, headers);
        restTemplate.postForEntity(
                "/api/claims/" + claimId + "/items",
                itemEntity,
                ExpenseItemResponse.class
        );

        HttpEntity<Void> submitEntity = new HttpEntity<>(headers);
        restTemplate.postForEntity(
                "/api/claims/" + claimId + "/submit",
                submitEntity,
                ClaimResponse.class
        );

        logger.info("Claim submitted: {}", claimId);

        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                "/api/claims/delete/" + claimId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class
        );

        logger.info("Delete response status: {}", deleteResponse.getStatusCode());


        assertEquals(HttpStatus.BAD_REQUEST, deleteResponse.getStatusCode());

        ExpenseClaim claim = expenseClaimRepository.findById(claimId).orElse(null);
        assertNotNull(claim);
        assertEquals("SUBMITTED", claim.getStatus().name());

        List<ExpenseItem> items = expenseItemRepository.findByClaimClaimId(claimId);
        assertEquals(1, items.size());

    }


    @Test
    @Order(5)
    void auditLogSurvives_AfterClaimDeletion(){
        CreateClaimRequest createRequest = new CreateClaimRequest();
        createRequest.setTitle("Test Audit log remains after deletion");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + employeeToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateClaimRequest> createEntity = new HttpEntity<>(createRequest, headers);

        ResponseEntity<ClaimResponse> createResponse = restTemplate.postForEntity(
                "/api/claims",
                createEntity,
                ClaimResponse.class
        );

        Long claimId = createResponse.getBody().getClaimId();
        String claimNumber = createResponse.getBody().getClaimNumber();




        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/claims/delete/" + claimId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        logger.info("Claim deleted successfully");


        boolean claimExists = expenseClaimRepository.findById(claimId).isPresent();
        assertFalse(claimExists, "Claim should be deleted");
    }
}
