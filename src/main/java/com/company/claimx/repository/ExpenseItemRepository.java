package com.company.claimx.repository;

import com.company.claimx.entity.ExpenseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseItemRepository extends JpaRepository<ExpenseItem, Long> {


    List<ExpenseItem> findByClaimClaimId(Long claimId);

    //COALESCE IS USED BECAUSE - if expression is NULL then return fallback_value that is 0
    @Query(value = "SELECT COALESCE(SUM(ei.amount), 0) FROM ExpenseItem ei WHERE ei.claim.claimId=:claimId")
    BigDecimal calculateTotalByClaimId(@Param("claimId") Long claimId);

    long countByClaimClaimId(Long claimId);


    void deleteByClaimClaimId(Long claimId);

}
