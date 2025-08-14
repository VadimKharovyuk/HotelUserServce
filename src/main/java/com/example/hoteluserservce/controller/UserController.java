package com.example.hoteluserservce.controller;

import com.example.hoteluserservce.dto.user.UpdateUserDto;
import com.example.hoteluserservce.dto.user.UserDto;
import com.example.hoteluserservce.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;


    @Value("${server.port:1511}")
    private String serverPort;


    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(Authentication authentication, HttpServletRequest request) {
        String username = authentication.getName();

        log.info("🎯 [ОТВЕЧАЕТ ПОРТ-{}] Profile request for user: {}",
                serverPort, username);

        UserDto profile = userService.getUserByUsername(username);

        log.info("✅ [ОТВЕЧАЕТ ПОРТ-{}] Profile successfully returned for user: {} (ID: {})",
                serverPort, username, profile.getId());

        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserDto updateDto) {
        String username = authentication.getName();
        UserDto updatedUser = userService.updateUserByUsername(username, updateDto);
        return ResponseEntity.ok(updatedUser);
    }






}
