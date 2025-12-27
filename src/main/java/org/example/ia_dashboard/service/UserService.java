package org.example.ia_dashboard.service;

import lombok.RequiredArgsConstructor;
import org.example.ia_dashboard.Entity.User;
import org.example.ia_dashboard.Repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final org.example.ia_dashboard.Repository.FileRepository fileRepository;
    private final @Lazy PasswordEncoder passwordEncoder;

    public User getCurrentUser() {
        if (SecurityContextHolder.getContext().getAuthentication() == null ||
                !SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().replace("ROLE_", ""))
                .build();
    }

    public User updateUser(Long id, org.example.ia_dashboard.dto.UpdateUserRequest updatedUser) {
        return userRepository.findById(id)
                .map(user -> {
                    if (updatedUser.getUsername() != null && !updatedUser.getUsername().isEmpty()) {
                        // Check if new username is already taken by another user
                        userRepository.findByUsername(updatedUser.getUsername())
                                .ifPresent(existingUser -> {
                                    if (!existingUser.getId().equals(id)) {
                                        throw new RuntimeException("Username already exists");
                                    }
                                });
                        user.setUsername(updatedUser.getUsername());
                    }
                    if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                        user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
                    }
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @org.springframework.transaction.annotation.Transactional
    public boolean deleteUser(Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    fileRepository.deleteByUserId(id);
                    userRepository.delete(user);
                    return true;
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean isAdmin(User user) {
        return user.getRole().equals("ROLE_ADMIN");
    }

    public User changeUserRole(Long userId, String newRole) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setRole(newRole);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }
}
