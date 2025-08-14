package com.example.hoteluserservce.controller;

import com.example.hoteluserservce.dto.user.UpdateUserDto;
import com.example.hoteluserservce.dto.user.UserDto;
import com.example.hoteluserservce.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    // ✅ Для текущего пользователя - через Authentication (токен)
    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(Authentication authentication) {
        String username = authentication.getName();
        UserDto profile = userService.getUserByUsername(username);
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
