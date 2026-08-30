package com.multigenesys.booking.config;

import com.multigenesys.booking.entity.Resource;
import com.multigenesys.booking.entity.ResourceType;
import com.multigenesys.booking.entity.Role;
import com.multigenesys.booking.entity.User;
import com.multigenesys.booking.repository.ResourceRepository;
import com.multigenesys.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedResources();
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

    private void seedResources() {
        if (resourceRepository.count() == 0) {
            Resource conferenceRoom = Resource.builder()
                    .name("Grand Conference Room A")
                    .description("High-capacity conference room equipped with 4K projector, surround sound, and video conferencing.")
                    .type(ResourceType.CONFERENCE_HALL)
                    .pricePerHour(new BigDecimal("150.00"))
                    .isAvailable(true)
                    .build();

            Resource executiveVan = Resource.builder()
                    .name("Executive Shuttle Van")
                    .description("8-seater luxury electric shuttle for corporate transport.")
                    .type(ResourceType.VEHICLE)
                    .pricePerHour(new BigDecimal("85.00"))
                    .isAvailable(true)
                    .build();

            Resource studioCamera = Resource.builder()
                    .name("Sony FX3 Cinema Camera Kit")
                    .description("Full frame cinema camera with GM prime lenses and wireless audio kit.")
                    .type(ResourceType.EQUIPMENT)
                    .pricePerHour(new BigDecimal("45.00"))
                    .isAvailable(true)
                    .build();

            Resource hotDesk = Resource.builder()
                    .name("Dedicated Workspace Desk 12")
                    .description("Ergonomic sit-stand desk with dual 4K monitors in quiet zone.")
                    .type(ResourceType.DESK)
                    .pricePerHour(new BigDecimal("15.00"))
                    .isAvailable(true)
                    .build();

            resourceRepository.save(conferenceRoom);
            resourceRepository.save(executiveVan);
            resourceRepository.save(studioCamera);
            resourceRepository.save(hotDesk);
            log.info("Sample bookable resources seeded successfully.");
        }
    }
}
