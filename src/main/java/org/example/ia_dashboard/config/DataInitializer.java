package org.example.ia_dashboard.config;

import lombok.RequiredArgsConstructor;
import org.example.ia_dashboard.Entity.User;
import org.example.ia_dashboard.Repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create Admin user if not exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);
            System.out.println("Default admin user created: username=admin, password=admin123");
        }

        // Create Regular user if not exists
        if (userRepository.findByUsername("user1").isEmpty()) {
            User user = new User();
            user.setUsername("user1");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole("ROLE_USER");
            userRepository.save(user);
            System.out.println("Default regular user created: username=user1, password=password123");
        }
    }
}
