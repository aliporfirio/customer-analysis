package com.aruba.customeranalysis.infrastructure.persistence.security;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aruba.customeranalysis.infrastructure.persistence.model.security.RoleEntity;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {}

