package org.example.ia_dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.example.ia_dashboard.Entity.User;
import org.example.ia_dashboard.security.JwtUtils;
import org.example.ia_dashboard.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    // Inscription
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody org.example.ia_dashboard.dto.RegisterRequest request) {
        if (userService.findByUsername(request.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        User savedUser = userService.saveUser(user);

        return ResponseEntity.ok(org.example.ia_dashboard.dto.UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .role(savedUser.getRole())
                .build());
    }

    // Connexion
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody org.example.ia_dashboard.dto.LoginRequest request) {
        User user = userService.findByUsername(request.getUsername());
        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // Note: generateToken signature might have changed in JwtUtils, checking that next
            String token = jwtUtils.generateToken(user.getUsername());
            return ResponseEntity.ok(org.example.ia_dashboard.dto.AuthResponse.builder()
                    .token(token)
                    .build());
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}
