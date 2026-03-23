package com.company.claimx.repository;

import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.entity.User;
import com.company.claimx.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExpenseClaimRepository extends JpaRepository<ExpenseClaim, Long> {

    // Find by employee
    List<ExpenseClaim> findByEmployee(User employee);

    // Find by employee and status
    List<ExpenseClaim> findByEmployeeAndStatus(User employee, ClaimStatus status);

    // Find by status
    List<ExpenseClaim> findByStatus(ClaimStatus status);

    boolean existsByClaimNumber(String claimNumber);


    @Query(value = "SELECT nextval('claim_number_seq')", nativeQuery = true)
    Long getNextClaimSequence();
}
