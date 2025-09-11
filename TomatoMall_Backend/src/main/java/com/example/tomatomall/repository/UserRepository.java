package com.example.tomatomall.repository;

import com.example.tomatomall.po.UserPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserPO, Long> {
    Optional<UserPO> findByUsername(String username);
    boolean existsByUsername(String username);
} 