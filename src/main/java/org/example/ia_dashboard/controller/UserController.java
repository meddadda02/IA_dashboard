package org.example.ia_dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.example.ia_dashboard.Entity.User;
import org.example.ia_dashboard.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final org.example.ia_dashboard.Repository.FileRepository fileRepository;

    // ✅ GET son profil (JWT obligatoire) - Utilise l'ID de l'utilisateur connecté
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<org.example.ia_dashboard.dto.UserResponse> getProfile() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(mapToUserResponse(currentUser));
    }

    // ✅ PUT: modifier son profil (JWT obligatoire) - Seulement l'utilisateur peut
    // modifier son profil
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<org.example.ia_dashboard.dto.UserResponse> updateProfile(
            @RequestBody org.example.ia_dashboard.dto.UpdateUserRequest updatedUser) {
        User currentUser = userService.getCurrentUser();
        User user = userService.updateUser(currentUser.getId(), updatedUser);
        return ResponseEntity.ok(mapToUserResponse(user));
    }

    // ✅ DELETE: supprimer son compte (JWT obligatoire) - Seulement l'utilisateur
    // peut supprimer son compte
    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteProfile() {
        User currentUser = userService.getCurrentUser();
        userService.deleteUser(currentUser.getId());
        return ResponseEntity.ok("Account deleted successfully");
    }

    private org.example.ia_dashboard.dto.UserResponse mapToUserResponse(User user) {
        return org.example.ia_dashboard.dto.UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .analysisCount(fileRepository.countByUserId(user.getId()))
                .build();
    }
}
