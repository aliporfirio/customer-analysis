package com.aruba.customeranalysis.infrastructure.persistence.security;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aruba.customeranalysis.infrastructure.persistence.model.security.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
}

