package com.example.hoteluserservce.controller;

import com.example.hoteluserservce.dto.*;
import com.example.hoteluserservce.exception.UserAlreadyExistsException;
import com.example.hoteluserservce.service.AuthService;
import com.example.hoteluserservce.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * Регистрация нового пользователя
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());

        try {
            UserDto registeredUser = userService.registerUser(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Пользователь успешно зарегистрирован");
            response.put("user", registeredUser);

            log.info("User registered successfully: {}", registeredUser.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (UserAlreadyExistsException e) {
            log.warn("Registration failed - user already exists: {}", request.getEmail());
            return createErrorResponse(HttpStatus.CONFLICT, "Пользователь уже существует", e.getMessage());

        } catch (IllegalArgumentException e) {
            log.warn("Registration failed - invalid data: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Неверные данные", e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error during registration for email {}: {}",
                    request.getEmail(), e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Внутренняя ошибка сервера", "Попробуйте позже");
        }
    }

    /**
     * Авторизация пользователя
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());

        try {
            AuthResponse authResponse = authService.authenticateUser(request);

            log.info("User logged in successfully: {}", request.getEmail());
            return ResponseEntity.ok(authResponse);

        } catch (UsernameNotFoundException e) {
            log.warn("Login failed - user not found: {}", request.getEmail());
            return createErrorResponse(HttpStatus.UNAUTHORIZED,
                    "Неверные учетные данные", "Пользователь не найден");

        } catch (BadCredentialsException e) {
            log.warn("Login failed - invalid credentials: {}", request.getEmail());
            return createErrorResponse(HttpStatus.UNAUTHORIZED,
                    "Неверные учетные данные", "Неверный пароль");

        } catch (Exception e) {
            log.error("Unexpected error during login for email {}: {}",
                    request.getEmail(), e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Внутренняя ошибка сервера", "Попробуйте позже");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.UNAUTHORIZED, "Недействительный токен", e.getMessage());
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка сервера", "Попробуйте позже");
        }
    }



    /**
     * Создание ответа с ошибкой
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(
            HttpStatus status, String error, String message) {

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(status).body(response);
    }
}