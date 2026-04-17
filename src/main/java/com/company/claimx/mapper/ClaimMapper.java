package com.company.claimx.mapper;

import com.company.claimx.dto.response.AuditLogResponse;
import com.company.claimx.dto.response.ClaimResponse;
import com.company.claimx.dto.response.ExpenseItemResponse;
import com.company.claimx.dto.response.UserResponse;
import com.company.claimx.entity.AuditLog;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.entity.ExpenseItem;
import com.company.claimx.entity.User;
import com.company.claimx.service.ManagerLookupService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses={ManagerLookupService.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class ClaimMapper {

    @Autowired
    public ManagerLookupService managerLookupService;


    /**
     * Converts ExpenseClaim entity to claim ClaimResponse dto
     * @param expenseClaim expenseClaim
     * @return ClaimResponse dto
     */
    @Mapping(target = "employeeName", source = "employee.name")
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeCode", source = "employee.employeeCode")
    @Mapping(target = "managerName", expression = "java(managerLookupService.getManagerName(expenseClaim))")
    @Mapping(target = "managerId", expression = "java(managerLookupService.getManagerId(expenseClaim))")
    @Mapping(target = "items", source = "items")
    public abstract ClaimResponse toClaimResponse(ExpenseClaim expenseClaim);


    /**
     *converts list of claims to list of responses.
     * @param claims list of expense claims
     * @return list of claim responses
     */
    public abstract List<ClaimResponse> toClaimResponseList(List<ExpenseClaim> claims);

    /**
     * itemId - field name in ExpenseItemResponse (target)
     * id - field in ExpenseItem (source)
     * @param item the expense item entity
     * @return ExpenseItemResponse dto
     */

    @Mapping(target = "claimId", source = "claim.claimId")
    public abstract ExpenseItemResponse toExpenseItemResponse(ExpenseItem item);


    public abstract List<ExpenseItemResponse> toExpenseItemResponseList(List<ExpenseItem> items);


    public abstract UserResponse userResponse(User user);

    public abstract List<UserResponse> userResponsesList(List<User> user);

    @Mapping(target = "claimId", source = "claim.claimId")
    @Mapping(target = "claimNumber", source = "claim.claimNumber")
    @Mapping(target = "performedBy", source = "performedBy.email")
    public abstract AuditLogResponse auditLogResponse(AuditLog auditLog);

    public abstract List<AuditLogResponse> auditLogResponseList (List<AuditLog> auditLogs);

}
