package com.mercato.Repository;

import com.mercato.Entity.AppRole;
import com.mercato.Entity.EcommUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<EcommUser, Long> {

    Optional<EcommUser> findByUserId(String orderId);

    Optional<EcommUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM EcommUser u JOIN u.roles r WHERE r.roleName IN :roleNames")
    List<EcommUser> findUsersByRoleNames(@Param("roleNames") List<AppRole> roleNames);

    @Query("SELECT COUNT(u) FROM EcommUser u JOIN u.roles r WHERE r.roleName = :role")
    long countByRole(@Param("role") AppRole role);

    @Query("SELECT COUNT(u) FROM EcommUser u WHERE u.createdAt BETWEEN :start AND :end")
    long countByCreatedAtBetween(@Param("start") Instant start, @Param("end") Instant end);
}
