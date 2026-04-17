package com.company.claimx.integration;

import com.company.claimx.context.AuthenticationContext;
import com.company.claimx.dto.request.LoginRequest;
import com.company.claimx.dto.response.ApiResponse;
import com.company.claimx.dto.response.LoginResponse;
import com.company.claimx.repository.ExpenseClaimRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthenticationTests {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationTests.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ExpenseClaimRepository expenseClaimRepository;

    @Test
    @Order(1)
    void testLogin_Success(){

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@claimx.com");
        loginRequest.setPassword("admin@123");



        ResponseEntity<ApiResponse<LoginResponse>> responseEntity = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ApiResponse<LoginResponse>>(){}
        );


        logger.info("Response Status:{} " ,responseEntity.getStatusCode());
        logger.info("Response body: {}", responseEntity.getBody());


        assertNotNull(responseEntity, "Response should not be null");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode(), "Status should be 200 OK");

        LoginResponse loginResponse = responseEntity.getBody().getData();
        assertNotNull(loginResponse.getToken(), "Token should not be null");
        assertFalse(loginResponse.getToken().isEmpty(), "Token should not be empty");
        assertEquals("ADMIN", loginResponse.getRole().toString(), "Role should be EMPLOYEE");

        logger.info("Token received: {}", loginResponse.getToken().toString());
    }





    @Test
    @Order(2)
    void testLoginInvalidPassword(){

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@claimx.com");
        loginRequest.setPassword("wrongPassword");

        ResponseEntity<ApiResponse<LoginResponse>> responseEntity = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ApiResponse<LoginResponse>>(){}
        );

        logger.info("Response Status:{} " ,responseEntity.getStatusCode());
        logger.info("Response body: {}", responseEntity.getBody());

        assertNotNull(responseEntity, "Response should not be null");
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode(), "Status should be 403 FORBIDDEN");



    }

    @Test
    @Order(3)
    void testLoginInvalidUsername(){

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("invalidUserName@claimx.com");
        loginRequest.setPassword("admin@123");



        ResponseEntity<ApiResponse<LoginResponse>> responseEntity = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ApiResponse<LoginResponse>>(){}
        );


        logger.info("Response Status:{} " ,responseEntity.getStatusCode());


        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), "Status should be 404 FORBIDDEN");


    }

    @Test
    @Order(4)
    void testEmployeeLogin_success(){

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("prajwal.employee@claimx.com");
        loginRequest.setPassword("prajwal@123");



        ResponseEntity<ApiResponse<LoginResponse>> responseEntity = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ApiResponse<LoginResponse>>(){}
        );


        logger.info("Response Status:{} " ,responseEntity.getStatusCode());
        logger.info("Response body: {}", responseEntity.getBody());


        assertNotNull(responseEntity, "Response should not be null");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        LoginResponse loginResponse = responseEntity.getBody().getData();
        assertNotNull(loginResponse.getToken(), "Token should not be null");
        assertFalse(loginResponse.getToken().isEmpty(), "Token should not be empty");
        assertEquals("EMPLOYEE", loginResponse.getRole().toString());

        logger.info("Token received: {}", loginResponse.getToken().toString());
    }


    @Test
    @Order(5)
    void testManagerLogin_success(){

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("venkat.manager@claimx.com");
        loginRequest.setPassword("venkat@123");



        ResponseEntity<ApiResponse<LoginResponse>> responseEntity = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ApiResponse<LoginResponse>>(){}
        );


        logger.info("Response Status:{} " ,responseEntity.getStatusCode());
        logger.info("Response body: {}", responseEntity.getBody());


        assertNotNull(responseEntity, "Response should not be null");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        LoginResponse loginResponse = responseEntity.getBody().getData();
        assertNotNull(loginResponse.getToken(), "Token should not be null");
        assertFalse(loginResponse.getToken().isEmpty(), "Token should not be empty");
        assertEquals("MANAGER", loginResponse.getRole().toString());

        logger.info("Token received: {}", loginResponse.getToken().toString());
    }

    @Test
    @Order(5)
    void testFinanceLogin_success(){

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("akash.finance@claimx.com");
        loginRequest.setPassword("akash@123");



        ResponseEntity<ApiResponse<LoginResponse>> responseEntity = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ApiResponse<LoginResponse>>(){}
        );


        logger.info("Response Status:{} " ,responseEntity.getStatusCode());
        logger.info("Response body: {}", responseEntity.getBody());


        assertNotNull(responseEntity, "Response should not be null");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        LoginResponse loginResponse = responseEntity.getBody().getData();
        assertNotNull(loginResponse.getToken(), "Token should not be null");
        assertFalse(loginResponse.getToken().isEmpty(), "Token should not be empty");
        assertEquals("FINANCE", loginResponse.getRole().toString());

        logger.info("Token received: {}", loginResponse.getToken().toString());
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "prasid.employee@claimx.com, prasid@123, EMPLOYEE",
                    "venkat.manager@claimx.com, venkat@123, MANAGER",
                    "akash.finance@claimx.com, akash@123, FINANCE",
                    "admin@claimx.com, admin@123, ADMIN"
            }
    )
    void testLogin_AllRoles(String email, String password, String role){
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);



        ResponseEntity<ApiResponse<LoginResponse>> responseEntity = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ApiResponse<LoginResponse>>(){}
        );


        logger.info("Response Status:{} " ,responseEntity.getStatusCode());
        logger.info("Response body: {}", responseEntity.getBody());


        assertNotNull(responseEntity, "Response should not be null");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode(), "Status should be 200 OK");

        LoginResponse loginResponse = responseEntity.getBody().getData();
        assertNotNull(loginResponse.getToken(), "Token should not be null");
        assertFalse(loginResponse.getToken().isEmpty(), "Token should not be empty");
        assertEquals(role, loginResponse.getRole().toString());

        logger.info("Token received: {}", loginResponse.getToken().toString());

    }






}



















