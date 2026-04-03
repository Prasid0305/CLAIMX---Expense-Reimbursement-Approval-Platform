package com.company.claimx.dto.request;


import com.company.claimx.enums.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * request dto to update the expense item
 * contains info of the item
 */

@Data

public class UpdateExpenseItemRequest {

    @NotNull(message = "category must not be null")
    private Category category;

    @NotNull(message = "description must not be null")
    private  String description;

    @NotNull(message = "amount must not be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "date is required ")
    private LocalDate expenseDate;
}
