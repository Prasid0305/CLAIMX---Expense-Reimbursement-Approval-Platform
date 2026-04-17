package com.company.claimx.service;

import com.company.claimx.entity.AuditLog;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.entity.User;
import com.company.claimx.repository.AuditLogRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The service class contains the business logic of audit log
 * the audit log records all when there is a Status change, records the user who performed the action and what was the old and the new status
 */
@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    @Autowired
    AuditLogRepository auditLogRepository;

    /**
     * this logs the claim action
     *
     * @param claim       - expense claim which undergoes the change
     * @param performedBy - the user who was responsible for the change
     * @param action      - the action that was performed
     * @param oldStatus   - previous status
     * @param newStatus   - new status
     */
    @Transactional
    public void logClaimAction(ExpenseClaim claim, User performedBy, String action, String oldStatus, String newStatus){
        AuditLog auditLog = new AuditLog();
        auditLog.setClaim(claim);
        auditLog.setPerformedBy(performedBy);
        auditLog.setAction(action);
        auditLog.setOldStatus(oldStatus);
        auditLog.setNewStatus(newStatus);

        auditLogRepository.save(auditLog);

    }

    /**
     * to get all the logs with the descending time stamp
     * @return list of all the logs in descending time
     */
    public List<AuditLog> getAllLogs() {

        logger.info("Retrieving all the logs ");
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    /**
     * to get the logs for the particular claim
     * @param claimId
     * @return
     */
    public List<AuditLog> getLogsForClaim(Long claimId) {
        return auditLogRepository.findByClaimClaimIdOrderByTimestampAsc(claimId);
    }
}
