package com.company.claimx.service;

import com.company.claimx.entity.EmployeeManager;
import com.company.claimx.entity.ExpenseClaim;
import com.company.claimx.repository.EmployeeManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManagerLookupService {

    @Autowired
    private EmployeeManagerRepository employeeManagerRepository;

    public String getManagerName(ExpenseClaim expenseClaim){
        if (expenseClaim.getApprovedBy()!= null){
            return expenseClaim.getApprovedByName();
        }

        return employeeManagerRepository.findByEmployee(expenseClaim.getEmployee())
                .map(EmployeeManager::getManager)
                .map(manager ->manager.getName())
                .orElse(null);
    }

    public Long getManagerId(ExpenseClaim expenseClaim){
        if (expenseClaim.getApprovedBy()!= null){
            return expenseClaim.getApprovedBy().getId();
        }

        return employeeManagerRepository.findByEmployee(expenseClaim.getEmployee())
                .map(EmployeeManager::getManager)
                .map(manager ->manager.getId())
                .orElse(null);
    }

}
