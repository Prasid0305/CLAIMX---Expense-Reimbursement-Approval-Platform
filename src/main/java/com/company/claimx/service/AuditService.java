package com.company.claimx.service;

import com.company.claimx.entity.AuditLog;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.entity.User;
import com.company.claimx.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    @Autowired
    AuditLogRepository auditLogRepository;

    public void logClaimAction(ExpenseClaim claim, User performedBy, String action, String oldStatus, String newStatus, String comments){
        AuditLog auditLog = new AuditLog();
        auditLog.setClaim(claim);
        auditLog.setPerformedBy(performedBy);
        auditLog.setAction(action);
        auditLog.setOldStatus(oldStatus);
        auditLog.setNewStatus(newStatus);
        auditLog.setComments(comments);

        auditLogRepository.save(auditLog);

    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    public List<AuditLog> getLogsForClaim(Long claimId) {
        return auditLogRepository.findByClaimClaimIdOrderByTimestampAsc(claimId);
    }
}
