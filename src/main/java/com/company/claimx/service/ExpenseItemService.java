package com.company.claimx.service;

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
import com.company.claimx.exception.*;
import com.company.claimx.repository.EmployeeManagerRepository;
import com.company.claimx.repository.ExpenseClaimRepository;
import com.company.claimx.repository.ExpenseItemRepository;
import com.company.claimx.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ExpenseItemService {

    @Autowired
    private ExpenseItemRepository expenseItemRepository;

    @Autowired
    private ExpenseClaimRepository expenseClaimRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeManagerRepository employeeManagerRepository;

    @Transactional
    public ExpenseItemResponse addItem(Long claimId, AddExpenseItemRequest addExpenseItemRequest, String userEmail){

        // finding claim
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException("claim not found with id: " +claimId));

        // finding user email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException("user not found "));

        // validate the user's own claim
        if(!claim.getEmployee().getId().equals(user.getId())){
            throw new RuntimeException("the items can only be added to your own claim");
        }

        //status = DRAFT
        if(claim.getStatus()!= ClaimStatus.DRAFT){
            throw new InvalidClaimStatus("the status must be draft inorder to add the items, Current Status: " +claim.getStatus());
        }

        //setting expense item
        ExpenseItem item = new ExpenseItem();
        item.setClaim(claim);
        item.setCategory(addExpenseItemRequest.getCategory());
        item.setDescription(addExpenseItemRequest.getDescription());
        item.setAmount(addExpenseItemRequest.getAmount());
        item.setExpenseDate(addExpenseItemRequest.getExpenseDate());

        //saving
        ExpenseItem saveItem = expenseItemRepository.save(item);

        //update total amt
        updateClaimTotal(claim);

        //return response
        return mapToResponse(saveItem);
    }

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

    private void updateClaimTotal(ExpenseClaim claim) {
        BigDecimal totalAmount = expenseItemRepository.calculateTotalByClaimId(claim.getClaimId());
        claim.setTotalAmount(totalAmount);
        expenseClaimRepository.save(claim);
    }

    @Transactional
    public List<ExpenseItemResponse> getItemByClaim(Long claimId, String userEmail){
        // finding claim
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException("claim not found with id: " +claimId));

        // finding user email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException("user not found "));

        validateClaimAccess(claim, user);

        List<ExpenseItem> items = expenseItemRepository.findByClaimClaimId(claimId);
        return items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void validateClaimAccess(ExpenseClaim claim, User user) {
        // Employee can see their own claims
        if(claim.getEmployee().getId().equals(user.getId())){
            return;
        }

        // Check if user is the current manager
        EmployeeManager empMgr = employeeManagerRepository.findByEmployee(claim.getEmployee())
                .orElse(null);
        if (empMgr != null && empMgr.getManager().getId().equals(user.getId())){
            return;
        }
        if(user.getRole().name().equals("ADMIN") || user.getRole().name().equals("FINANCE")){
            return;
        }
        throw new UnauthorizedAccessException("user not authorized");

    }

    public void deleteItem(Long claimId, Long itemId, String userEmail){

        ExpenseItem expenseItem = expenseItemRepository.findById(itemId)
                .orElseThrow(()-> new  ItemNotFound("item not found with the id:"+itemId));

        if(!expenseItem.getClaim().getClaimId().equals(claimId)){
            throw new ItemDoesNotBelongToClaim("the item does not belong to this claim");
        }

        ExpenseClaim expenseClaim = expenseItem.getClaim();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException("user not found "));

        if(!expenseClaim.getEmployee().getId().equals(user.getId())){
            throw new RuntimeException("only your own claim's items can be deleted");
        }

        if(expenseClaim.getStatus()!= ClaimStatus.DRAFT){
            throw new InvalidClaimStatus("the status must be draft inorder to delete the items, Current Status: " +expenseClaim.getStatus());
        }

        expenseItemRepository.delete(expenseItem);

        updateClaimTotal(expenseClaim);
    }


    @Transactional
    public ExpenseItemResponse updateExpenseItem(Long claimId, Long itemId, UpdateExpenseItemRequest request, String userEmail){

        // finding claim
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException("claim not found with id: " +claimId));

        // finding the item
        ExpenseItem item = expenseItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));


        // finding user email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException("user not found "));

        // validate the user's own claim
        if(!claim.getEmployee().getId().equals(user.getId())){
            throw new RuntimeException("the items can only be added to your own claim");
        }

        //status = DRAFT
        if(claim.getStatus()!= ClaimStatus.DRAFT){
            throw new InvalidClaimStatus("the status must be draft inorder to add the items, Current Status: " +claim.getStatus());
        }



        item.setClaim(claim);
        item.setCategory(request.getCategory());
        item.setDescription(request.getDescription());
        item.setAmount(request.getAmount());
        item.setExpenseDate(request.getExpenseDate());

        //saving
        ExpenseItem updateItem = expenseItemRepository.save(item);

        //update total amt
        updateClaimTotal(claim);

        //return response
        return mapToResponse(updateItem);
    }

    public List<ExpenseItemResponse> addMultipleItems(Long claimId, AddMultipleItemRequest request, String userEmail){
        // finding claim
        ExpenseClaim claim = expenseClaimRepository.findById(claimId)
                .orElseThrow(()->new ClaimNotFoundException("claim not found with id: " +claimId));


        // finding user email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()->new UserNotFoundException("user not found "));

        // validate the user's own claim
        if(!claim.getEmployee().getId().equals(user.getId())){
            throw new RuntimeException("the items can only be added to your own claim");
        }

        //status = DRAFT
        if(claim.getStatus()!= ClaimStatus.DRAFT){
            throw new InvalidClaimStatus("the status must be draft inorder to add the items, Current Status: " +claim.getStatus());
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

        //return response
        return savedItems.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toUnmodifiableList());

    }


}
