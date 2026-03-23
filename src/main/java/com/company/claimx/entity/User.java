package com.company.claimx.entity;

import com.company.claimx.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name= "employee_code", nullable = false, unique = true, length = 50)       //
    private String employeeCode;

    @Column(name= "name", nullable = false, length = 255)
    private String name;

    @Column(name= "email", nullable = false, unique = true,length = 255)
    private String email;

    @Column(name= "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 255)
    private UserRole role;

    @Column(name= "is_active", nullable = false)
    private Boolean isActive= true;

    @Column(name= "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
    }
}
