package com.company.claimx.entity;

import com.company.claimx.enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * entity representing an expense claim
 */
@Entity
@Table(name="expense_claims")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseClaim {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="claim_id")
    private Long claimId;

    @Column(name="claim_number", nullable = false, unique = true, length = 50)
    private String claimNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id",nullable = false)
    private User employee;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "manager_id",nullable = false)
//    private User manager;

    @ManyToOne
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    @Column(name = "approved_by_name", length = 255)
    private String approvedByName;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "total_amount", nullable = false)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false)
    private ClaimStatus status;


    @Column(name = "reviewed_date")
    private LocalDateTime reviewedDate;

    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;


    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = ClaimStatus.DRAFT;
        }

        if (this.totalAmount == null) {
            this.totalAmount = BigDecimal.ZERO;  // Default amount
        }

    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
