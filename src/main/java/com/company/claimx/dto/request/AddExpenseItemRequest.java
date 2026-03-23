package com.company.claimx.dto.request;

import com.company.claimx.enums.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AddExpenseItemRequest {

    @NotNull(message="mention- Travel/accommodation/food/office_supplies/communication/other")
    private Category category;

    @NotNull(message = "description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull(message = "expense date is required")
    @PastOrPresent
    private LocalDate expenseDate;

    public AddExpenseItemRequest(Category category, String description, BigDecimal amount, LocalDate expenseDate) {
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.expenseDate = expenseDate;
    }

    public AddExpenseItemRequest() {
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }
}

