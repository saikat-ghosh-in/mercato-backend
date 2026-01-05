package com.ecommerce_backend.Repository;

import com.ecommerce_backend.Entity.EcommUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<EcommUser, Long> {

    Optional<EcommUser> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}
