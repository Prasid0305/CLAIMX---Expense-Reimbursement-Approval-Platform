package com.company.claimx.dto.response;

import com.company.claimx.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * response dto to expense item
 * includes item id, claim id, category, etc.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder //clean build
public class ExpenseItemResponse {
    private Long itemId;
    private Long claimId;
    private LocalDate expenseDate;
    private Category category;
    private BigDecimal amount;
    private String description;
}
