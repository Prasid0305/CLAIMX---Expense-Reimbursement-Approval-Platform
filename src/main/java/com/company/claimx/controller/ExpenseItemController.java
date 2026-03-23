package com.company.claimx.controller;


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


@RestController
@RequestMapping("/api/claims/{claimId}/items")
@Tag(name = "Employee expense items", description = "manage expense items within claims")
public class ExpenseItemController {

    @Autowired
    private ExpenseItemService expenseItemService;

    @PostMapping
    public ResponseEntity<ExpenseItemResponse> addItem(@PathVariable Long claimId, @Valid @RequestBody AddExpenseItemRequest addExpenseItemRequest){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        ExpenseItemResponse expenseItemResponse = expenseItemService.addItem(claimId, addExpenseItemRequest, userEmail);

        return new ResponseEntity<>(expenseItemResponse, HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<ExpenseItemResponse>> getItems(@PathVariable Long claimId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        List<ExpenseItemResponse> items = expenseItemService.getItemByClaim(claimId, userEmail);

        return ResponseEntity.ok(items);
    }

    @DeleteMapping("/delete/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long claimId, @PathVariable Long itemId){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        expenseItemService.deleteItem(claimId, itemId, userEmail );
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{itemId}")
    public ResponseEntity<ExpenseItemResponse> updateExpenseItem(@PathVariable Long claimId, @PathVariable Long itemId, @Valid @RequestBody UpdateExpenseItemRequest request){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        ExpenseItemResponse expenseItemResponse= expenseItemService.updateExpenseItem(claimId, itemId, request, userEmail);

        return ResponseEntity.ok(expenseItemResponse);
    }

    @PostMapping("/multipleItems")
    public ResponseEntity<List<ExpenseItemResponse>> addMultipleItem(@PathVariable Long claimId, @Valid @RequestBody  AddMultipleItemRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        List<ExpenseItemResponse> expenseItemResponse = expenseItemService.addMultipleItems(claimId, request, userEmail);

        return new ResponseEntity<>(expenseItemResponse, HttpStatus.CREATED);
    }
}
