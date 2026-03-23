package com.company.claimx.repository;

import com.company.claimx.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmployeeCode(String employeeCode);

    Optional<User> findByEmail(String email);



    boolean existsByEmail(String email);

    boolean existsByEmployeeCode(String employeeCode);


}
