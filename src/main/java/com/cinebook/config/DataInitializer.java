package com.cinebook.config;

import com.cinebook.entity.User;
import com.cinebook.enums.Role;
import com.cinebook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Runs once on startup.
 * Creates the default admin account if it does not already exist.
 *
 * Configure via .env:
 *   ADMIN_EMAIL=admin@cinebook.com
 *   ADMIN_PASSWORD=yourStrongPassword
 *   ADMIN_FIRST_NAME=Super
 *   ADMIN_LAST_NAME=Admin
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@cinebook.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;

    @Value("${app.admin.first-name:Super}")
    private String adminFirstName;

    @Value("${app.admin.last-name:Admin}")
    private String adminLastName;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .firstName(adminFirstName)
                    .lastName(adminLastName)
                    .role(Role.ROLE_ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("✅ Default admin account created: {}", adminEmail);
            log.info("⚠️  Change the admin password immediately via the profile page!");
        } else {
            log.info("ℹ️  Admin account already exists: {}", adminEmail);
        }
    }
}
