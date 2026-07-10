package com.codegym.config;

import com.codegym.model.user.AppUser;
import com.codegym.model.user.Role;
import com.codegym.repository.user.AppUserRepository;
import com.codegym.repository.user.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.email:admin@gmail.com}")
    private String adminEmail;

    @Value("${admin.default.password:123456}")
    private String adminPassword;

    @Value("${admin.default.fullname:System Admin}")
    private String adminFullName;

    @Override
    public void run(String... args) {
        log.info("Checking and seeding default data...");

        // Ensure ROLE_ADMIN exists
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ROLE_ADMIN");
                    role.setDescription("Administrator Role");
                    return roleRepository.save(role);
                });

        // Ensure Admin user exists
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            AppUser adminUser = new AppUser();
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setFullName(adminFullName);
            adminUser.setRole(adminRole);
            adminUser.setCreatedAt(LocalDateTime.now());
            userRepository.save(adminUser);
            log.info("Default Admin account created with email: {}", adminEmail);
        } else {
            log.info("Default Admin account already exists.");
        }
    }
}
