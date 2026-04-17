package com.company.claimx.service;

import com.company.claimx.constants.ErrorMessageConstants;
import com.company.claimx.dto.request.AddExpenseItemRequest;
import com.company.claimx.dto.request.AddMultipleItemRequest;
import com.company.claimx.dto.request.UpdateExpenseItemRequest;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.dto.response.ExpenseItemResponse;
import com.company.claimx.entity.EmployeeManager;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.entity.ExpenseItem;
import com.company.claimx.entity.User;
import com.company.claimx.enums.ClaimStatus;
import com.company.claimx.enums.UserRole;
import com.company.claimx.exception.*;
import com.company.claimx.repository.EmployeeManagerRepository;
import com.company.claimx.repository.ExpenseClaimRepository;
import com.company.claimx.repository.ExpenseItemRepository;
import com.company.claimx.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * service class for handling the business logic
 * this class handles the logic for adding the items to the claim, retrieve the items by claim, updating the claim total amount, deleting item, updating item, adding multiple item.
 */
@Service
public class ExpenseItemService {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseItemService.class);
    @Autowired
    private ExpenseItemRepository expenseItemRepository;

    @Autowired
    private ExpenseClaimRepository expenseClaimRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeManagerRepository employeeManagerRepository;



    /**
     * adding the item details - "category", "description", "string","amount", "expenseDate"
     * @param claimId - to access the claim for which the items are added.
     * @param addExpenseItemRequest - to get the body of the item
     * @param userEmail - to validate the authorization
     * @return - ExpenseItemResponse for the item which contains the details (claim id, item id, category, title, description, amount)
     * @throws ClaimNotFoundException - if there is no claim with the given Id.
     * @throws UserNotFoundException - if there is no user.
     */
    @Transactional
    public ExpenseItemResponse addItem(Long claimId, AddExpenseItemRequest addExpenseItemRequest, String userEmail){

        logger.info("Adding items to the claim with id:{}",claimId);


        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException(ErrorMessageConstants.CLAIM_NOT_FOUND_WITH_ID +claimId));


        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND_WITH_EMAIL));


        if(!claim.getEmployee().getId().equals(user.getId())){
            throw new ItemDoesNotBelongToClaim(ErrorMessageConstants.UNAUTHORIZED_EXPENSE_ITEM_ADDITION);
        }


        if(claim.getStatus()!= ClaimStatus.DRAFT){
            throw new InvalidClaimStatus(ErrorMessageConstants.INVALID_CLAIM_STATUS_FOR_ITEM_ADDITION +claim.getStatus());
        }


        ExpenseItem item = new ExpenseItem();
        item.setClaim(claim);
        item.setCategory(addExpenseItemRequest.getCategory());
        item.setDescription(addExpenseItemRequest.getDescription());
        item.setAmount(addExpenseItemRequest.getAmount());
        item.setExpenseDate(addExpenseItemRequest.getExpenseDate());


        ExpenseItem saveItem = expenseItemRepository.save(item);


        updateClaimTotal(claim);


        logger.info("Items added to the claim with id:{}",claimId);

        return mapToResponse(saveItem);
    }

    /**
     * to return the item details.
     * @param saveItem - all the item details
     * @return expense item which contains details such as itemId, claimId, category, description, amount, expense date
     */
    private ExpenseItemResponse mapToResponse(ExpenseItem saveItem) {
        return ExpenseItemResponse.builder()
                .itemId(saveItem.getItemId())
                .claimId(saveItem.getClaim().getClaimId())
                .category(saveItem.getCategory())
                .description(saveItem.getDescription())
                .amount(saveItem.getAmount())
                .expenseDate(saveItem.getExpenseDate())
                .build();
    }

    /**
     * update the total of the claim by adding the amounts of the each item's amount
     * @param claim - expense claim
     */
    private void updateClaimTotal(ExpenseClaim claim) {

        logger.info("Updating the claim total amount");
        BigDecimal totalAmount = expenseItemRepository.calculateTotalByClaimId(claim.getClaimId());
        claim.setTotalAmount(totalAmount);
        expenseClaimRepository.save(claim);
    }

    /**
     * to get the list of items belonging to the particular claim
     * @param claimId - to access the claim for which the items need to be retrieved
     * @param userEmail - to check the authorization
     * @return list of items for the claim
     * @throws ClaimNotFoundException
     * @throws UserNotFoundException
     *
     */
    @Transactional
    public List<ExpenseItemResponse> getItemByClaim(Long claimId, String userEmail){

        logger.info("Retrieving claim's items");

        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException(ErrorMessageConstants.CLAIM_NOT_FOUND_WITH_ID +claimId));


        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND_WITH_EMAIL));

        validateClaimAccess(claim, user);

        List<ExpenseItem> items = expenseItemRepository.findByClaimClaimId(claimId);
        return items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * to validate the claim access for a user
     * @param claim - details of the claim
     * @param user - to check the authorization
     * @throws UnauthorizedAccessException - if the user is not authorized to access the claim
     */
    private void validateClaimAccess(ExpenseClaim claim, User user) {


        logger.info("Validating claim access");
        if(claim.getEmployee().getId().equals(user.getId())){
            return;
        }


        EmployeeManager empMgr = employeeManagerRepository.findByEmployee(claim.getEmployee())
                .orElse(null);
        if (empMgr != null && empMgr.getManager().getId().equals(user.getId())){
            return;
        }
        if(user.getRole()== UserRole.ADMIN || user.getRole() == UserRole.FINANCE){
            return;
        }
        throw new UnauthorizedAccessException(ErrorMessageConstants.UNAUTHORIZED_ACCESS);

    }

    /**
     * this method is to delete the item in the claim
     * @param claimId - to access the claim which the Id.
     * @param itemId - to delete the particular item
     * @param userEmail - to check the authorization
     * @throws ItemDoesNotBelongToClaim - if the item does not belong to this claim.
     * @throws ItemNotFound - if the item is not found with the id
     * @throws UserNotFoundException - if user not found
     * @throws UnauthorizedAccessException - only your own claim's items can be deleted
     * @throws InvalidClaimStatus -  the status must be draft inorder to delete the items
     */
    @Transactional
    public void deleteItem(Long claimId, Long itemId, String userEmail){

        logger.info("Delete the item with id:{} belonging to the claim {}",itemId,claimId);

        ExpenseItem expenseItem = expenseItemRepository.findById(itemId)
                .orElseThrow(()-> new  ItemNotFound(ErrorMessageConstants.ITEM_NOT_FOUND_WITH_ID + itemId));

        if(!expenseItem.getClaim().getClaimId().equals(claimId)){
            throw new ItemDoesNotBelongToClaim(ErrorMessageConstants.ITEM_DOES_NOT_BELONG_TO_CLAIM + claimId);
        }

        ExpenseClaim expenseClaim = expenseItem.getClaim();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND_WITH_EMAIL + userEmail));

        if(!expenseClaim.getEmployee().getId().equals(user.getId())){
            throw new UnauthorizedAccessException(ErrorMessageConstants.UNAUTHORIZED_ITEM_DELETION);
        }

        if(expenseClaim.getStatus()!= ClaimStatus.DRAFT){
            throw new InvalidClaimStatus(ErrorMessageConstants.INVALID_CLAIM_STATUS_FOR_ITEM_DELETION +expenseClaim.getStatus());
        }

        expenseItemRepository.delete(expenseItem);

        logger.info("Item deleted successfully");
        updateClaimTotal(expenseClaim);
    }

    /**
     * update the details of the expense item
     * can only be updated when the claim is inn DRAFT status
     * new total amount is calclulated
     * this can only be done if the claim status is DRAFT
     * @param claimId - to get the claim's item
     * @param itemId - to get the particular claim
     * @param request - the updated expense item details of the item
     * @param userEmail - to check the authorization
     * @return - updated item details
     * @throws ClaimNotFoundException
     * @throws RuntimeException
     * @throws UserNotFoundException
     * @throws UnauthorizedAccessException
     * @throws InvalidClaimStatus
     */
    @Transactional
    public ExpenseItemResponse updateExpenseItem(Long claimId, Long itemId, UpdateExpenseItemRequest request, String userEmail){

        logger.info("Updating the item with id:{}",itemId);


        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException(ErrorMessageConstants.CLAIM_NOT_FOUND_WITH_ID +claimId));


        ExpenseItem item = expenseItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException(ErrorMessageConstants.ITEM_NOT_FOUND_WITH_ID + itemId));



        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND_WITH_EMAIL));


        if(!claim.getEmployee().getId().equals(user.getId())){
            throw new UnauthorizedAccessException(ErrorMessageConstants.UNAUTHORIZED_ITEM_UPDATE);
        }


        if(claim.getStatus()!= ClaimStatus.DRAFT){
            throw new InvalidClaimStatus(ErrorMessageConstants.INVALID_CLAIM_STATUS_FOR_ITEM_UPDATE +claim.getStatus());
        }



        item.setClaim(claim);
        item.setCategory(request.getCategory());
        item.setDescription(request.getDescription());
        item.setAmount(request.getAmount());
        item.setExpenseDate(request.getExpenseDate());


        ExpenseItem updateItem = expenseItemRepository.save(item);


        updateClaimTotal(claim);

        logger.info("Updated the item {}, in the claim {}",itemId,claimId);

        return mapToResponse(updateItem);
    }

    /**
     * adding multiple items at once with list
     * items can only be added when the claim status is in DRAFT
     * @param claimId - to access the claim with id
     * @param request - adding multiple items request
     * @param userEmail - to check the authorization
     * @return - list of items that is added to a claim
     * @throws ClaimNotFoundException
     * @throws UserNotFoundException
     * @throws UnauthorizedAccessException
     * @throws InvalidClaimStatus
     */
    public List<ExpenseItemResponse> addMultipleItems(Long claimId, AddMultipleItemRequest request, String userEmail){

        logger.info("Adding items to the claim id:{}",claimId);

        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException(ErrorMessageConstants.CLAIM_NOT_FOUND_WITH_ID +claimId));



        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException(ErrorMessageConstants.USER_NOT_FOUND));


        if(!claim.getEmployee().getId().equals(user.getId())){
            throw new UnauthorizedAccessException(ErrorMessageConstants.UNAUTHORIZED_EXPENSE_ITEM_ADDITION);
        }


        if(claim.getStatus()!= ClaimStatus.DRAFT){
            throw new InvalidClaimStatus(ErrorMessageConstants.INVALID_CLAIM_STATUS_FOR_ITEM_ADDITION +claim.getStatus());
        }

        List<ExpenseItem> savedItems = new ArrayList<>();

        for(AddExpenseItemRequest itemRequest : request.getItems()){
            ExpenseItem expenseItem = new ExpenseItem();
            expenseItem.setClaim(claim);
            expenseItem.setCategory(itemRequest.getCategory());
            expenseItem.setDescription(itemRequest.getDescription());
            expenseItem.setAmount(itemRequest.getAmount());
            expenseItem.setExpenseDate(itemRequest.getExpenseDate());

            ExpenseItem itemSave = expenseItemRepository.save(expenseItem);

            savedItems.add(itemSave);
        }

        updateClaimTotal(claim);


        logger.info("Items to the claim {} are added successfully",claimId);

        return savedItems.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toUnmodifiableList());

    }


}
