package com.company.claimx.repository;

import com.company.claimx.entity.AuditLog;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long > {


    List<AuditLog> findByClaim(ExpenseClaim claim);


    List<AuditLog> findByClaimClaimId(Long claimId);


    List<AuditLog> findByPerformedBy(User user);


    List<AuditLog> findByAction(String action);


    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);


    List<AuditLog> findAllByOrderByTimestampDesc();


    List<AuditLog> findByClaimClaimIdOrderByTimestampAsc(Long claimId);

    @Transactional
    void deleteByClaimClaimId(Long claimId);
}
