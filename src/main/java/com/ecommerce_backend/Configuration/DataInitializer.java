package com.ecommerce_backend.Configuration;

import com.ecommerce_backend.Entity.AppRole;
import com.ecommerce_backend.Entity.Role;
import com.ecommerce_backend.Repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        createRoleIfNotExists(AppRole.ROLE_USER);
        createRoleIfNotExists(AppRole.ROLE_SELLER);
        createRoleIfNotExists(AppRole.ROLE_ADMIN);
    }

    private void createRoleIfNotExists(AppRole roleName) {
        roleRepository.findByRoleName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }
}