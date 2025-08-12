package com.example.hoteluserservce.service.impl;

import com.example.hoteluserservce.dto.user.RegisterRequest;
import com.example.hoteluserservce.dto.user.UserDto;
import com.example.hoteluserservce.enums.UserRole;
import com.example.hoteluserservce.exception.UserAlreadyExistsException;
import com.example.hoteluserservce.mapper.UserMapper;
import com.example.hoteluserservce.model.User;
import com.example.hoteluserservce.repository.RefreshTokenRepository;
import com.example.hoteluserservce.repository.UserRepository;
import com.example.hoteluserservce.service.UserService;
import com.example.hoteluserservce.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RefreshTokenRepository tokenRepository;
    private final JwtUtil jwtUtil;


    @Override
    @Transactional
    public UserDto registerUser(RegisterRequest request) {
        log.info("Attempting to register new user with email: {}", request.getEmail());

        try {
            // 1. Валидация входных данных
            validateRegisterRequest(request);

            // 2. Проверить, не существует ли пользователь с таким email
            if (userRepository.existsByEmail(request.getEmail())) {
                log.warn("Registration failed: email already exists - {}", request.getEmail());
                throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
            }



            // 4. Хешировать пароль
            String hashedPassword = passwordEncoder.encode(request.getPassword());

            // 5. Создать нового пользователя
            User newUser = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(hashedPassword)
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())
                    .role(UserRole.USER) // По умолчанию обычный пользователь
                    .emailVerified(false) // По умолчанию не подтвержден
                    .accountLocked(false) // По умолчанию не заблокирован
                    .build();

            // 6. Сохранить пользователя в базе данных
            User savedUser = userRepository.save(newUser);

            log.info("User registered successfully with ID: {} and email: {}",
                    savedUser.getId(), savedUser.getEmail());

            // 7. Преобразовать в DTO и вернуть (без пароля!)
            return userMapper.toUserDto(savedUser);

        } catch (UserAlreadyExistsException e) {
            log.error("Registration failed for email {}: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during registration for email {}: {}",
                    request.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Ошибка регистрации пользователя", e);
        }
    }

    @Override
    public UserDto getUserById(Long userId) {
        log.info("Getting user by ID: {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

            log.info("User found: {}", user.getEmail());
            return userMapper.toUserDto(user);

        } catch (UsernameNotFoundException e) {
            log.error("User not found with ID: {}", userId);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Ошибка получения пользователя", e);
        }
    }

    @Override
    public UserDto getUserByUsername(String username) {
        log.info("Getting user by username: {}", username);

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

            log.info("User found: {} ({})", user.getUsername(), user.getEmail());
            return userMapper.toUserDto(user);

        } catch (UsernameNotFoundException e) {
            log.error("User not found with username: {}", username);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Ошибка получения пользователя", e);
        }
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Запрос на регистрацию не может быть пустым");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email обязателен для заполнения");
        }

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя обязательно для заполнения");
        }

        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Пароль должен содержать минимум 6 символов");
        }

        // Проверка на валидность email
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("Неверный формат email");
        }

        // Проверка длины username
        if (request.getUsername().length() < 3 || request.getUsername().length() > 50) {
            throw new IllegalArgumentException("Имя пользователя должно быть от 3 до 50 символов");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
