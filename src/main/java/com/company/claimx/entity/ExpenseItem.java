package com.company.claimx.entity;

import com.company.claimx.enums.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="expense_item")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseItem {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private ExpenseClaim claim;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "category",nullable = false)
    private Category category;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "description",nullable = false)
    private String description;

}
