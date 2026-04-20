package com.bloghub.controller;

import com.bloghub.dto.UserResponse;
import com.bloghub.entity.User;
import com.bloghub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(mapToResponse(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody UserResponse request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        
        user.setName(request.getName());
        user.setBio(request.getBio());
        user.setProfileImage(request.getProfileImage());
        
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(mapToResponse(savedUser));
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .bio(user.getBio())
                .profileImage(user.getProfileImage())
                .isMember(user.isMember())
                .subscriptionType(user.getSubscriptionType().name())
                .freeStoriesRemaining(user.getFreeStoriesRemaining())
                .build();
    }
}