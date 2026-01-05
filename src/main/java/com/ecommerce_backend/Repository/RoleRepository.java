package com.ecommerce_backend.Repository;

import com.ecommerce_backend.Entity.AppRole;
import com.ecommerce_backend.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRole appRole);
}
