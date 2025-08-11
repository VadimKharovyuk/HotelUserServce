package com.example.hoteluserservce.service.impl;

import com.example.hoteluserservce.dto.AuthResponse;
import com.example.hoteluserservce.dto.LoginRequest;
import com.example.hoteluserservce.mapper.UserMapper;
import com.example.hoteluserservce.model.RefreshToken;
import com.example.hoteluserservce.model.User;
import com.example.hoteluserservce.repository.RefreshTokenRepository;
import com.example.hoteluserservce.repository.UserRepository;
import com.example.hoteluserservce.service.AuthService;
import com.example.hoteluserservce.service.UserService;
import com.example.hoteluserservce.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountLockedException;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Value("${jwt.access-token.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;


    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


    @Override
    public AuthResponse authenticateUser(LoginRequest request) throws AccountLockedException {
        log.info("Attempting to authenticate user: {}", request.getEmail());

        try {
            // 1. Найти пользователя по email
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

            // 2. Проверить, не заблокирован ли аккаунт
            if (user.isAccountLocked()) {
                log.warn("Attempt to login with locked account: {}", request.getEmail());
                throw new AccountLockedException("Аккаунт заблокирован");
            }

            // 3. Проверить пароль
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                log.warn("Invalid password for user: {}", request.getEmail());
                throw new BadCredentialsException("Неверный пароль");
            }

            // 4. Генерировать токены
            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken();

            // 5. Сохранить refresh token в базе
            RefreshToken refreshTokenEntity = RefreshToken.builder()
                    .token(refreshToken)
                    .userId(user.getId())
                    .expiresAt(LocalDateTime.now().plus(
                            Duration.ofMillis(refreshTokenExpiration)
                    ))
                    .build();

            tokenRepository.save(refreshTokenEntity);

            // 6. Обновить время последнего входа
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            log.info("User authenticated successfully: {}", user.getEmail());

            // 7. Вернуть ответ
            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(accessTokenExpiration / 1000) // в секундах
                    .user(userMapper.toUserDto(user))
                    .build();

        } catch (UsernameNotFoundException | BadCredentialsException | AccountLockedException e) {
            log.error("Authentication failed for user {}: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during authentication for user {}: {}",
                    request.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Ошибка аутентификации", e);
        }
    }
}
