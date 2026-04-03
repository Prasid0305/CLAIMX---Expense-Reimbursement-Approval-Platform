package com.company.claimx.controller;


import com.company.claimx.annotation.Authenticated;
import com.company.claimx.context.AuthenticationContext;
import com.company.claimx.dto.request.AddExpenseItemRequest;
import com.company.claimx.dto.request.AddMultipleItemRequest;
import com.company.claimx.dto.request.UpdateClaimRequest;
import com.company.claimx.dto.request.UpdateExpenseItemRequest;
import com.company.claimx.dto.response.ExpenseItemResponse;
import com.company.claimx.service.ExpenseItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * controller class for the expense item operation
 * provides the endpoints to create the item, retrieve the items, get the items by id, delete the items, update the item, add multiple items
 */
@RestController
@RequestMapping("/api/claims/{claimId}/items")
@Tag(name = "Employee expense items", description = "manage expense items within claims")
public class ExpenseItemController {

    @Autowired
    private ExpenseItemService expenseItemService;

    /**
     * endpoint to add the item for the claim
     * @param claimId - id of the claim that the iotem needs to be added
     * @param addExpenseItemRequest - expense item request
     * @return - expense item details
     */
    @PostMapping
    @Authenticated
    public ResponseEntity<ExpenseItemResponse> addItem(@PathVariable Long claimId, @Valid @RequestBody AddExpenseItemRequest addExpenseItemRequest){

        String userEmail = AuthenticationContext.getUserEmail();

        ExpenseItemResponse expenseItemResponse = expenseItemService.addItem(claimId, addExpenseItemRequest, userEmail);

        return new ResponseEntity<>(expenseItemResponse, HttpStatus.CREATED);
    }

    /**
     * endpoint to get all the items
     * @param claimId - id of the claim
     * @return
     */
    @GetMapping
    @Authenticated
    public ResponseEntity<List<ExpenseItemResponse>> getItems(@PathVariable Long claimId) {

        String userEmail = AuthenticationContext.getUserEmail();

        List<ExpenseItemResponse> items = expenseItemService.getItemByClaim(claimId, userEmail);

        return ResponseEntity.ok(items);
    }

    /**
     * end point to delete the item for the id
     * @param claimId - id of the claim
     * @param itemId - id of the item
     * @return - success code
     */
    @DeleteMapping("/delete/{itemId}")
    @Authenticated
    public ResponseEntity<Void> deleteItem(@PathVariable Long claimId, @PathVariable Long itemId){

        String userEmail = AuthenticationContext.getUserEmail();

        expenseItemService.deleteItem(claimId, itemId, userEmail );
        return ResponseEntity.noContent().build();
    }

    /**
     * endpoint to update the item's detail
     * @param claimId  - id of the claim
     * @param itemId - id of the item
     * @param request - expense item request
     * @return - updated item response
     */
    @PutMapping("/update/{itemId}")
    @Authenticated
    public ResponseEntity<ExpenseItemResponse> updateExpenseItem(@PathVariable Long claimId, @PathVariable Long itemId, @Valid @RequestBody UpdateExpenseItemRequest request){

        String userEmail = AuthenticationContext.getUserEmail();

        ExpenseItemResponse expenseItemResponse= expenseItemService.updateExpenseItem(claimId, itemId, request, userEmail);

        return ResponseEntity.ok(expenseItemResponse);
    }

    /**
     * endpoint to add multiple items
     * @param claimId - id of the claim
     * @param request - list of the items
     * @return - all the items that were added
     */
    @PostMapping("/multipleItems")
    @Authenticated
    public ResponseEntity<List<ExpenseItemResponse>> addMultipleItem(@PathVariable Long claimId, @Valid @RequestBody  AddMultipleItemRequest request){

        String userEmail = AuthenticationContext.getUserEmail();

        List<ExpenseItemResponse> expenseItemResponse = expenseItemService.addMultipleItems(claimId, request, userEmail);

        return new ResponseEntity<>(expenseItemResponse, HttpStatus.CREATED);
    }
}
