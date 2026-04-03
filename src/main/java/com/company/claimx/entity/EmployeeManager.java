package com.company.claimx.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * entity representing employee_manager
 */
@Entity
@Table(name="employee_manager")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeManager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id",nullable = false, unique = true)
    private User employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "manager_id", nullable = false)
    private User manager;


}
