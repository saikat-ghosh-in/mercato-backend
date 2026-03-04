package com.ecommerce_backend.Repository;

import com.ecommerce_backend.Entity.AppRole;
import com.ecommerce_backend.Entity.EcommUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<EcommUser, Long> {

    Optional<EcommUser> findByUserId(String orderNumber);

    Optional<EcommUser> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM EcommUser u JOIN u.roles r WHERE r.roleName IN :roleNames")
    List<EcommUser> findUsersByRoleNames(@Param("roleNames") List<AppRole> roleNames);
}
