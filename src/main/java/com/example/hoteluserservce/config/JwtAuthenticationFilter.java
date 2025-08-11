package com.example.hoteluserservce.config;

import com.example.hoteluserservce.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Проверяем наличие токена
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Извлекаем токен
        jwt = authHeader.substring(7);

        try {
            // Извлекаем username из токена
            username = jwtUtil.extractUsername(jwt);

            // Если пользователь не аутентифицирован
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Валидируем токен
                if (jwtUtil.validateToken(jwt, username)) {

                    // Извлекаем роль из токена
                    String role = jwtUtil.extractRole(jwt);
                    Long userId = jwtUtil.extractUserId(jwt);

                    // Создаем authorities
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + role)
                    );

                    // Создаем аутентификацию
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    authorities
                            );

                    // Добавляем детали запроса
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Устанавливаем аутентификацию в контекст
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // Добавляем userId в request для удобства
                    request.setAttribute("userId", userId);
                    request.setAttribute("userRole", role);
                }
            }
        } catch (JwtException e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            // Можно отправить 401 ошибку или просто продолжить без аутентификации
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Пропускаем фильтр для публичных эндпоинтов
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/") ||
                path.startsWith("/actuator/") ||
                path.equals("/") ||
                path.startsWith("/swagger-") ||
                path.startsWith("/v3/api-docs");
    }
}