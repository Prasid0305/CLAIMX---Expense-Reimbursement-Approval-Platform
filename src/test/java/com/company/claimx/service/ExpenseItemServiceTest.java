package com.company.claimx.service;

import com.company.claimx.dto.request.AddExpenseItemRequest;
import com.company.claimx.dto.request.AddMultipleItemRequest;
import com.company.claimx.dto.request.UpdateExpenseItemRequest;
import com.company.claimx.dto.response.ExpenseItemResponse;
import com.company.claimx.entity.EmployeeManager;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.entity.ExpenseItem;
import com.company.claimx.entity.User;
import com.company.claimx.enums.Category;
import com.company.claimx.enums.ClaimStatus;
import com.company.claimx.enums.UserRole;
import com.company.claimx.exception.ClaimNotFoundException;
import com.company.claimx.exception.InvalidClaimStatus;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseItemServiceTest {

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

    @InjectMocks
    private ExpenseItemService expenseItemService;

    @Mock
    private AuditService auditService;

    private User employee;
    private User manager;
    private EmployeeManager employeeManager;
    private ExpenseClaim draftClaim;
    private ExpenseClaim submittedClaim;

    private ExpenseItem expenseItem;
    private AddExpenseItemRequest itemRequest;

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
        manager.setEmail("flick.manager@claimx.com");
        manager.setEmployeeCode("MGR001");
        manager.setName("Flick");
        manager.setRole(UserRole.MANAGER);
        manager.setIsActive(true);
        manager.setCreatedAt(LocalDateTime.now());



        employeeManager = new EmployeeManager();
        employeeManager.setEmployee(employee);
        employeeManager.setManager(manager);






        draftClaim = new ExpenseClaim();
        draftClaim.setTitle("Test Claim");
        draftClaim.setClaimId(1L);
        draftClaim.setClaimNumber("CLM-2026-01012");
        draftClaim.setTitle("BusinessTrip");
        draftClaim.setEmployee(employee);

        draftClaim.setTotalAmount(BigDecimal.ZERO);
        draftClaim.setStatus(ClaimStatus.DRAFT);
        draftClaim.setCreatedAt(LocalDateTime.now());

//        submittedClaim = new ExpenseClaim();
//        submittedClaim.setTitle("Test Claim");
//        submittedClaim.setClaimId(50L);
//        submittedClaim.setClaimNumber("CLM-2026-01013");
//        submittedClaim.setTitle("BusinessTrip");
//        submittedClaim.setEmployee(employee);
//
//        submittedClaim.setTotalAmount(BigDecimal.ZERO);
//        submittedClaim.setStatus(ClaimStatus.SUBMITTED);
//        submittedClaim.setCreatedAt(LocalDateTime.now());

        itemRequest = new AddExpenseItemRequest();
        itemRequest.setCategory(Category.TRAVEL);
        itemRequest.setDescription("Flight tickets");
        itemRequest.setAmount(new BigDecimal("10000.00"));
        itemRequest.setExpenseDate(LocalDate.now());

    }

    @Test
    void testAddItem_Success() {

        ExpenseItem savedItem = new ExpenseItem();
        savedItem.setItemId(1L);
        savedItem.setClaim(draftClaim);
        savedItem.setCategory(Category.TRAVEL);
        savedItem.setDescription("Flight ");
        savedItem.setAmount(new BigDecimal("10000.00"));
        savedItem.setExpenseDate(LocalDate.now());

        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(draftClaim));
        when(userRepository.findByEmail("pedri.employee@claimx.com"))
                .thenReturn(Optional.of(employee));
        when(expenseItemRepository.save(any(ExpenseItem.class)))
                .thenReturn(savedItem);
        when(expenseItemRepository.calculateTotalByClaimId(1L))
                .thenReturn(new BigDecimal("10000.00"));


        ExpenseItemResponse response = expenseItemService.addItem(
                1L,
                itemRequest,
                "pedri.employee@claimx.com"
        );


        assertNotNull(response);
        assertEquals(1L, response.getItemId());
        assertEquals(Category.TRAVEL, response.getCategory());
        assertEquals("Flight ", response.getDescription());
        assertEquals(new BigDecimal("10000.00"), response.getAmount());

        verify(expenseItemRepository, times(1)).save(any(ExpenseItem.class));
        verify(expenseClaimRepository, times(1)).save(draftClaim);

        logger.info("Add item test successful");
    }

    @Test
    void testAddItem_ClaimNotFound_ThrowsException() {

        when(expenseClaimRepository.findById(999L))
                .thenReturn(Optional.empty());


        assertThrows(ClaimNotFoundException.class, () -> {
            expenseItemService.addItem(999L, itemRequest, "pedri.employee@claimx.com");
        });

        verify(expenseItemRepository, never()).save(any());

        logger.info("Add item - claim not found test successful");
    }

    @Test
    void testAddItem_NotDraftStatus_ThrowsException() {

        draftClaim.setStatus(ClaimStatus.SUBMITTED);

        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(draftClaim));
        when(userRepository.findByEmail("pedri.employee@claimx.com"))
                .thenReturn(Optional.of(employee));


        InvalidClaimStatus exception = assertThrows(
                InvalidClaimStatus.class,
                () -> expenseItemService.addItem(1L, itemRequest, "pedri.employee@claimx.com")
        );

        assertTrue(exception.getMessage().contains("DRAFT"));
        verify(expenseItemRepository, never()).save(any());

        logger.info("to add items the claim status must be in draft state tested successfully");
    }

    @Test
    void testAddMultipleItems_Success() {

        AddExpenseItemRequest item1 = new AddExpenseItemRequest();
        item1.setCategory(Category.TRAVEL);
        item1.setDescription("Flight");
        item1.setAmount(new BigDecimal("10000.00"));
        item1.setExpenseDate(LocalDate.now());

        AddExpenseItemRequest item2 = new AddExpenseItemRequest();
        item2.setCategory(Category.ACCOMMODATION);
        item2.setDescription("Hotel");
        item2.setAmount(new BigDecimal("15000.00"));
        item2.setExpenseDate(LocalDate.now());

        AddMultipleItemRequest multipleItemRequestRequest = new AddMultipleItemRequest();
        multipleItemRequestRequest.setItems(Arrays.asList(item1, item2));

        ExpenseItem savedItem1 = new ExpenseItem();
        savedItem1.setItemId(1L);
        savedItem1.setClaim(draftClaim);
        savedItem1.setCategory(Category.TRAVEL);
        savedItem1.setAmount(new BigDecimal("10000.00"));

        ExpenseItem savedItem2 = new ExpenseItem();
        savedItem2.setItemId(2L);
        savedItem2.setClaim(draftClaim);
        savedItem2.setCategory(Category.ACCOMMODATION);
        savedItem2.setAmount(new BigDecimal("15000.00"));

        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(draftClaim));
        when(userRepository.findByEmail("pedri.employee@claimx.com"))
                .thenReturn(Optional.of(employee));
        when(expenseItemRepository.save(any(ExpenseItem.class)))
                .thenReturn(savedItem1, savedItem2);
        when(expenseItemRepository.calculateTotalByClaimId(1L))
                .thenReturn(new BigDecimal("25000.00"));


        List<ExpenseItemResponse> responses = expenseItemService.addMultipleItems(
                1L,
                multipleItemRequestRequest,
                "pedri.employee@claimx.com"
        );


        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(Category.TRAVEL, responses.get(0).getCategory());
        assertEquals(Category.ACCOMMODATION, responses.get(1).getCategory());

        verify(expenseItemRepository, times(2)).save(any(ExpenseItem.class));
        verify(expenseClaimRepository, times(1)).save(draftClaim); // Only once at end

        logger.info("Add multiple items test successful ");
    }

    @Test
    void testDeleteItem_Success() {

        ExpenseItem item = new ExpenseItem();
        item.setItemId(1L);
        item.setClaim(draftClaim);
        item.setAmount(new BigDecimal("5000.00"));

        when(expenseItemRepository.findById(1L))
                .thenReturn(Optional.of(item));
        when(userRepository.findByEmail("pedri.employee@claimx.com"))
                .thenReturn(Optional.of(employee));
        when(expenseItemRepository.calculateTotalByClaimId(1L))
                .thenReturn(BigDecimal.ZERO);


        expenseItemService.deleteItem(1L, 1L, "pedri.employee@claimx.com");


        verify(expenseItemRepository, times(1)).delete(item);
        verify(expenseClaimRepository, times(1)).save(draftClaim);

        logger.info("Delete item test successful");
    }

    @Test
    void testUpdateExpenseItem_Success() {

        ExpenseItem existingItem = new ExpenseItem();
        existingItem.setItemId(1L);
        existingItem.setClaim(draftClaim);
        existingItem.setCategory(Category.TRAVEL);
        existingItem.setAmount(new BigDecimal("5000.00"));

        UpdateExpenseItemRequest updateRequest = new UpdateExpenseItemRequest();
        updateRequest.setCategory(Category.FOOD);
        updateRequest.setDescription("Updated description");
        updateRequest.setAmount(new BigDecimal("8000.00"));
        updateRequest.setExpenseDate(LocalDate.now());

        when(expenseClaimRepository.findById(1L))
                .thenReturn(Optional.of(draftClaim));
        when(expenseItemRepository.findById(1L))
                .thenReturn(Optional.of(existingItem));
        when(userRepository.findByEmail("pedri.employee@claimx.com"))
                .thenReturn(Optional.of(employee));
        when(expenseItemRepository.save(any(ExpenseItem.class)))
                .thenReturn(existingItem);
        when(expenseItemRepository.calculateTotalByClaimId(1L))
                .thenReturn(new BigDecimal("8000.00"));


        ExpenseItemResponse response = expenseItemService.updateExpenseItem(
                1L,
                1L,
                updateRequest,
                "pedri.employee@claimx.com"
        );


        assertNotNull(response);
        assertEquals(Category.FOOD, existingItem.getCategory());
        assertEquals(new BigDecimal("8000.00"), existingItem.getAmount());

        verify(expenseItemRepository, times(1)).save(existingItem);
        verify(expenseClaimRepository, times(1)).save(draftClaim);

        logger.info("Update expense item test successful");
    }




}
