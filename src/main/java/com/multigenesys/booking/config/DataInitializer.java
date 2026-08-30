package com.multigenesys.booking.config;

import com.multigenesys.booking.entity.Role;
import com.multigenesys.booking.entity.User;
import com.multigenesys.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
    }

    private void seedUsers() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .fullName("System Administrator")
                    .role(Role.ROLE_ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Default ADMIN user seeded: admin / admin@example.com");
        }

        if (!userRepository.existsByUsername("user1")) {
            User user1 = User.builder()
                    .username("user1")
                    .email("user1@example.com")
                    .password(passwordEncoder.encode("User@123"))
                    .fullName("John Doe")
                    .role(Role.ROLE_USER)
                    .build();
            userRepository.save(user1);
            log.info("Default regular user seeded: user1 / user1@example.com");
        }

        if (!userRepository.existsByUsername("user2")) {
            User user2 = User.builder()
                    .username("user2")
                    .email("user2@example.com")
                    .password(passwordEncoder.encode("User@123"))
                    .fullName("Jane Smith")
                    .role(Role.ROLE_USER)
                    .build();
            userRepository.save(user2);
            log.info("Default secondary user seeded: user2 / user2@example.com");
        }
    }
}
