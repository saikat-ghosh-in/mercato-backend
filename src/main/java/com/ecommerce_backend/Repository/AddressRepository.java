package com.ecommerce_backend.Repository;

import com.ecommerce_backend.Entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByAddressId(String addressId);
}
