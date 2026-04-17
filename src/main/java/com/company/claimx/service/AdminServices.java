package com.company.claimx.service;

import com.company.claimx.constants.ErrorMessageConstants;
import com.company.claimx.dto.response.*;
import com.company.claimx.entity.*;
import com.company.claimx.enums.UserRole;
import com.company.claimx.exception.UserNotFoundException;
import com.company.claimx.mapper.ClaimMapper;
import com.company.claimx.repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * service class to define all the business logic of admin services
 * To get all the users, get all the claims, get particular user's details along with the claim, get all the audit logs
 */
@Service
public class AdminServices {


    private static final Logger logger = LoggerFactory.getLogger(AdminServices.class);
    @Autowired
    UserRepository userRepository;

    @Autowired
    ExpenseClaimRepository expenseClaimRepository;

    @Autowired
    AuditLogRepository auditLogRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private ExpenseItemRepository expenseItemRepository;

    @Autowired
    private EmployeeManagerRepository employeeManagerRepository;

    @Autowired
    private ClaimMapper claimMapper;


    /**
     * to get all the users for the admin
     * @param userEmail - admin email
     * @return - list of all the users
     * @throws UserNotFoundException - if the admin user is not found
     */
    @Transactional
    public List<UserResponse> getAllUsers(String userEmail){

        logger.info("Retrieving all the users.");
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND));
        List<User> users = userRepository.findAll();
        

        return claimMapper.userResponsesList(users);
    }

    /**
     * to map the user details
     * @param user - user for the userResponse.
     * @return - details of the user
     */


    /**
     * to get all the claim of all the users
     * @param userEmail - admin user email
     * @return - list of all the claims
     * @throws UserNotFoundException
     */
    @Transactional
    public List<ClaimResponse> getAllClaims(String userEmail){

        logger.info("Retrieve all the claims");

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND));


        List<ExpenseClaim> claims = expenseClaimRepository.findAll();


        return claimMapper.toClaimResponseList(claims);
    }


    /**
     * to get all the audit logs
     * @param userEmail - to authorize the admin user
     * @return - the list of all the audit logs
     */
    @Transactional
    public List<AuditLogResponse> getAllAuditLogs(String userEmail) {

        logger.info("Retrieving all the audit logs.");

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.ADMIN_NOT_FOUND));


        List<AuditLog> logs = auditService.getAllLogs();



        return claimMapper.auditLogResponseList(logs);
    }




    /**
     * to get all the user details with all his claims
     * @param userId - get user id inorder to retrieve the user details and the claims
     * @return - user response with the claims
     * @throws UserNotFoundException - if the user is not found
     */
    @Transactional
    public UserWithClaimResponse getUserWithClaims(Long userId){

        logger.info("Retrieving the claims and the user fro id:{}",userId);
        User user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND_WITH_ID +userId));


        List<ExpenseClaim> claims = expenseClaimRepository.findByEmployee(user);



        List<ClaimResponse> claimResponseList = claimMapper.toClaimResponseList(claims);


        return UserWithClaimResponse.builder()
                .userId(user.getId())
                .employeeCode(user.getEmployeeCode())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .claims(claimResponseList)
                .build();
    }
}
