package com.company.claimx.repository;

import com.company.claimx.entity.EmployeeManager;
import com.company.claimx.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeManagerRepository extends JpaRepository<EmployeeManager, Long> {

    Optional<EmployeeManager> findByEmployee(User employee);

    Optional<EmployeeManager> findByEmployeeId(Long employeeId);

    List<EmployeeManager> findByManager(User manager);

    List<EmployeeManager> findByManagerId(Long managerId);

    boolean existsByEmployee(User employee);

    boolean existsByEmployeeId(User employeeId);

}
