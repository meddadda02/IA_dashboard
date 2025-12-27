package org.example.ia_dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.example.ia_dashboard.Entity.User;
import org.example.ia_dashboard.dto.UserResponse;
import org.example.ia_dashboard.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final org.example.ia_dashboard.Repository.FileRepository fileRepository;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        List<User> allUsers = userService.getAllUsers();
        stats.put("totalUsers", allUsers.size());
        stats.put("adminUsers", allUsers.stream().filter(u -> u.getRole().equals("ROLE_ADMIN")).count());
        stats.put("regularUsers", allUsers.stream().filter(u -> u.getRole().equals("ROLE_USER")).count());
        stats.put("systemStatus", "Operational");
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable Long userId, @RequestParam String newRole) {
        if (!newRole.equals("ROLE_USER") && !newRole.equals("ROLE_ADMIN")) {
            return ResponseEntity.badRequest().body("Invalid role");
        }

        User updatedUser = userService.changeUserRole(userId, newRole);
        return ResponseEntity.ok(mapToUserResponse(updatedUser));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .analysisCount(fileRepository.countByUserId(user.getId()))
                .build();
    }
}
