package com.example.hoteluserservce.service.impl;

import com.example.hoteluserservce.dto.user.AuthResponse;
import com.example.hoteluserservce.dto.user.LoginRequest;
import com.example.hoteluserservce.dto.user.RefreshTokenRequest;
import com.example.hoteluserservce.mapper.UserMapper;
import com.example.hoteluserservce.model.RefreshToken;
import com.example.hoteluserservce.model.User;
import com.example.hoteluserservce.repository.RefreshTokenRepository;
import com.example.hoteluserservce.repository.UserRepository;
import com.example.hoteluserservce.service.AuthService;
import com.example.hoteluserservce.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) throws AccountLockedException {
        log.info("Attempting to refresh token: {}", request.getRefreshToken());

        try {
            // 1. Найти refresh token в базе данных
            RefreshToken refreshTokenEntity = tokenRepository.findByToken(request.getRefreshToken())
                    .orElseThrow(() -> new IllegalArgumentException("Недействительный refresh token"));

            // 2. Проверить, не отозван ли токен
            if (refreshTokenEntity.isRevoked()) {
                log.warn("Attempt to use revoked refresh token: {}", request.getRefreshToken());
                throw new IllegalArgumentException("Refresh token отозван");
            }

            // 3. Проверить срок действия
            if (refreshTokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.warn("Attempt to use expired refresh token: {}", request.getRefreshToken());
                // Удалить просроченный токен из базы
                tokenRepository.delete(refreshTokenEntity);
                throw new IllegalArgumentException("Refresh token истек");
            }

            // 4. Найти пользователя
            User user = userRepository.findById(refreshTokenEntity.getUserId())
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

            // 5. Проверить, не заблокирован ли аккаунт
            if (user.isAccountLocked()) {
                log.warn("Attempt to refresh token for locked account: {}", user.getEmail());
                throw new AccountLockedException("Аккаунт заблокирован");
            }

            // 6. Генерировать новый access token
            String newAccessToken = jwtUtil.generateAccessToken(user);

            // 7. Генерировать новый refresh token
            String newRefreshToken = jwtUtil.generateRefreshToken();

            // 8. Отозвать старый refresh token
            refreshTokenEntity.setRevoked(true);
            tokenRepository.save(refreshTokenEntity);

            // 9. Сохранить новый refresh token
            RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                    .token(newRefreshToken)
                    .userId(user.getId())
                    .expiresAt(LocalDateTime.now().plus(
                            Duration.ofMillis(refreshTokenExpiration)
                    ))
                    .build();

            tokenRepository.save(newRefreshTokenEntity);

            log.info("Token refreshed successfully for user: {}", user.getEmail());

            // 10. Вернуть новые токены
            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(accessTokenExpiration / 1000) // в секундах
                    .user(userMapper.toUserDto(user))
                    .build();

        } catch (IllegalArgumentException | UsernameNotFoundException | AccountLockedException e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during token refresh: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка обновления токена", e);
        }
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        log.info("Attempting to logout with refresh token");

        try {
            // Найти и отозвать refresh token
            RefreshToken tokenEntity = tokenRepository.findByToken(refreshToken)
                    .orElseThrow(() -> new IllegalArgumentException("Недействительный refresh token"));

            if (!tokenEntity.isRevoked()) {
                tokenEntity.setRevoked(true);
                tokenRepository.save(tokenEntity);
                log.info("User logged out successfully from single device");
            } else {
                log.warn("Attempt to logout with already revoked token");
                throw new IllegalArgumentException("Токен уже отозван");
            }

        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            throw new RuntimeException("Ошибка при выходе", e);
        }
    }
}
