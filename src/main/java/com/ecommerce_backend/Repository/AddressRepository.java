package com.ecommerce_backend.Repository;

import com.ecommerce_backend.Entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
