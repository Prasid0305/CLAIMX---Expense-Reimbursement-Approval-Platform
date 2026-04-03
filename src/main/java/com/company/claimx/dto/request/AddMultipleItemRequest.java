package com.company.claimx.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * request dto for adding multiple items
 * contains a list to add the items
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddMultipleItemRequest {

    @NotNull
    private List<AddExpenseItemRequest> items;
}
